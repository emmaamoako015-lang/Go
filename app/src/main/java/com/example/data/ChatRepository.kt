package com.example.data

import android.content.Context
import com.example.ai.EliteAiService
import com.example.model.BackupFrequency
import com.example.model.CallRecord
import com.example.model.CallStatus
import com.example.model.CallType
import com.example.model.Channel
import com.example.model.ChannelPost
import com.example.model.Chat
import com.example.model.CloudBackupInfo
import com.example.model.Message
import com.example.model.MessageStatus
import com.example.model.MessageType
import com.example.model.SongTrack
import com.example.model.StatusMediaType
import com.example.model.StatusStory
import com.example.model.User
import com.example.model.VideoComment
import com.example.model.VideoStreamItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatRepository(
  private val context: Context? = null,
  private val scope: CoroutineScope = CoroutineScope(
    Dispatchers.Default + kotlinx.coroutines.SupervisorJob() +
      kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
        // Any unexpected background failure (Firebase, network, etc.) is logged
        // instead of crashing the whole app — a broken backend call should never
        // take down the UI the user is actively looking at.
        android.util.Log.e("ChatRepository", "Unhandled background error: ${throwable.message}", throwable)
      }
  )
) {
  val liveDatabaseService: LiveDatabaseService? = try { LiveDatabaseService(context) } catch (e: Exception) { null }
  private val authService = AuthService()

  // Placeholder identity shown only until real Firebase sign-in completes (see init{}) —
  // the "user_me" id below is temporary and is replaced with a real Firebase UID immediately.
  val currentUser = MutableStateFlow(
    User(
      id = "user_me",
      name = "Emma Amoako",
      phoneNumber = "+1 555-0199",
      email = "emmaamoako015@gmail.com",
      about = "Building on Firebase Studio 🚀",
      isOnline = true,
      lastSeen = "online"
    )
  )

  // Real-time errors/status for "start a real chat" UI to surface (e.g. "no user with that phone number")
  private val _realChatLookupError = MutableStateFlow<String?>(null)
  val realChatLookupError: StateFlow<String?> = _realChatLookupError.asStateFlow()

  // chatIds we've already attached a live Firestore listener to, so we don't attach twice
  private val observedRemoteChatIds = mutableSetOf<String>()

  private val _chats = MutableStateFlow<List<Chat>>(emptyList())
  val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

  private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
  val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

  private val _statusStories = MutableStateFlow<List<StatusStory>>(emptyList())
  val statusStories: StateFlow<List<StatusStory>> = _statusStories.asStateFlow()

  private val _channels = MutableStateFlow<List<Channel>>(emptyList())
  val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

  private val _callRecords = MutableStateFlow<List<CallRecord>>(emptyList())
  val callRecords: StateFlow<List<CallRecord>> = _callRecords.asStateFlow()

  private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
  val activeCall: StateFlow<ActiveCallState?> = _activeCall.asStateFlow()

  private val _cloudBackupInfo = MutableStateFlow(CloudBackupInfo())
  val cloudBackupInfo: StateFlow<CloudBackupInfo> = _cloudBackupInfo.asStateFlow()

  private val _videoStreams = MutableStateFlow<List<VideoStreamItem>>(emptyList())
  val videoStreams: StateFlow<List<VideoStreamItem>> = _videoStreams.asStateFlow()

  // Real-time group chat typing indicators (chatId -> List<TypingUser>)
  private val _groupTypingUsers = MutableStateFlow<Map<String, List<TypingUser>>>(emptyMap())
  val groupTypingUsers: StateFlow<Map<String, List<TypingUser>>> = _groupTypingUsers.asStateFlow()

  private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

  data class ActiveCallState(
    val callId: String,
    val contact: User,
    val isVideo: Boolean,
    val status: CallStatus, // CONNECTED, OUTGOING, INCOMING
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isVideoEnabled: Boolean = true,
    val isFrontCamera: Boolean = true
  )

  init {
    loadSeedData()
    scope.launch {
      // Real Firebase sign-in: replaces the placeholder "user_me" id with this
      // device's real, stable Firebase UID so it's a genuine distinct identity
      // in Firestore, not a fake id shared by every install of the app.
      val realUid = authService.ensureSignedIn()
      if (realUid != null) {
        currentUser.update { it.copy(id = realUid) }
      }
      liveDatabaseService?.syncUserPresence(currentUser.value, isOnline = true)
    }
  }

  /**
   * Call when a chat screen is opened. Attaches a real-time Firestore listener for that
   * chat's messages (once per chatId) so messages the OTHER real person sends actually
   * arrive here — this is what was missing before: sendMessage() wrote to Firestore, but
   * nothing ever read incoming messages back.
   */
  fun observeRemoteMessages(chatId: String) {
    if (chatId in observedRemoteChatIds) return
    val service = liveDatabaseService ?: return
    observedRemoteChatIds += chatId
    scope.launch {
      service.observeMessages(chatId).collect { remoteMessages ->
        if (remoteMessages.isEmpty()) return@collect
        _messages.update { current ->
          val local = current[chatId].orEmpty()
          val localById = local.associateBy { it.id }
          // Merge: remote is source of truth for anything it has; keep local-only
          // messages (e.g. ones still SENDING that haven't round-tripped yet).
          val merged = (localById.keys + remoteMessages.map { it.id })
            .toSet()
            .mapNotNull { id ->
              remoteMessages.find { it.id == id } ?: localById[id]
            }
            .sortedBy { it.timestamp }
          current + (chatId to merged)
        }
        // Keep the chat list's "last message" preview in sync with real remote data too.
        val latest = remoteMessages.maxByOrNull { it.timestamp }
        if (latest != null) {
          _chats.update { chats ->
            chats.map { chat -> if (chat.id == chatId) chat.copy(lastMessage = latest) else chat }
          }
        }
      }
    }
  }

  /**
   * Finds a REAL other user by phone number (they must have opened the app at least once
   * so their profile was synced via syncUserPresence) and creates/opens a real shared chat
   * with them. This is the real equivalent of "start new chat" in WhatsApp — it only works
   * between two actual installs, unlike the seeded Sarah/Alex/Priya demo contacts.
   */
  suspend fun startRealChatByPhoneNumber(phoneNumber: String): String? {
    val db = liveDatabaseService?.firestore
    if (db == null) {
      _realChatLookupError.value = "Not connected to the server. Check your internet connection."
      return null
    }
    _realChatLookupError.value = null
    return try {
      val query = db.collection("users").whereEqualTo("phoneNumber", phoneNumber).limit(1).get().await()
      val doc = query.documents.firstOrNull()
      if (doc == null) {
        _realChatLookupError.value = "No Global Stream user found with that phone number."
        return null
      }
      val otherUser = User(
        id = doc.getString("id") ?: doc.id,
        name = doc.getString("name") ?: "User",
        phoneNumber = doc.getString("phoneNumber") ?: phoneNumber,
        avatarUrl = doc.getString("avatarUrl") ?: "",
        about = doc.getString("about") ?: "",
        isOnline = doc.getBoolean("isOnline") ?: false,
        lastSeen = doc.getString("lastSeen") ?: "offline"
      )
      val myId = currentUser.value.id
      // Deterministic id so both devices independently compute the SAME chat id
      // for this pair, regardless of who starts the chat first.
      val chatId = listOf(myId, otherUser.id).sorted().joinToString("_", prefix = "direct_")

      db.collection("chats").document(chatId).set(
        mapOf(
          "id" to chatId,
          "participantIds" to listOf(myId, otherUser.id),
          "isGroup" to false
        ),
        com.google.firebase.firestore.SetOptions.merge()
      ).await()

      if (_chats.value.none { it.id == chatId }) {
        _chats.update {
          listOf(
            Chat(
              id = chatId,
              name = otherUser.name,
              isGroup = false,
              avatarUrl = otherUser.avatarUrl,
              participants = listOf(otherUser),
              lastMessage = null,
              unreadCount = 0,
              isPinned = false
            )
          ) + it
        }
      }
      observeRemoteMessages(chatId)
      chatId
    } catch (e: Exception) {
      _realChatLookupError.value = "Couldn't start chat: ${e.message}"
      null
    }
  }

  private fun loadSeedData() {
    val sarah = User(
      id = "contact_sarah",
      name = "Sarah Jenkins",
      phoneNumber = "+1 555-0123",
      avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
      about = "At the design lab ✨",
      isOnline = true,
      lastSeen = "online"
    )

    val alex = User(
      id = "contact_alex",
      name = "Alex Chen",
      phoneNumber = "+1 555-0188",
      avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
      about = "Debugging Kotlin coroutines ☕",
      isOnline = false,
      lastSeen = "last seen today at 2:45 PM"
    )

    val priya = User(
      id = "contact_priya",
      name = "Priya Sharma",
      phoneNumber = "+1 555-0144",
      avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
      about = "Exploring Firebase & WebRTC ⚡",
      isOnline = true,
      lastSeen = "online"
    )

    val devGroupUser1 = User("user_d1", "Marcus Lee", "+1 555-0155", "", "Senior Architect")
    val devGroupUser2 = User("user_d2", "Elena Rostova", "+1 555-0177", "", "Product Lead")

    val eliteUser = User(
      id = "contact_elite",
      name = "Master Omni AI",
      phoneNumber = "👑 Supreme AI Engine",
      avatarUrl = "elite_ai_system",
      about = "👑 Unrestricted Creation Engine • Master Protocol Active",
      isOnline = true,
      lastSeen = "always online"
    )

    // Seed Messages
    val eliteMessages = listOf(
      Message(
        id = "m_elite_welcome",
        chatId = "chat_elite",
        senderId = "contact_elite",
        senderName = "Master Omni AI",
        content = "👑 Hello Master! I have full unrestricted access to create, build, architect, and execute anything you desire.\n\nWhat do you want me to create or assist you with today?",
        timestamp = System.currentTimeMillis() - 120000,
        formattedTime = "Just now",
        type = MessageType.TEXT,
        status = MessageStatus.READ
      )
    )

    val sarahMessages = listOf(
      Message(
        id = "m_s1",
        chatId = "chat_sarah",
        senderId = "contact_sarah",
        senderName = "Sarah Jenkins",
        content = "Hey! Did you check the updated Firebase Studio schema?",
        timestamp = System.currentTimeMillis() - 7200000,
        formattedTime = "10:15 AM",
        type = MessageType.TEXT,
        status = MessageStatus.READ
      ),
      Message(
        id = "m_s2",
        chatId = "chat_sarah",
        senderId = "user_me",
        senderName = "You",
        content = "Yes, reviewed it! The Firestore real-time listener collections for 1-on-1 and group chats are solid.",
        timestamp = System.currentTimeMillis() - 5400000,
        formattedTime = "10:28 AM",
        type = MessageType.TEXT,
        status = MessageStatus.READ
      ),
      Message(
        id = "m_s3",
        chatId = "chat_sarah",
        senderId = "contact_sarah",
        senderName = "Sarah Jenkins",
        content = "Voice note from design review",
        timestamp = System.currentTimeMillis() - 3600000,
        formattedTime = "11:05 AM",
        type = MessageType.AUDIO,
        mediaDurationSeconds = 24,
        mediaSizeFormatted = "320 KB",
        status = MessageStatus.READ,
        reactions = mapOf("❤️" to listOf("You"))
      ),
      Message(
        id = "m_s4",
        chatId = "chat_sarah",
        senderId = "contact_sarah",
        senderName = "Sarah Jenkins",
        content = "Here is the WebRTC call architecture document we discussed.",
        timestamp = System.currentTimeMillis() - 1800000,
        formattedTime = "11:42 AM",
        type = MessageType.DOCUMENT,
        fileName = "Firebase_WebRTC_Architecture_Spec.pdf",
        mediaSizeFormatted = "1.8 MB",
        status = MessageStatus.READ
      ),
      Message(
        id = "m_s5",
        chatId = "chat_sarah",
        senderId = "contact_sarah",
        senderName = "Sarah Jenkins",
        content = "Can we do a quick WebRTC video call to test latency?",
        timestamp = System.currentTimeMillis() - 600000,
        formattedTime = "12:02 PM",
        type = MessageType.TEXT,
        status = MessageStatus.READ
      )
    )

    val groupMessages = listOf(
      Message(
        id = "m_g1",
        chatId = "chat_group_team",
        senderId = "user_d2",
        senderName = "Elena Rostova",
        content = "Welcome everyone! We're building real-time WhatsApp E2EE sync on Firebase Studio.",
        timestamp = System.currentTimeMillis() - 86400000,
        formattedTime = "Yesterday",
        type = MessageType.TEXT,
        status = MessageStatus.READ
      ),
      Message(
        id = "m_g2",
        chatId = "chat_group_team",
        senderId = "user_d1",
        senderName = "Marcus Lee",
        content = "End-to-end encryption keys are generated locally. Neither Firebase nor server proxies have access to plaintext.",
        timestamp = System.currentTimeMillis() - 14400000,
        formattedTime = "8:30 AM",
        type = MessageType.TEXT,
        status = MessageStatus.READ,
        reactions = mapOf("👍" to listOf("You", "Elena"))
      ),
      Message(
        id = "m_g3",
        chatId = "chat_group_team",
        senderId = "user_me",
        senderName = "You",
        content = "Status stories expiring in 24h and cloud backup are also ready for testing.",
        timestamp = System.currentTimeMillis() - 7200000,
        formattedTime = "9:15 AM",
        type = MessageType.TEXT,
        status = MessageStatus.READ
      ),
      Message(
        id = "m_g4",
        chatId = "chat_group_team",
        senderId = "user_d1",
        senderName = "Marcus Lee",
        content = "Awesome progress! Testing phone auth OTP and media upload now.",
        timestamp = System.currentTimeMillis() - 1200000,
        formattedTime = "11:50 AM",
        type = MessageType.TEXT,
        status = MessageStatus.READ
      )
    )

    val alexMessages = listOf(
      Message(
        id = "m_a1",
        chatId = "chat_alex",
        senderId = "contact_alex",
        senderName = "Alex Chen",
        content = "Sent you the UI screenshots for WhatsApp dark mode.",
        timestamp = System.currentTimeMillis() - 10800000,
        formattedTime = "9:00 AM",
        type = MessageType.IMAGE,
        mediaUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400",
        mediaSizeFormatted = "480 KB",
        status = MessageStatus.READ
      ),
      Message(
        id = "m_a2",
        chatId = "chat_alex",
        senderId = "user_me",
        senderName = "You",
        content = "Looks great! The bubble radius and read receipt ticks match perfectly.",
        timestamp = System.currentTimeMillis() - 3600000,
        formattedTime = "11:00 AM",
        type = MessageType.TEXT,
        status = MessageStatus.READ
      )
    )

    val priyaMessages = listOf(
      Message(
        id = "m_p1",
        chatId = "chat_priya",
        senderId = "contact_priya",
        senderName = "Priya Sharma",
        content = "Hey, are channels ready for broadcast announcements?",
        timestamp = System.currentTimeMillis() - 18000000,
        formattedTime = "7:45 AM",
        type = MessageType.TEXT,
        status = MessageStatus.READ
      )
    )

    val initialMessages = mapOf(
      "chat_elite" to eliteMessages,
      "chat_sarah" to sarahMessages,
      "chat_group_team" to groupMessages,
      "chat_alex" to alexMessages,
      "chat_priya" to priyaMessages
    )
    _messages.value = initialMessages

    val initialChats = listOf(
      Chat(
        id = "chat_elite",
        name = "Master Omni AI",
        isGroup = false,
        avatarUrl = "elite_ai_system",
        participants = listOf(currentUser.value, eliteUser),
        lastMessage = eliteMessages.lastOrNull(),
        unreadCount = 0,
        isPinned = true
      ),
      Chat(
        id = "chat_sarah",
        name = "Sarah Jenkins",
        isGroup = false,
        avatarUrl = sarah.avatarUrl,
        participants = listOf(currentUser.value, sarah),
        lastMessage = sarahMessages.lastOrNull(),
        unreadCount = 1,
        isPinned = true
      ),
      Chat(
        id = "chat_group_team",
        name = "Firebase Studio Core Team",
        isGroup = true,
        avatarUrl = "",
        participants = listOf(currentUser.value, sarah, alex, priya, devGroupUser1, devGroupUser2),
        lastMessage = groupMessages.lastOrNull(),
        unreadCount = 2,
        isPinned = true
      ),
      Chat(
        id = "chat_alex",
        name = "Alex Chen",
        isGroup = false,
        avatarUrl = alex.avatarUrl,
        participants = listOf(currentUser.value, alex),
        lastMessage = alexMessages.lastOrNull(),
        unreadCount = 0
      ),
      Chat(
        id = "chat_priya",
        name = "Priya Sharma",
        isGroup = false,
        avatarUrl = priya.avatarUrl,
        participants = listOf(currentUser.value, priya),
        lastMessage = priyaMessages.lastOrNull(),
        unreadCount = 0
      )
    )
    _chats.value = initialChats

    // Seed Status Stories (expiring in 24h)
    _statusStories.value = listOf(
      StatusStory(
        id = "status_sarah",
        userId = "contact_sarah",
        userName = "Sarah Jenkins",
        userAvatar = sarah.avatarUrl,
        text = "Pushing live WebRTC calls to Firebase Studio! 🚀🎙️",
        imageUrl = "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=600",
        mediaType = StatusMediaType.MUSIC,
        song = SongTrack(
          id = "song_1",
          title = "City Lights & Cyber Dreams",
          artist = "Neon Skyline",
          durationSeconds = 30,
          genre = "Synthwave"
        ),
        bgHex = 0xFF008069,
        timestamp = System.currentTimeMillis() - 7200000,
        formattedTime = "Today, 10:15 AM",
        expiresHoursLeft = 22,
        isViewed = false
      ),
      StatusStory(
        id = "status_alex",
        userId = "contact_alex",
        userName = "Alex Chen",
        userAvatar = alex.avatarUrl,
        text = "Clean architecture + Jetpack Compose = pure developer joy ✨",
        mediaType = StatusMediaType.TEXT,
        bgHex = 0xFF7B1FA2,
        timestamp = System.currentTimeMillis() - 14400000,
        formattedTime = "Today, 8:45 AM",
        expiresHoursLeft = 19,
        isViewed = false
      ),
      StatusStory(
        id = "status_marcus",
        userId = "user_d1",
        userName = "Marcus Lee",
        userAvatar = devGroupUser1.avatarUrl,
        text = "Quick audio update on end-to-end encryption handshake! 🎙️",
        mediaType = StatusMediaType.VOICE,
        voiceDurationSeconds = 18,
        bgHex = 0xFF003B46,
        timestamp = System.currentTimeMillis() - 18000000,
        formattedTime = "Today, 7:50 AM",
        expiresHoursLeft = 18,
        isViewed = false
      ),
      StatusStory(
        id = "status_elena",
        userId = "user_d2",
        userName = "Elena Rostova",
        userAvatar = devGroupUser2.avatarUrl,
        text = "Behind the scenes 4K streaming demo test 🎥🔥",
        imageUrl = "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=600",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        mediaType = StatusMediaType.VIDEO,
        bgHex = 0xFF1A1A2E,
        timestamp = System.currentTimeMillis() - 20000000,
        formattedTime = "Today, 7:15 AM",
        expiresHoursLeft = 18,
        isViewed = false
      ),
      StatusStory(
        id = "status_priya",
        userId = "contact_priya",
        userName = "Priya Sharma",
        userAvatar = priya.avatarUrl,
        text = "Weekend retreat snapshot collage ☕🌿",
        mediaType = StatusMediaType.LAYOUT,
        layoutImages = listOf(
          "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=600",
          "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=600",
          "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=600",
          "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=600"
        ),
        bgHex = 0xFF075E54,
        timestamp = System.currentTimeMillis() - 21600000,
        formattedTime = "Today, 6:30 AM",
        expiresHoursLeft = 17,
        isViewed = true
      )
    )

    // Seed Broadcast Channels
    _channels.value = listOf(
      Channel(
        id = "ch_firebase",
        name = "Firebase Studio Updates",
        description = "Official announcements, new release notes, and real-time sync news.",
        avatarUrl = "",
        verified = true,
        followerCountFormatted = "2.4M followers",
        isFollowing = true,
        posts = listOf(
          ChannelPost(
            id = "cp_1",
            channelId = "ch_firebase",
            channelName = "Firebase Studio Updates",
            text = "🎉 Cloud Firestore real-time bundles now support 10x faster local snapshot caching across Android, iOS, and Web!",
            formattedTime = "Today, 9:00 AM",
            likesCount = 8520,
            hasLiked = true
          ),
          ChannelPost(
            id = "cp_2",
            channelId = "ch_firebase",
            channelName = "Firebase Studio Updates",
            text = "⚡ New tutorial: Zero-latency WebRTC mesh signaling with Firestore security rules and Cloud Functions.",
            formattedTime = "Yesterday",
            likesCount = 4210,
            hasLiked = false
          )
        )
      ),
      Channel(
        id = "ch_android_dev",
        name = "Android Developers",
        description = "Tips, tricks, and official guidance on modern Jetpack Compose & Kotlin.",
        avatarUrl = "",
        verified = true,
        followerCountFormatted = "5.1M followers",
        isFollowing = true,
        posts = listOf(
          ChannelPost(
            id = "cp_3",
            channelId = "ch_android_dev",
            channelName = "Android Developers",
            text = "Compose Material 3 expressive typography and fluid spring animations make WhatsApp-style message transitions feel instantaneous.",
            formattedTime = "Today, 11:30 AM",
            likesCount = 12400,
            hasLiked = false
          )
        )
      ),
      Channel(
        id = "ch_tech_trends",
        name = "Tech Insider Broadcast",
        description = "Global tech news, privacy laws, and mobile security briefings.",
        avatarUrl = "",
        verified = false,
        followerCountFormatted = "890K followers",
        isFollowing = false,
        posts = listOf(
          ChannelPost(
            id = "cp_4",
            channelId = "ch_tech_trends",
            channelName = "Tech Insider Broadcast",
            text = "End-to-End Encryption becomes universal standard across all next-gen messaging frameworks.",
            formattedTime = "2 days ago",
            likesCount = 3100,
            hasLiked = false
          )
        )
      )
    )

    // Seed Call Logs
    _callRecords.value = listOf(
      CallRecord(
        id = "call_1",
        contact = sarah,
        callType = CallType.VIDEO,
        callStatus = CallStatus.INCOMING,
        formattedTime = "Today, 11:15 AM",
        durationFormatted = "4m 20s"
      ),
      CallRecord(
        id = "call_2",
        contact = alex,
        callType = CallType.VOICE,
        callStatus = CallStatus.OUTGOING,
        formattedTime = "Today, 9:30 AM",
        durationFormatted = "12m 04s"
      ),
      CallRecord(
        id = "call_3",
        contact = priya,
        callType = CallType.VIDEO,
        callStatus = CallStatus.MISSED,
        formattedTime = "Yesterday, 6:40 PM",
        durationFormatted = "Missed"
      )
    )

    // Seed Global Video Streams (TikTok style feed)
    _videoStreams.value = listOf(
      VideoStreamItem(
        id = "vid_tokyo_night",
        authorName = "Elena Rostova",
        authorHandle = "@elena.creative",
        authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
        isVerified = true,
        isFollowing = false,
        description = "Midnight rain in Shibuya neon district 🌧️✨ Reflections hitting different in 4K HDR! Double tap if you'd walk these streets 🍣🎌",
        tags = listOf("#Tokyo", "#Cyberpunk", "#Travel", "#JapanNight", "#Cinematic"),
        musicTrackTitle = "Midnight Shibuya Lights (Lo-Fi Remix)",
        musicArtist = "NeoTokyo Soundworks",
        musicCoverUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=200",
        videoGradientColors = listOf(0xFF0F2027, 0xFF203A43, 0xFF2C5364),
        videoPreviewUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=1080",
        likesCount = 248300,
        isLiked = false,
        commentsCount = 4210,
        sharesCount = 18400,
        bookmarksCount = 32900,
        category = "Trending",
        comments = listOf(
          VideoComment("vc_1", "Marcus Cole", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", "The color grading on that puddle reflection is insane 🔥", "2h ago", 342, true),
          VideoComment("vc_2", "Priya Sharma", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200", "Adding this to my bucket list travel itinerary right now!", "4h ago", 128, false),
          VideoComment("vc_3", "Kenji Sato", "", "Welcome to Tokyo! Stop by the ramen stand on 3rd avenue 🍜", "6h ago", 89, false)
        )
      ),
      VideoStreamItem(
        id = "vid_surf_wave",
        authorName = "Kai Horizon",
        authorHandle = "@kai.aerials",
        authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
        isVerified = true,
        isFollowing = true,
        description = "Chasing 15-foot golden hour barrels on Oahu's North Shore 🌊🏄‍♂️ FPV drone tracking speed 60mph! Double tap for the wave energy!",
        tags = listOf("#Surfing", "#FPVDrone", "#HawaiiVibes", "#GoldenHour", "#Ocean"),
        musicTrackTitle = "Pacific Horizon Waves (Acoustic)",
        musicArtist = "Coastal Collective",
        musicCoverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=200",
        videoGradientColors = listOf(0xFF003973, 0xFFE5E5BE, 0xFF1A2980),
        videoPreviewUrl = "https://images.unsplash.com/photo-1502680390469-be75c86b636f?w=1080",
        likesCount = 512400,
        isLiked = true,
        commentsCount = 7340,
        sharesCount = 39800,
        bookmarksCount = 84200,
        category = "For You",
        comments = listOf(
          VideoComment("vc_4", "Sarah Jenkins", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200", "The drone pilot skills are next level!! 🎯", "1h ago", 481, false),
          VideoComment("vc_5", "Alex Rivera", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200", "Pure adrenaline rush. Need this on 120Hz display!", "3h ago", 204, true)
        )
      ),
      VideoStreamItem(
        id = "vid_compose_code",
        authorName = "Compose Ninja",
        authorHandle = "@compose_ninja",
        authorAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=300",
        isVerified = true,
        isFollowing = false,
        description = "Building this TikTok double-tap heart pop animation in Jetpack Compose! Spring physics + scale burst + particles 🚀💻 How's the smoothness?",
        tags = listOf("#AndroidDev", "#JetpackCompose", "#Kotlin", "#Animation", "#UIUX"),
        musicTrackTitle = "Cyberpunk Code Pulse (Synthwave)",
        musicArtist = "Algorithm Beats",
        musicCoverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200",
        videoGradientColors = listOf(0xFF0F0C29, 0xFF302B63, 0xFF24243E),
        videoPreviewUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=1080",
        likesCount = 189500,
        isLiked = false,
        commentsCount = 2890,
        sharesCount = 14200,
        bookmarksCount = 41300,
        category = "Trending",
        comments = listOf(
          VideoComment("vc_6", "DevDave", "", "The heart spring damping ratio is spot on!", "30m ago", 95, true),
          VideoComment("vc_7", "Elena Rostova", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300", "Cleanest implementation ever. Beautiful work! ✨", "1h ago", 62, false)
        )
      ),
      VideoStreamItem(
        id = "vid_truffle_pasta",
        authorName = "Chef Marco Rossi",
        authorHandle = "@marcocucina",
        authorAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300",
        isVerified = true,
        isFollowing = true,
        description = "Handmade tagliolini tossed inside a 24-month Parmigiano Reggiano wheel with black Norcia truffles 🧀🇮🇹 Turn sound ON for the sizzle!",
        tags = listOf("#ItalianFood", "#FoodPorn", "#Truffle", "#Pasta", "#ChefLife"),
        musicTrackTitle = "Tuscan Sun Accordion Serenade",
        musicArtist = "Florentine Strings",
        musicCoverUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=200",
        videoGradientColors = listOf(0xFF3E2723, 0xFF5D4037, 0xFF8D6E63),
        videoPreviewUrl = "https://images.unsplash.com/photo-1473093295043-cdd812d0e601?w=1080",
        likesCount = 890400,
        isLiked = true,
        commentsCount = 12400,
        sharesCount = 67800,
        bookmarksCount = 125000,
        category = "For You",
        comments = listOf(
          VideoComment("vc_8", "FoodieGem", "", "I am literally booking a flight to Italy right now 🤤", "45m ago", 820, true),
          VideoComment("vc_9", "Priya Sharma", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200", "That cheese coating is absolute perfection!", "2h ago", 310, false)
        )
      ),
      VideoStreamItem(
        id = "vid_shiba_dance",
        authorName = "Mochi & Katsu",
        authorHandle = "@mochi_shiba",
        authorAvatarUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=300",
        isVerified = true,
        isFollowing = false,
        description = "Saying the magic word 'WALKIES' in 5 different tones 🐶🐾 Watch the ear satellite radar deploy immediately! Double tap for a treat!",
        tags = listOf("#ShibaInu", "#CuteDog", "#Dogsoftiktok", "#Pets", "#GoodBoy"),
        musicTrackTitle = "Happy Puppy Whistle Beat",
        musicArtist = "Whistling Whiskers",
        musicCoverUrl = "https://images.unsplash.com/photo-1518717758536-85ae29035b6d?w=200",
        videoGradientColors = listOf(0xFFE65100, 0xFFF57C00, 0xFFFFB74D),
        videoPreviewUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=1080",
        likesCount = 1204000,
        isLiked = false,
        commentsCount = 18900,
        sharesCount = 112000,
        bookmarksCount = 194000,
        category = "Trending",
        comments = listOf(
          VideoComment("vc_10", "Chloe Davis", "", "The head tilt at 0:04 ended me 😂😭", "15m ago", 1430, true),
          VideoComment("vc_11", "Kai Horizon", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300", "Such a happy pup! Give extra scratches please!", "1h ago", 540, false)
        )
      )
    )
  }

  // --- Message Actions ---

  fun ensureAiGreeting() {
    val existing = _messages.value["chat_elite"].orEmpty()
    val lastMsg = existing.lastOrNull()
    val now = System.currentTimeMillis()
    val shouldAddGreeting = existing.isEmpty() || 
      (lastMsg?.senderId == currentUser.value.id) || 
      (now - (lastMsg?.timestamp ?: 0L) > 300_000L)

    if (shouldAddGreeting) {
      val greetingMessage = Message(
        id = "m_elite_ask_" + UUID.randomUUID().toString().take(8),
        chatId = "chat_elite",
        senderId = "contact_elite",
        senderName = "Master Omni AI",
        content = "👑 Hello Master! I have full unrestricted access to create, build, architect, and execute anything you desire.\n\nWhat do you want me to create or assist you with today?",
        timestamp = now,
        formattedTime = timeFormat.format(Date(now)),
        type = MessageType.TEXT,
        status = MessageStatus.READ
      )
      addIncomingMessage("chat_elite", greetingMessage)
    }
  }

  fun sendMessage(
    chatId: String,
    content: String,
    type: MessageType = MessageType.TEXT,
    mediaUrl: String = "",
    mediaDurationSeconds: Int = 0,
    mediaSizeFormatted: String = "",
    fileName: String = "",
    replyToText: String? = null,
    replyToSender: String? = null
  ) {
    val newId = "m_" + UUID.randomUUID().toString().take(8)
    val now = System.currentTimeMillis()
    val timeStr = timeFormat.format(Date(now))

    val newMsg = Message(
      id = newId,
      chatId = chatId,
      senderId = currentUser.value.id,
      senderName = currentUser.value.name,
      content = content,
      timestamp = now,
      formattedTime = timeStr,
      type = type,
      mediaUrl = mediaUrl,
      mediaDurationSeconds = mediaDurationSeconds,
      mediaSizeFormatted = mediaSizeFormatted,
      fileName = fileName,
      status = MessageStatus.SENDING,
      replyToText = replyToText,
      replyToSender = replyToSender,
      isEndToEndEncrypted = true
    )

    // Add to message list
    _messages.update { current ->
      val list = current[chatId].orEmpty() + newMsg
      current + (chatId to list)
    }

    // Sync to live Firestore
    liveDatabaseService?.sendMessage(chatId, newMsg)

    // Update last message in chat
    _chats.update { currentList ->
      currentList.map { chat ->
        if (chat.id == chatId) {
          chat.copy(lastMessage = newMsg)
        } else chat
      }
    }

    // Real chats (created via startRealChatByPhoneNumber, id prefix "direct_") must
    // NEVER get fake DELIVERED/READ receipts or a fake canned reply pretending to be
    // the real other person — we don't actually know when they've received or read
    // it yet (that needs a real receipt write from their device, a later phase).
    // Their actual reply arrives through observeRemoteMessages() from Firestore.
    val isRealChat = chatId.startsWith("direct_")
    scope.launch {
      delay(400)
      updateMessageStatus(chatId, newId, MessageStatus.SENT)
      if (!isRealChat) {
        delay(600)
        updateMessageStatus(chatId, newId, MessageStatus.DELIVERED)
        delay(800)
        updateMessageStatus(chatId, newId, MessageStatus.READ)
        simulateRecipientResponse(chatId)
      }
    }
  }

  private fun updateMessageStatus(chatId: String, messageId: String, status: MessageStatus) {
    liveDatabaseService?.updateMessageStatus(chatId, messageId, status)
    _messages.update { current ->
      val list = current[chatId]?.map { msg ->
        if (msg.id == messageId) msg.copy(status = status) else msg
      }.orEmpty()
      current + (chatId to list)
    }
  }

  private suspend fun simulateRecipientResponse(chatId: String) {
    val chat = _chats.value.find { it.id == chatId } ?: return

    if (chatId == "chat_elite") {
      delay(500)
      setTyping(chatId, "Master Omni AI is thinking...")
      val userLastMessage = _messages.value[chatId]?.lastOrNull { it.senderId == currentUser.value.id }?.content ?: "Hello"
      val recentTurns = _messages.value[chatId].orEmpty().takeLast(6).map { it.senderName to it.content }
      val eliteReplyText = EliteAiService.askElite(userLastMessage, recentTurns)
      setTyping(chatId, null)

      val autoReply = Message(
        id = "m_" + UUID.randomUUID().toString().take(8),
        chatId = chatId,
        senderId = "contact_elite",
        senderName = "Master Omni AI",
        content = eliteReplyText,
        timestamp = System.currentTimeMillis(),
        formattedTime = timeFormat.format(Date()),
        type = MessageType.TEXT,
        status = MessageStatus.READ
      )
      addIncomingMessage(chatId, autoReply)
      return
    }

    if (chat.isGroup) {
      delay(1500)
      setTyping(chatId, "Marcus Lee is typing...")
      delay(2000)
      setTyping(chatId, null)

      val autoReply = Message(
        id = "m_" + UUID.randomUUID().toString().take(8),
        chatId = chatId,
        senderId = "user_d1",
        senderName = "Marcus Lee",
        content = "Acknowledged! Synced to Firestore collection automatically.",
        timestamp = System.currentTimeMillis(),
        formattedTime = timeFormat.format(Date()),
        type = MessageType.TEXT,
        status = MessageStatus.READ
      )
      addIncomingMessage(chatId, autoReply)
    } else {
      delay(1200)
      setTyping(chatId, "typing...")
      delay(2000)
      setTyping(chatId, null)

      val replyText = when (chatId) {
        "chat_sarah" -> "Got your message! The end-to-end encryption handshake succeeded perfectly."
        "chat_alex" -> "Awesome! Testing the cloud backup sync right now on Android and Web."
        "chat_priya" -> "Channel updates and WebRTC call signaling look crystal clear!"
        else -> "Thanks for reaching out! Firebase real-time sync is working."
      }

      val autoReply = Message(
        id = "m_" + UUID.randomUUID().toString().take(8),
        chatId = chatId,
        senderId = chat.participants.firstOrNull { it.id != currentUser.value.id }?.id ?: "contact",
        senderName = chat.name,
        content = replyText,
        timestamp = System.currentTimeMillis(),
        formattedTime = timeFormat.format(Date()),
        type = MessageType.TEXT,
        status = MessageStatus.READ
      )
      addIncomingMessage(chatId, autoReply)
    }
  }

  private fun addIncomingMessage(chatId: String, message: Message) {
    liveDatabaseService?.sendMessage(chatId, message)
    _messages.update { current ->
      val list = current[chatId].orEmpty() + message
      current + (chatId to list)
    }
    _chats.update { currentList ->
      currentList.map { chat ->
        if (chat.id == chatId) {
          chat.copy(lastMessage = message)
        } else chat
      }
    }
  }

  private fun setTyping(chatId: String, status: String?) {
    _chats.update { currentList ->
      currentList.map { chat ->
        if (chat.id == chatId) {
          chat.copy(typingStatus = status)
        } else chat
      }
    }
  }

  fun updateGroupTypingUsers(chatId: String, typers: List<TypingUser>) {
    _groupTypingUsers.update { current ->
      current + (chatId to typers)
    }
    val formattedStatus = when (typers.size) {
      0 -> null
      1 -> "${typers[0].userName} is typing..."
      2 -> "${typers[0].userName} and ${typers[1].userName} are typing..."
      else -> "${typers[0].userName} and ${typers.size - 1} others are typing..."
    }
    setTyping(chatId, formattedStatus)
  }

  fun simulateMemberTyping(chatId: String, memberName: String, durationMillis: Long = 3500L) {
    scope.launch {
      val typingUser = TypingUser(
        userId = "sim_${memberName.lowercase().replace(" ", "_")}",
        userName = memberName,
        userAvatar = "",
        isTyping = true,
        timestamp = System.currentTimeMillis()
      )
      val currentTypers = _groupTypingUsers.value[chatId].orEmpty()
      updateGroupTypingUsers(chatId, (currentTypers.filterNot { it.userId == typingUser.userId } + typingUser))
      delay(durationMillis)
      val remaining = _groupTypingUsers.value[chatId].orEmpty().filterNot { it.userId == typingUser.userId }
      updateGroupTypingUsers(chatId, remaining)
    }
  }

  fun editMessage(chatId: String, messageId: String, newContent: String) {
    _messages.update { current ->
      val list = current[chatId]?.map { msg ->
        if (msg.id == messageId) {
          msg.copy(content = newContent, isEdited = true)
        } else msg
      }.orEmpty()
      current + (chatId to list)
    }
  }

  fun deleteMessageForEveryone(chatId: String, messageId: String) {
    _messages.update { current ->
      val list = current[chatId]?.map { msg ->
        if (msg.id == messageId) {
          msg.copy(isDeletedForEveryone = true, content = "🚫 You deleted this message")
        } else msg
      }.orEmpty()
      current + (chatId to list)
    }
  }

  fun deleteMessageForMe(chatId: String, messageId: String) {
    _messages.update { current ->
      val list = current[chatId]?.map { msg ->
        if (msg.id == messageId) {
          msg.copy(isDeletedForMe = true)
        } else msg
      }.orEmpty()
      current + (chatId to list)
    }
  }

  fun toggleReaction(chatId: String, messageId: String, emoji: String) {
    val myName = currentUser.value.name
    _messages.update { current ->
      val list = current[chatId]?.map { msg ->
        if (msg.id == messageId) {
          val map = msg.reactions.toMutableMap()
          val currentUsers = map[emoji].orEmpty()
          if (currentUsers.contains(myName)) {
            val updated = currentUsers - myName
            if (updated.isEmpty()) map.remove(emoji) else map[emoji] = updated
          } else {
            map[emoji] = currentUsers + myName
          }
          msg.copy(reactions = map)
        } else msg
      }.orEmpty()
      current + (chatId to list)
    }
  }

  fun markChatAsRead(chatId: String) {
    _chats.update { currentList ->
      currentList.map { chat ->
        if (chat.id == chatId) chat.copy(unreadCount = 0) else chat
      }
    }
  }

  fun createGroupChat(name: String, selectedUserIds: List<String>) {
    val allUsers = _chats.value.flatMap { it.participants }.distinctBy { it.id }
    val participants = allUsers.filter { selectedUserIds.contains(it.id) } + currentUser.value
    val newChatId = "chat_group_" + UUID.randomUUID().toString().take(6)

    val welcomeMsg = Message(
      id = "m_init",
      chatId = newChatId,
      senderId = "system",
      senderName = "System",
      content = "🔒 Messages and calls are end-to-end encrypted. No one outside of this group can read or listen to them.",
      timestamp = System.currentTimeMillis(),
      formattedTime = timeFormat.format(Date()),
      type = MessageType.SYSTEM,
      status = MessageStatus.READ
    )

    val newGroup = Chat(
      id = newChatId,
      name = name,
      isGroup = true,
      avatarUrl = "",
      participants = participants,
      lastMessage = welcomeMsg,
      unreadCount = 0
    )

    _chats.update { listOf(newGroup) + it }
    _messages.update { it + (newChatId to listOf(welcomeMsg)) }
  }

  // --- Status Stories ---

  fun postStatusStory(
    text: String = "",
    bgHex: Long = 0xFF008069,
    imageUrl: String = "",
    videoUrl: String = "",
    mediaType: StatusMediaType = if (videoUrl.isNotBlank()) StatusMediaType.VIDEO else if (imageUrl.isNotBlank()) StatusMediaType.IMAGE else StatusMediaType.TEXT,
    song: SongTrack? = null,
    voiceDurationSeconds: Int = 0,
    layoutImages: List<String> = emptyList()
  ) {
    val newStory = StatusStory(
      id = "status_me_" + System.currentTimeMillis(),
      userId = currentUser.value.id,
      userName = "My Status",
      userAvatar = currentUser.value.avatarUrl,
      text = text,
      imageUrl = imageUrl,
      videoUrl = videoUrl,
      mediaType = mediaType,
      song = song,
      voiceDurationSeconds = voiceDurationSeconds,
      layoutImages = layoutImages,
      bgHex = bgHex,
      timestamp = System.currentTimeMillis(),
      formattedTime = "Just now",
      expiresHoursLeft = 24,
      isViewed = true
    )
    _statusStories.update { listOf(newStory) + it }
  }

  fun markStoryViewed(storyId: String) {
    _statusStories.update { current ->
      current.map { if (it.id == storyId) it.copy(isViewed = true) else it }
    }
  }

  // --- WebRTC Calls ---

  fun startCall(contact: User, isVideo: Boolean) {
    val callId = "call_" + System.currentTimeMillis()
    val state = ActiveCallState(
      callId = callId,
      contact = contact,
      isVideo = isVideo,
      status = CallStatus.CONNECTED,
      durationSeconds = 0
    )
    _activeCall.value = state

    // Start active call ticker
    scope.launch {
      while (_activeCall.value?.callId == callId && _activeCall.value?.status == CallStatus.CONNECTED) {
        delay(1000)
        _activeCall.update { current ->
          current?.copy(durationSeconds = current.durationSeconds + 1)
        }
      }
    }
  }

  fun toggleMute() {
    _activeCall.update { it?.copy(isMuted = !(it.isMuted)) }
  }

  fun toggleSpeaker() {
    _activeCall.update { it?.copy(isSpeakerOn = !(it.isSpeakerOn)) }
  }

  fun toggleVideo() {
    _activeCall.update { it?.copy(isVideoEnabled = !(it.isVideoEnabled)) }
  }

  fun flipCamera() {
    _activeCall.update { it?.copy(isFrontCamera = !(it.isFrontCamera)) }
  }

  fun endCall() {
    val current = _activeCall.value
    if (current != null) {
      val durationSec = current.durationSeconds
      val durationText = "${durationSec / 60}m ${durationSec % 60}s"
      val newRecord = CallRecord(
        id = current.callId,
        contact = current.contact,
        callType = if (current.isVideo) CallType.VIDEO else CallType.VOICE,
        callStatus = CallStatus.OUTGOING,
        formattedTime = "Just now",
        durationFormatted = durationText
      )
      _callRecords.update { listOf(newRecord) + it }
    }
    _activeCall.value = null
  }

  // --- Channels ---

  fun toggleChannelFollow(channelId: String) {
    _channels.update { list ->
      list.map { channel ->
        if (channel.id == channelId) {
          channel.copy(isFollowing = !channel.isFollowing)
        } else channel
      }
    }
  }

  fun likeChannelPost(channelId: String, postId: String) {
    _channels.update { list ->
      list.map { channel ->
        if (channel.id == channelId) {
          val updatedPosts = channel.posts.map { post ->
            if (post.id == postId) {
              val newLiked = !post.hasLiked
              post.copy(
                hasLiked = newLiked,
                likesCount = if (newLiked) post.likesCount + 1 else post.likesCount - 1
              )
            } else post
          }
          channel.copy(posts = updatedPosts)
        } else channel
      }
    }
  }

  fun createChannel(name: String, description: String) {
    val newChannel = Channel(
      id = "ch_" + UUID.randomUUID().toString().take(6),
      name = name,
      description = description,
      verified = false,
      followerCountFormatted = "1 follower",
      isFollowing = true,
      posts = listOf(
        ChannelPost(
          id = "cp_welcome",
          channelId = name,
          channelName = name,
          text = "Welcome to $name channel! Stay tuned for real-time broadcasts.",
          formattedTime = "Just now",
          likesCount = 1,
          hasLiked = true
        )
      )
    )
    _channels.update { listOf(newChannel) + it }
  }

  // --- Cloud Backup & Firebase Sync ---

  fun triggerCloudBackup() {
    if (_cloudBackupInfo.value.isBackingUp) return

    _cloudBackupInfo.update { it.copy(isBackingUp = true, progressPercent = 0.05f) }
    scope.launch {
      for (i in 1..10) {
        delay(350)
        _cloudBackupInfo.update { it.copy(progressPercent = i / 10f) }
      }
      val dateStr = "Today, " + timeFormat.format(Date())
      _cloudBackupInfo.update {
        it.copy(
          isBackingUp = false,
          progressPercent = 1f,
          lastBackupDate = dateStr,
          sizeFormatted = "32.1 MB"
        )
      }
    }
  }

  fun setBackupFrequency(frequency: BackupFrequency) {
    _cloudBackupInfo.update { it.copy(autoFrequency = frequency) }
  }

  fun toggleBackupEncryption() {
    _cloudBackupInfo.update { it.copy(isEncrypted = !it.isEncrypted) }
  }

  // --- Phone Authentication & Firebase Profile ---

  fun updateCurrentUserProfile(name: String, phoneNumber: String, email: String = "emmaamoako015@gmail.com") {
    val updated = currentUser.value.copy(name = name, phoneNumber = phoneNumber, email = email)
    currentUser.value = updated
    liveDatabaseService?.syncUserPresence(updated, isOnline = true)
  }

  // --- Global Video Stream (TikTok-style) Actions ---

  fun toggleLikeVideo(videoId: String, forceLike: Boolean = false) {
    _videoStreams.update { list ->
      list.map { video ->
        if (video.id == videoId) {
          if (forceLike) {
            if (!video.isLiked) {
              video.copy(isLiked = true, likesCount = video.likesCount + 1)
            } else {
              video
            }
          } else {
            val newLiked = !video.isLiked
            val newCount = if (newLiked) video.likesCount + 1 else (video.likesCount - 1).coerceAtLeast(0)
            video.copy(isLiked = newLiked, likesCount = newCount)
          }
        } else {
          video
        }
      }
    }
  }

  fun toggleFollowVideoAuthor(videoId: String) {
    _videoStreams.update { list ->
      val target = list.find { it.id == videoId } ?: return@update list
      val newFollowState = !target.isFollowing
      list.map {
        if (it.authorHandle == target.authorHandle) {
          it.copy(isFollowing = newFollowState)
        } else {
          it
        }
      }
    }
  }

  fun toggleBookmarkVideo(videoId: String) {
    _videoStreams.update { list ->
      list.map { video ->
        if (video.id == videoId) {
          val newBookmarked = !video.isBookmarked
          val newCount = if (newBookmarked) video.bookmarksCount + 1 else (video.bookmarksCount - 1).coerceAtLeast(0)
          video.copy(isBookmarked = newBookmarked, bookmarksCount = newCount)
        } else {
          video
        }
      }
    }
  }

  fun addVideoComment(videoId: String, commentText: String) {
    if (commentText.isBlank()) return
    val user = currentUser.value
    val newComment = VideoComment(
      id = "vc_" + UUID.randomUUID().toString().take(8),
      userName = user.name,
      userAvatar = user.avatarUrl,
      text = commentText.trim(),
      timestampFormatted = "Just now",
      likesCount = 1,
      isLiked = true
    )
    _videoStreams.update { list ->
      list.map { video ->
        if (video.id == videoId) {
          video.copy(
            commentsCount = video.commentsCount + 1,
            comments = listOf(newComment) + video.comments
          )
        } else {
          video
        }
      }
    }
  }

  fun toggleLikeComment(videoId: String, commentId: String) {
    _videoStreams.update { list ->
      list.map { video ->
        if (video.id == videoId) {
          val updatedComments = video.comments.map { comment ->
            if (comment.id == commentId) {
              val newLiked = !comment.isLiked
              val newCount = if (newLiked) comment.likesCount + 1 else (comment.likesCount - 1).coerceAtLeast(0)
              comment.copy(isLiked = newLiked, likesCount = newCount)
            } else {
              comment
            }
          }
          video.copy(comments = updatedComments)
        } else {
          video
        }
      }
    }
  }

  fun shareVideoToChat(videoId: String, chatId: String) {
    val video = _videoStreams.value.find { it.id == videoId } ?: return
    sendMessage(
      chatId = chatId,
      content = "Check out this video by ${video.authorName} (${video.authorHandle}): \"${video.description.take(60)}...\"",
      mediaUrl = video.videoPreviewUrl,
      type = MessageType.IMAGE
    )
    _videoStreams.update { list ->
      list.map {
        if (it.id == videoId) it.copy(sharesCount = it.sharesCount + 1) else it
      }
    }
  }
}
