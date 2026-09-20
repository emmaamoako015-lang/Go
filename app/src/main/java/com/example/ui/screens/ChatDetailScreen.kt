package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import com.example.ai.AiEngine
import com.example.ai.AiResponse
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ai.EliteAiService
import com.example.data.ChatRepository
import com.example.data.FirestoreTypingService
import com.example.data.TypingUser
import com.example.model.Chat
import com.example.model.Message
import com.example.model.MessageType
import com.example.model.User
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.BouncingDotsIndicator
import com.example.ui.components.ChatHeaderPresence
import com.example.ui.components.E2EEBadge
import com.example.ui.components.GroupTypingIndicatorBubble
import com.example.ui.components.MessageStatusTick
import com.example.ui.components.MirrorIconButton
import com.example.ui.components.MirrorText
import com.example.ui.components.PresencePulseDot
import com.example.ui.components.SpeechToTextPanel
import com.example.ui.components.UserAvatar
import com.example.ui.components.rememberAnimatedRgbBrush
import com.example.ui.dialogs.SecurityE2EEDialog
import com.example.util.SpeechState
import com.example.util.SpeechToTextManager
import com.example.ui.theme.MirrorBorderGlint
import com.example.ui.theme.MirrorBorderSubtle
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen
import com.example.ui.theme.RgbNeonPink
import com.example.ui.theme.RgbNeonYellow
import com.example.ui.theme.WhatsAppChatBg
import com.example.ui.theme.WhatsAppIncomingBubble
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppOutgoingBubble
import com.example.ui.theme.WhatsAppTeal
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(
  ExperimentalMaterial3Api::class,
  androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
fun ChatDetailScreen(
  chat: Chat,
  repository: ChatRepository,
  onBack: () -> Unit
) {
  val messagesMap by repository.messages.collectAsState()
  val messages = messagesMap[chat.id].orEmpty().filterNot { it.isDeletedForMe }
  val currentUser = repository.currentUser.value

  // Attaches the real-time Firestore listener for this chat so messages the other
  // real person sends actually show up here, not just messages we send ourselves.
  LaunchedEffect(chat.id) {
    repository.observeRemoteMessages(chat.id)
  }

  var inputText by remember { mutableStateOf("") }
  var replyingToMessage by remember { mutableStateOf<Message?>(null) }
  var selectedMessageForMenu by remember { mutableStateOf<Message?>(null) }
  var showEditDialog by remember { mutableStateOf(false) }
  var showAttachmentSheet by remember { mutableStateOf(false) }
  var showE2EEDialog by remember { mutableStateOf(false) }
  var showEliteSheet by remember { mutableStateOf(false) }
  var isRecordingVoiceNote by remember { mutableStateOf(false) }
  var recordSeconds by remember { mutableIntStateOf(0) }
  var menuExpanded by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 3800)

  // Speech-to-Text State & Lifecycle
  var isSpeechToTextActive by remember { mutableStateOf(false) }
  val speechToTextManager = remember { SpeechToTextManager(context) }
  val speechState by speechToTextManager.speechState.collectAsState()
  val partialSpeechText by speechToTextManager.partialText.collectAsState()
  val rmsAudioLevel by speechToTextManager.rmsAudioLevel.collectAsState()
  val speechErrorMessage by speechToTextManager.errorMessage.collectAsState()

  DisposableEffect(speechToTextManager) {
    onDispose {
      speechToTextManager.destroy()
    }
  }

  val speechIntentFallbackLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.resultCode == android.app.Activity.RESULT_OK) {
      val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
      val spoken = matches?.firstOrNull()?.trim()
      if (!spoken.isNullOrEmpty()) {
        inputText = if (inputText.isBlank()) spoken else "$inputText $spoken"
      }
    }
    isSpeechToTextActive = false
  }

  lateinit var triggerAudioPermission: () -> Unit

  fun startVoiceTyping() {
    val hasPermission = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) {
      triggerAudioPermission()
      return
    }

    if (SpeechToTextManager.isSpeechRecognitionAvailable(context)) {
      isSpeechToTextActive = true
      speechToTextManager.startListening(
        onPartial = { _ ->
          // Real-time partial text updates live in the speech panel
        },
        onFinal = { finalResult ->
          val trimmed = finalResult.trim()
          if (trimmed.isNotEmpty()) {
            inputText = if (inputText.isBlank()) trimmed else "$inputText $trimmed"
          }
        }
      )
    } else {
      try {
        speechIntentFallbackLauncher.launch(SpeechToTextManager.buildSpeechRecognizerIntent())
      } catch (e: Exception) {
        Toast.makeText(context, "Speech recognition service unavailable on device", Toast.LENGTH_SHORT).show()
      }
    }
  }

  val audioPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    if (granted) {
      startVoiceTyping()
    } else {
      Toast.makeText(context, "Microphone permission required for voice typing", Toast.LENGTH_LONG).show()
    }
  }

  triggerAudioPermission = {
    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
  }

  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (granted) {
      repository.sendMessage(
        chatId = chat.id,
        content = "Live Location: 37.7749° N, 122.4194° W (San Francisco, CA)\nhttps://maps.google.com/?q=37.7749,-122.4194",
        type = MessageType.LOCATION
      )
    }
  }

  fun shareToApps(text: String, title: String = "Share via") {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(sendIntent, title)
    context.startActivity(chooser)
  }

  // Auto-scroll on new message
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
    repository.markChatAsRead(chat.id)
  }

  // Voice recording timer
  LaunchedEffect(isRecordingVoiceNote) {
    if (isRecordingVoiceNote) {
      recordSeconds = 0
      while (isRecordingVoiceNote) {
        delay(1000)
        recordSeconds++
      }
    }
  }

  // Real-time Firestore Group Typing Indicator Service & State
  val firestoreTypingService = remember { FirestoreTypingService(context) }
  val firestoreTypingUsers by firestoreTypingService.observeGroupTyping(chat.id, currentUser.id).collectAsState(initial = emptyList())
  val groupTypingMap by repository.groupTypingUsers.collectAsState()
  val localTypingUsers = groupTypingMap[chat.id].orEmpty()

  val activeGroupTypers = remember(firestoreTypingUsers, localTypingUsers, currentUser.id) {
    (firestoreTypingUsers + localTypingUsers)
      .filter { it.userId != currentUser.id && it.isTyping }
      .distinctBy { it.userId }
  }

  // Update repository's group chat status so Home screen list item mirrors typing state in real-time
  LaunchedEffect(activeGroupTypers) {
    if (chat.isGroup) {
      repository.updateGroupTypingUsers(chat.id, activeGroupTypers)
    }
  }

  // Broadcast current user's typing status to Firestore with 3s debounce
  LaunchedEffect(inputText) {
    if (chat.isGroup) {
      if (inputText.isNotBlank()) {
        firestoreTypingService.setTypingStatus(chat.id, currentUser, true)
        delay(3000)
        firestoreTypingService.setTypingStatus(chat.id, currentUser, false)
      } else {
        firestoreTypingService.setTypingStatus(chat.id, currentUser, false)
      }
    }
  }

  DisposableEffect(chat.id) {
    onDispose {
      if (chat.isGroup) {
        firestoreTypingService.setTypingStatus(chat.id, currentUser, false)
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Transparent)
      .testTag("chat_detail_screen")
  ) {
    // 1. Sleek Mirror Top Bar with RGB bottom border
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xE60D1526))
        .border(width = 1.dp, brush = rgbBrush, shape = androidx.compose.ui.graphics.RectangleShape)
        .drawWithContent {
          drawContent()
          drawRect(
            brush = Brush.verticalGradient(
              listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
            ),
            size = size.copy(height = size.height * 0.45f)
          )
        }
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 28.dp, bottom = 8.dp, start = 4.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onBack) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
          )
        }

        UserAvatar(
          avatarUrl = chat.avatarUrl,
          name = chat.name,
          size = 38.dp,
          onClick = { showE2EEDialog = true }
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
          modifier = Modifier
            .weight(1f)
            .clickable { showE2EEDialog = true }
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            MirrorText(
              text = if (chat.id == "chat_elite") "👑 Master Omni AI" else chat.name,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1
            )
            if (chat.id == "chat_elite") {
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color(0x35FFD700), RoundedCornerShape(4.dp))
                  .border(0.8.dp, Color(0xFFFFD700), RoundedCornerShape(4.dp))
                  .padding(horizontal = 4.dp, vertical = 1.dp)
              ) {
                Text("UNRESTRICTED", fontSize = 7.5.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD700))
              }
            }
          }

          val typingText = when {
            activeGroupTypers.isNotEmpty() -> firestoreTypingService.formatTypingStatus(activeGroupTypers)
            chat.typingStatus != null -> chat.typingStatus
            else -> null
          }

          val otherUser = chat.participants.firstOrNull { it.id != currentUser.id }
          val isOnline = otherUser?.isOnline == true
          val lastSeen = otherUser?.lastSeen ?: "offline"

          ChatHeaderPresence(
            isOnline = isOnline,
            lastSeenText = lastSeen,
            typingText = typingText,
            isGroup = chat.isGroup,
            participantCount = chat.participants.size,
            isElite = chat.id == "chat_elite"
          )
        }

        // Voice Call with mirror icon button
        MirrorIconButton(
          onClick = {
            val contact = chat.participants.firstOrNull { it.id != currentUser.id }
              ?: User("group", chat.name, "")
            repository.startCall(contact, isVideo = false)
          },
          icon = Icons.Default.Call,
          contentDescription = "Voice Call",
          tint = RgbNeonGreen,
          size = 36.dp
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Video Call with mirror icon button
        MirrorIconButton(
          onClick = {
            val contact = chat.participants.firstOrNull { it.id != currentUser.id }
              ?: User("group", chat.name, "")
            repository.startCall(contact, isVideo = true)
          },
          icon = Icons.Default.Videocam,
          contentDescription = "Video Call",
          tint = RgbNeonCyan,
          size = 36.dp
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Elite AI Co-Pilot button
        IconButton(
          onClick = { showEliteSheet = true },
          modifier = Modifier
            .size(36.dp)
            .background(
              Brush.sweepGradient(
                listOf(
                  Color(0xFF7928CA),
                  Color(0xFF00DFD8),
                  Color(0xFFFF0080),
                  Color(0xFF7928CA)
                )
              ),
              CircleShape
            )
            .border(1.2.dp, Color.White.copy(alpha = 0.85f), CircleShape)
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "Elite AI Assistant",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Overflow Menu
        Box {
          MirrorIconButton(
            onClick = { menuExpanded = true },
            icon = Icons.Default.MoreVert,
            contentDescription = "Menu",
            size = 36.dp
          )
          DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier
              .background(Color(0xF20B1424))
              .border(1.2.dp, rgbBrush, RoundedCornerShape(16.dp))
              .padding(vertical = 4.dp)
          ) {
            DropdownMenuItem(
              text = { MirrorText("Ask Elite AI", fontSize = 14.sp) },
              onClick = {
                menuExpanded = false
                showEliteSheet = true
              }
            )
            DropdownMenuItem(
              text = { MirrorText("Share Chat Link", fontSize = 14.sp) },
              onClick = {
                menuExpanded = false
                shareToApps("Chatting with ${chat.name} on Global Stream: https://globalstream.app/c/${chat.id}", "Share Chat")
              }
            )
            DropdownMenuItem(
              text = { MirrorText("Verify Encryption", fontSize = 14.sp) },
              onClick = {
                menuExpanded = false
                showE2EEDialog = true
              }
            )
            DropdownMenuItem(
              text = { Text("Mute Notifications") },
              onClick = { menuExpanded = false }
            )
            DropdownMenuItem(
              text = { Text("Clear Chat") },
              onClick = {
                menuExpanded = false
                messages.forEach { repository.deleteMessageForMe(chat.id, it.id) }
              }
            )
            if (chat.isGroup) {
              DropdownMenuItem(
                text = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    BouncingDotsIndicator(dotSize = 4.dp, dotColor = RgbNeonGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    MirrorText("Simulate Alex Typing (Firestore)", fontSize = 14.sp)
                  }
                },
                onClick = {
                  menuExpanded = false
                  repository.simulateMemberTyping(chat.id, "Alex Chen", 4000L)
                }
              )
            }
          }
        }
      }
    }

    // 2. Chat Message List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 8.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(10.dp))
        E2EEBadge(onClick = { showE2EEDialog = true })
        Spacer(modifier = Modifier.height(6.dp))
      }

      items(messages, key = { it.id }) { message ->
        val isMe = message.senderId == currentUser.id
        MessageBubbleItem(
          message = message,
          isMe = isMe,
          isGroup = chat.isGroup,
          onLongClick = { selectedMessageForMenu = message },
          onReaction = { emoji -> repository.toggleReaction(chat.id, message.id, emoji) }
        )
        Spacer(modifier = Modifier.height(4.dp))
      }

      item {
        Spacer(modifier = Modifier.height(8.dp))
      }
    }

    // 3. Reply Quote Bar (if active)
    replyingToMessage?.let { replyTarget ->
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
          .background(Color(0xEE121D30), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
          .border(1.dp, MirrorBorderSubtle, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
          .padding(horizontal = 14.dp, vertical = 8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .width(4.dp)
              .height(36.dp)
              .background(rgbBrush, RoundedCornerShape(2.dp))
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = replyTarget.senderName,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = RgbNeonCyan
            )
            Text(
              text = replyTarget.content,
              fontSize = 12.sp,
              color = Color(0xFF94A3B8),
              maxLines = 1
            )
          }
          IconButton(onClick = { replyingToMessage = null }) {
            Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = Color(0xFF94A3B8))
          }
        }
      }
    }

    // 3.5 Quick Master Creation Chips (if chatting with Master AI)
    if (chat.id == "chat_elite") {
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        val masterSuggestions = listOf(
          "🛠️ Create Android App" to "Create a complete, modern Android application architecture with Jetpack Compose, Room database, and clean MVVM design.",
          "💻 Write Kotlin Code" to "Write a production-ready Kotlin coroutine manager with thread-safe Mutex and exponential backoff retry.",
          "🧠 Solve Logic & Deduce" to "Analyze and provide a deductive solution to the Byzantine Generals Problem in distributed computing.",
          "✍️ Write an Epic Story" to "Write an evocative, suspenseful sci-fi story about an AI discovering a quantum anomaly in deep space.",
          "📊 System Architecture" to "Design a high-scale microservices and real-time event streaming architecture with low latency.",
          "🌐 Polyglot Translation" to "Translate 'Welcome to our platform. Everything you need is ready at your command' into French, Spanish, German, Japanese, and Mandarin."
        )

        items(masterSuggestions) { (label, prompt) ->
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0x351F293D), RoundedCornerShape(14.dp))
              .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
              .clickable {
                inputText = prompt
              }
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = label,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFFFFD700)
            )
          }
        }
      }
    }

    // 3.8 Active Speech-To-Text Dictation Panel
    AnimatedVisibility(
      visible = isSpeechToTextActive,
      enter = fadeIn() + slideInVertically { it / 2 },
      exit = fadeOut() + slideOutVertically { it / 2 }
    ) {
      SpeechToTextPanel(
        speechState = speechState,
        partialText = partialSpeechText,
        composedText = inputText,
        rmsAudioLevel = rmsAudioLevel,
        errorMessage = speechErrorMessage,
        onStopAndAccept = {
          val textToAppend = partialSpeechText.trim()
          if (textToAppend.isNotEmpty()) {
            inputText = if (inputText.isBlank()) textToAppend else "$inputText $textToAppend"
          }
          speechToTextManager.cancel()
          isSpeechToTextActive = false
        },
        onSendNow = {
          val textToAppend = partialSpeechText.trim()
          val fullMessage = if (textToAppend.isNotEmpty()) {
            if (inputText.isBlank()) textToAppend else "$inputText $textToAppend"
          } else inputText.trim()

          if (fullMessage.isNotBlank()) {
            repository.sendMessage(
              chatId = chat.id,
              content = fullMessage,
              type = MessageType.TEXT,
              replyToText = replyingToMessage?.content,
              replyToSender = replyingToMessage?.senderName
            )
            inputText = ""
            replyingToMessage = null
            if (chat.isGroup) {
              firestoreTypingService.setTypingStatus(chat.id, currentUser, false)
            }
          }
          speechToTextManager.cancel()
          isSpeechToTextActive = false
        },
        onRetry = {
          speechToTextManager.cancel()
          startVoiceTyping()
        },
        onCancel = {
          speechToTextManager.cancel()
          isSpeechToTextActive = false
        },
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
      )
    }

    // 3.9 Real-time Firestore Group Typing Indicator Bubble
    if (chat.isGroup) {
      GroupTypingIndicatorBubble(
        typingUsers = activeGroupTypers,
        modifier = Modifier.fillMaxWidth()
      )
    }

    // 4. Message Input Bar & Voice Note Controller with Mirror Finish
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color.Transparent)
        .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
      if (isRecordingVoiceNote) {
        // Active Voice Recording UI with dynamic RGB styling
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .clip(RoundedCornerShape(24.dp))
              .background(Color(0xDD0E1A2C), RoundedCornerShape(24.dp))
              .border(1.dp, rgbBrush, RoundedCornerShape(24.dp))
              .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .background(RgbNeonPink, CircleShape)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = String.format("%02d:%02d", recordSeconds / 60, recordSeconds % 60),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = RgbNeonPink
              )
              Spacer(modifier = Modifier.width(16.dp))
              AudioWaveformVisualizer(isPlaying = true, progress = 0.7f, modifier = Modifier.weight(1f))
              IconButton(onClick = { isRecordingVoiceNote = false }) {
                Icon(Icons.Default.Delete, contentDescription = "Cancel Recording", tint = Color(0xFF94A3B8))
              }
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(
            onClick = {
              val recordedDur = recordSeconds.coerceAtLeast(1)
              repository.sendMessage(
                chatId = chat.id,
                content = "Voice note ($recordedDur sec)",
                type = MessageType.AUDIO,
                mediaDurationSeconds = recordedDur,
                mediaSizeFormatted = "${recordedDur * 12} KB"
              )
              isRecordingVoiceNote = false
            },
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(Color(0xCC0E1A2C), CircleShape)
              .border(2.dp, rgbBrush, CircleShape)
              .testTag("send_voice_note_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send Voice Note",
              tint = RgbNeonGreen
            )
          }
        }
      } else {
        // Standard Input Field with Sleek Mirror Glass Pill
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(24.dp))
              .background(Color(0xDD0E1A2C), RoundedCornerShape(24.dp))
              .border(1.dp, if (isSpeechToTextActive) RgbNeonGreen else MirrorBorderSubtle, RoundedCornerShape(24.dp))
              .drawWithContent {
                drawContent()
                drawRect(
                  brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.07f), Color.Transparent)
                  ),
                  size = size.copy(height = size.height * 0.45f)
                )
              }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                  Text(
                    text = if (isSpeechToTextActive) "Listening to your voice..."
                           else if (chat.id == "chat_elite") "Command Master AI what you want..."
                           else "Message...",
                    color = if (isSpeechToTextActive) RgbNeonGreen else Color(0xFF64748B),
                    fontSize = 14.sp
                  )
                },
                singleLine = false,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White,
                  focusedBorderColor = Color.Transparent,
                  unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                  .weight(1f)
                  .testTag("message_input_field")
              )

              // Dedicated Speech-to-Text Voice Dictation button inside input bar
              IconButton(
                onClick = {
                  if (isSpeechToTextActive) {
                    val textToAppend = partialSpeechText.trim()
                    if (textToAppend.isNotEmpty()) {
                      inputText = if (inputText.isBlank()) textToAppend else "$inputText $textToAppend"
                    }
                    speechToTextManager.cancel()
                    isSpeechToTextActive = false
                  } else {
                    startVoiceTyping()
                  }
                },
                modifier = Modifier.testTag("speech_to_text_input_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Mic,
                  contentDescription = "Compose via Voice (Speech-to-Text)",
                  tint = if (isSpeechToTextActive) RgbNeonGreen else RgbNeonCyan
                )
              }

              IconButton(onClick = { showAttachmentSheet = true }) {
                Icon(
                  imageVector = Icons.Default.AttachFile,
                  contentDescription = "Attach media",
                  tint = Color(0xFF94A3B8)
                )
              }

              IconButton(
                onClick = {
                  repository.sendMessage(
                    chatId = chat.id,
                    content = "Photo from camera",
                    type = MessageType.IMAGE,
                    mediaUrl = "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=500"
                  )
                }
              ) {
                Icon(
                  imageVector = Icons.Default.CameraAlt,
                  contentDescription = "Camera",
                  tint = Color(0xFF94A3B8)
                )
              }
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Voice Dictation (Speech-to-Text) on tap OR send text button with mirror glass specular finish
          if (inputText.isBlank()) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xCC0E1A2C), CircleShape)
                .border(1.5.dp, if (isSpeechToTextActive) RgbNeonGreen else MirrorBorderSubtle, CircleShape)
                .combinedClickable(
                  onClick = {
                    if (isSpeechToTextActive) {
                      val textToAppend = partialSpeechText.trim()
                      if (textToAppend.isNotEmpty()) {
                        inputText = textToAppend
                      }
                      speechToTextManager.cancel()
                      isSpeechToTextActive = false
                    } else {
                      startVoiceTyping()
                    }
                  },
                  onLongClick = {
                    isRecordingVoiceNote = true
                  }
                )
                .drawWithContent {
                  drawContent()
                  drawRect(
                    brush = Brush.verticalGradient(
                      listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)
                    ),
                    size = size.copy(height = size.height * 0.45f)
                  )
                }
                .testTag("speech_to_text_main_button"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Tap to speak and compose message, Long press for voice note",
                tint = if (isSpeechToTextActive) RgbNeonGreen else RgbNeonCyan
              )
            }
          } else {
            IconButton(
              onClick = {
                val textToSend = inputText.trim()
                if (textToSend.isNotBlank()) {
                  repository.sendMessage(
                    chatId = chat.id,
                    content = textToSend,
                    type = MessageType.TEXT,
                    replyToText = replyingToMessage?.content,
                    replyToSender = replyingToMessage?.senderName
                  )
                  inputText = ""
                  replyingToMessage = null
                  if (chat.isGroup) {
                    firestoreTypingService.setTypingStatus(chat.id, currentUser, false)
                  }
                }
              },
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xCC0E1A2C), CircleShape)
                .border(2.dp, rgbBrush, CircleShape)
                .drawWithContent {
                  drawContent()
                  drawRect(
                    brush = Brush.verticalGradient(
                      listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)
                    ),
                    size = size.copy(height = size.height * 0.45f)
                  )
                }
                .testTag("send_message_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send Message",
                tint = RgbNeonGreen
              )
            }
          }
        }
      }
    }
  }

  // Attachment Picker Sheet
  if (showAttachmentSheet) {
    AttachmentPickerSheet(
      onDismiss = { showAttachmentSheet = false },
      onOptionSelected = { option ->
        showAttachmentSheet = false
        when (option) {
          "Document" -> {
            repository.sendMessage(
              chatId = chat.id,
              content = "Project_Architecture_Firebase.pdf",
              type = MessageType.DOCUMENT,
              fileName = "Project_Architecture_Firebase.pdf",
              mediaSizeFormatted = "1.4 MB"
            )
          }
          "Camera" -> {
            repository.sendMessage(
              chatId = chat.id,
              content = "Snapped photo",
              type = MessageType.IMAGE,
              mediaUrl = "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=500"
            )
          }
          "Gallery" -> {
            repository.sendMessage(
              chatId = chat.id,
              content = "UI Preview Mockup",
              type = MessageType.IMAGE,
              mediaUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500"
            )
          }
          "Audio" -> {
            repository.sendMessage(
              chatId = chat.id,
              content = "Meeting recording.m4a",
              type = MessageType.AUDIO,
              mediaDurationSeconds = 45,
              mediaSizeFormatted = "680 KB"
            )
          }
          "Location" -> {
            locationPermissionLauncher.launch(
              arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
              )
            )
          }
          "Share to..." -> {
            shareToApps(
              "Chatting with ${chat.name} on Global Stream!\nhttps://globalstream.app/c/${chat.id}",
              "Share Chat Link via"
            )
          }
          "Elite AI" -> {
            showEliteSheet = true
          }
          "Poll" -> {
            repository.sendMessage(
              chatId = chat.id,
              content = "Poll: When should we ship the WebRTC call release? (1. Tomorrow, 2. Next week)",
              type = MessageType.TEXT
            )
          }
        }
      }
    )
  }

  // Elite AI Assistant Dialog
  if (showEliteSheet) {
    EliteAssistantDialog(
      chatName = chat.name,
      recentMessages = messages.takeLast(10),
      currentDraft = inputText,
      onDismiss = { showEliteSheet = false },
      onUseDraft = { updatedText ->
        inputText = updatedText
        showEliteSheet = false
      },
      onSendDirectly = { directText ->
        repository.sendMessage(
          chatId = chat.id,
          content = directText,
          type = MessageType.TEXT
        )
        showEliteSheet = false
      },
      onShareText = { shareText ->
        shareToApps(shareText, "Share AI Summary via")
      }
    )
  }

  // Long-press Context Menu Dialog
  selectedMessageForMenu?.let { targetMessage ->
    MessageActionDialog(
      message = targetMessage,
      isMe = targetMessage.senderId == currentUser.id,
      onDismiss = { selectedMessageForMenu = null },
      onReply = {
        replyingToMessage = targetMessage
        selectedMessageForMenu = null
      },
      onEdit = {
        selectedMessageForMenu = null
        showEditDialog = true
      },
      onDeleteForEveryone = {
        repository.deleteMessageForEveryone(chat.id, targetMessage.id)
        selectedMessageForMenu = null
      },
      onDeleteForMe = {
        repository.deleteMessageForMe(chat.id, targetMessage.id)
        selectedMessageForMenu = null
      },
      onReaction = { emoji ->
        repository.toggleReaction(chat.id, targetMessage.id, emoji)
        selectedMessageForMenu = null
      },
      onShareToApps = {
        shareToApps(targetMessage.content, "Share Message via")
        selectedMessageForMenu = null
      },
      onAskElite = {
        selectedMessageForMenu = null
        showEliteSheet = true
      }
    )
  }

  // Edit Message Dialog
  if (showEditDialog) {
    selectedMessageForMenu?.let { msg ->
      EditMessageDialog(
        currentText = msg.content,
        onDismiss = { showEditDialog = false },
        onSave = { newContent ->
          repository.editMessage(chat.id, msg.id, newContent)
          showEditDialog = false
        }
      )
    }
  }

  // E2EE Verification Dialog
  if (showE2EEDialog) {
    SecurityE2EEDialog(
      contactName = chat.name,
      onDismiss = { showE2EEDialog = false }
    )
  }
}

@OptIn(ExperimentalLayoutApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MessageBubbleItem(
  message: Message,
  isMe: Boolean,
  isGroup: Boolean,
  onLongClick: () -> Unit,
  onReaction: (String) -> Unit
) {
  var isAudioPlaying by remember { mutableStateOf(false) }
  var audioPlaybackSpeed by remember { mutableFloatStateOf(1.0f) }

  val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart

  val bubbleBackground = if (isMe) {
    Color(0xDC0C2A3D)
  } else {
    Color(0xD8121E31)
  }

  val bubbleBorderBrush = if (isMe) {
    Brush.linearGradient(
      listOf(
        RgbNeonCyan.copy(alpha = 0.55f),
        MirrorBorderGlint.copy(alpha = 0.3f),
        Color(0x1000FFCC)
      )
    )
  } else {
    Brush.linearGradient(
      listOf(
        MirrorBorderGlint.copy(alpha = 0.4f),
        MirrorBorderSubtle,
        Color(0x05FFFFFF)
      )
    )
  }

  val bubbleShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = if (isMe) 16.dp else 2.dp,
    bottomEnd = if (isMe) 2.dp else 16.dp
  )

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 4.dp),
    contentAlignment = alignment
  ) {
    Box(
      modifier = Modifier
        .widthIn(max = 310.dp)
        .clip(bubbleShape)
        .combinedClickable(onClick = {}, onLongClick = onLongClick)
        .background(bubbleBackground, bubbleShape)
        .border(1.dp, bubbleBorderBrush, bubbleShape)
        .drawWithContent {
          drawContent()
          drawRect(
            brush = Brush.verticalGradient(
              listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
            ),
            size = size.copy(height = size.height * 0.45f)
          )
        }
        .padding(9.dp)
    ) {
      Column {
        // Group Chat Sender Name
        if (!isMe && isGroup) {
          Text(
            text = message.senderName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = RgbNeonCyan,
            modifier = Modifier.padding(bottom = 2.dp)
          )
        }

        // Quoted Reply Preview
        if (message.replyToText != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0x350A1422), RoundedCornerShape(8.dp))
              .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(8.dp))
              .padding(bottom = 4.dp)
          ) {
            Row(modifier = Modifier.padding(6.dp)) {
              Box(
                modifier = Modifier
                  .width(3.dp)
                  .height(26.dp)
                  .background(RgbNeonGreen, RoundedCornerShape(2.dp))
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                  text = message.replyToSender ?: "Reply",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = RgbNeonGreen
                )
                Text(
                  text = message.replyToText,
                  fontSize = 11.sp,
                  color = Color(0xFF94A3B8),
                  maxLines = 1
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
        }

        // Message Content based on Type
        if (message.isDeletedForEveryone) {
          Text(
            text = message.content,
            fontStyle = FontStyle.Italic,
            color = Color(0xFF64748B),
            fontSize = 14.sp
          )
        } else {
          when (message.type) {
            MessageType.TEXT, MessageType.SYSTEM -> {
              Text(
                text = message.content,
                fontSize = 14.sp,
                color = Color.White
              )
            }
            MessageType.IMAGE -> {
              AsyncImage(
                model = message.mediaUrl,
                contentDescription = "Image attachment",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(180.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .border(1.dp, Color(0x25FFFFFF), RoundedCornerShape(10.dp))
              )
              if (message.content.isNotBlank() && message.content != "Snapped photo") {
                Spacer(modifier = Modifier.height(5.dp))
                Text(text = message.content, fontSize = 14.sp, color = Color.White)
              }
            }
            MessageType.AUDIO -> {
              // Voice note player with neon glass styling
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC0E1A2C), CircleShape)
                    .border(1.dp, RgbNeonCyan, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  IconButton(
                    onClick = { isAudioPlaying = !isAudioPlaying },
                    modifier = Modifier.size(36.dp)
                  ) {
                    Icon(
                      imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                      contentDescription = "Play/Pause audio",
                      tint = RgbNeonCyan,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.width(8.dp))
                AudioWaveformVisualizer(
                  isPlaying = isAudioPlaying,
                  progress = if (isAudioPlaying) 0.65f else 0.2f,
                  modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x400E1A2C), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(8.dp))
                    .clickable {
                      audioPlaybackSpeed = when (audioPlaybackSpeed) {
                        1.0f -> 1.5f
                        1.5f -> 2.0f
                        else -> 1.0f
                      }
                    }
                ) {
                  Text(
                    text = "${audioPlaybackSpeed}x",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
            MessageType.DOCUMENT -> {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0x350A1422), RoundedCornerShape(8.dp))
                  .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(8.dp))
                  .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFEA4335), RoundedCornerShape(6.dp)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = "PDF Document",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = message.fileName.ifBlank { message.content },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1
                  )
                  Text(
                    text = message.mediaSizeFormatted.ifBlank { "1.2 MB" } + " • PDF",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                  )
                }
              }
            }
            MessageType.VIDEO -> {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(180.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color.Black)
                  .border(1.dp, Color(0x25FFFFFF), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = "Play Video",
                  tint = RgbNeonCyan,
                  modifier = Modifier.size(48.dp)
                )
              }
            }
            MessageType.LOCATION -> {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0x350A1422))
                  .border(1.dp, RgbNeonGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                  .padding(10.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(34.dp)
                      .background(RgbNeonGreen.copy(alpha = 0.2f), CircleShape)
                      .border(1.dp, RgbNeonGreen, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.LocationOn,
                      contentDescription = "Location",
                      tint = RgbNeonGreen,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = "Live Encrypted Location",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = RgbNeonGreen
                    )
                    Text(
                      text = "Accurate within 5 meters",
                      fontSize = 11.sp,
                      color = Color(0xFF94A3B8)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = message.content,
                  fontSize = 13.sp,
                  color = Color.White
                )
              }
            }
          }
        }

        // Timestamp & Status Ticks
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (message.isEdited) {
            Text(
              text = "(edited)",
              fontSize = 10.sp,
              color = Color(0xFF64748B),
              modifier = Modifier.padding(end = 4.dp)
            )
          }

          Text(
            text = message.formattedTime,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8)
          )

          if (isMe) {
            Spacer(modifier = Modifier.width(4.dp))
            MessageStatusTick(status = message.status)
          }
        }

        // Reactions in Frosted Glass Pills
        if (message.reactions.isNotEmpty()) {
          Spacer(modifier = Modifier.height(4.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(top = 2.dp)
          ) {
            message.reactions.forEach { (emoji, users) ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0x5015243B), RoundedCornerShape(10.dp))
                  .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(10.dp))
                  .clickable { onReaction(emoji) }
              ) {
                Text(
                  text = "$emoji ${users.size}",
                  fontSize = 11.sp,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun AttachmentPickerSheet(
  onDismiss: () -> Unit,
  onOptionSelected: (String) -> Unit
) {
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 3500)

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(Color(0xF20D1526), RoundedCornerShape(20.dp))
        .border(1.5.dp, rgbBrush, RoundedCornerShape(20.dp))
        .drawWithContent {
          drawContent()
          drawRect(
            brush = Brush.verticalGradient(
              listOf(Color.White.copy(alpha = 0.09f), Color.Transparent)
            ),
            size = size.copy(height = size.height * 0.4f)
          )
        }
        .padding(20.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        MirrorText(
          text = "SHARE CONTENT",
          fontSize = 14.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(18.dp))

        val options = listOf(
          Triple("Document", Icons.Default.Description, Color(0xFF7F66FF)),
          Triple("Camera", Icons.Default.CameraAlt, RgbNeonPink),
          Triple("Gallery", Icons.Default.AttachFile, Color(0xFFAC44CF)),
          Triple("Audio", Icons.Default.Headphones, Color(0xFFE56338)),
          Triple("Location", Icons.Default.LocationOn, RgbNeonGreen),
          Triple("Share to...", Icons.Default.Share, Color(0xFF38BDF8)),
          Triple("Elite AI", Icons.Default.AutoAwesome, Color(0xFF00DFD8)),
          Triple("Poll", Icons.Default.Poll, RgbNeonCyan)
        )

        Column {
          options.chunked(4).forEach { rowOptions ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly
            ) {
              rowOptions.forEach { (title, icon, color) ->
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier
                    .clickable { onOptionSelected(title) }
                    .padding(6.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(48.dp)
                      .clip(CircleShape)
                      .background(color.copy(alpha = 0.2f), CircleShape)
                      .border(1.5.dp, color, CircleShape)
                      .drawWithContent {
                        drawContent()
                        drawRect(
                          brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)
                          ),
                          size = size.copy(height = size.height * 0.45f)
                        )
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = icon,
                      contentDescription = title,
                      tint = color,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(text = title, fontSize = 11.sp, color = Color.White)
                }
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
          }
        }
      }
    }
  }
}

@Composable
fun MessageActionDialog(
  message: Message,
  isMe: Boolean,
  onDismiss: () -> Unit,
  onReply: () -> Unit,
  onEdit: () -> Unit,
  onDeleteForEveryone: () -> Unit,
  onDeleteForMe: () -> Unit,
  onReaction: (String) -> Unit,
  onShareToApps: () -> Unit,
  onAskElite: () -> Unit
) {
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 3500)

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(Color(0xF20D1526), RoundedCornerShape(20.dp))
        .border(1.dp, rgbBrush, RoundedCornerShape(20.dp))
        .drawWithContent {
          drawContent()
          drawRect(
            brush = Brush.verticalGradient(
              listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
            ),
            size = size.copy(height = size.height * 0.4f)
          )
        }
        .padding(18.dp)
    ) {
      Column {
        // Quick reactions row in dark glass capsules
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          listOf("👍", "❤️", "😂", "😮", "😢", "🔥").forEach { emoji ->
            Text(
              text = emoji,
              fontSize = 26.sp,
              modifier = Modifier
                .clickable { onReaction(emoji) }
                .padding(4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onReply)
            .padding(vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, tint = RgbNeonCyan)
          Spacer(modifier = Modifier.width(12.dp))
          MirrorText("Reply", fontSize = 15.sp)
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onShareToApps)
            .padding(vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF38BDF8))
          Spacer(modifier = Modifier.width(12.dp))
          MirrorText("Share to other apps", fontSize = 15.sp)
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAskElite)
            .padding(vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RgbNeonGreen)
          Spacer(modifier = Modifier.width(12.dp))
          MirrorText("Ask Elite AI about this", fontSize = 15.sp)
        }

        if (isMe && !message.isDeletedForEveryone && message.type == MessageType.TEXT) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable(onClick = onEdit)
              .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = RgbNeonGreen)
            Spacer(modifier = Modifier.width(12.dp))
            MirrorText("Edit Message", fontSize = 15.sp)
          }
        }

        if (isMe && !message.isDeletedForEveryone) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable(onClick = onDeleteForEveryone)
              .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = RgbNeonPink)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Delete for everyone", fontSize = 15.sp, color = RgbNeonPink)
          }
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDeleteForMe)
            .padding(vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFF94A3B8))
          Spacer(modifier = Modifier.width(12.dp))
          Text("Delete for me", fontSize = 15.sp, color = Color.White)
        }
      }
    }
  }
}

@Composable
fun EliteAssistantDialog(
  chatName: String,
  recentMessages: List<Message>,
  currentDraft: String,
  onDismiss: () -> Unit,
  onUseDraft: (String) -> Unit,
  onSendDirectly: (String) -> Unit,
  onShareText: (String) -> Unit
) {
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 3500)
  val coroutineScope = rememberCoroutineScope()
  var isAiLoading by remember { mutableStateOf(false) }
  var aiResponse by remember { mutableStateOf<String?>(null) }
  var aiThinking by remember { mutableStateOf<String?>(null) }
  var selectedEngine by remember { mutableStateOf(AiEngine.MASTER) }
  var customPrompt by remember { mutableStateOf("") }
  var activeTab by remember { mutableIntStateOf(0) }

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(22.dp))
        .background(Color(0xF50D1526), RoundedCornerShape(22.dp))
        .border(1.5.dp, rgbBrush, RoundedCornerShape(22.dp))
        .padding(18.dp)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .background(
                  Brush.sweepGradient(
                    listOf(
                      Color(0xFFFFD700),
                      Color(0xFF7928CA),
                      Color(0xFF00DFD8),
                      Color(0xFFFF0080),
                      Color(0xFFFFD700)
                    )
                  ),
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Elite AI",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              MirrorText("Omni Master AI", fontSize = 15.sp, fontWeight = FontWeight.Bold)
              Text("Full Creation Access • Master Protocol Active", fontSize = 11.sp, color = Color(0xFFFFD700))
            }
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Engine Selector
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          AiEngine.values().forEach { engine ->
            val isSelected = selectedEngine == engine
            val engineColor = when (engine) {
              AiEngine.MASTER -> Color(0xFFFFD700)
              AiEngine.GEMINI -> RgbNeonCyan
              AiEngine.CLAUDE -> Color(0xFFF97316)
              AiEngine.HYBRID -> Color(0xFFD946EF)
            }
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) engineColor.copy(alpha = 0.25f) else Color(0x2015243B), RoundedCornerShape(8.dp))
                .border(1.dp, if (isSelected) engineColor else MirrorBorderSubtle, RoundedCornerShape(8.dp))
                .clickable { selectedEngine = engine }
                .padding(vertical = 5.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = engine.badge,
                fontSize = 9.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFF94A3B8)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab selection chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          val tabs = listOf("Quick Reply", "Summarize", "Polish", "Ask")
          tabs.forEachIndexed { index, title ->
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(
                  if (activeTab == index) Color(0x3500DFD8) else Color(0x2015243B),
                  RoundedCornerShape(8.dp)
                )
                .border(
                  1.dp,
                  if (activeTab == index) RgbNeonCyan else MirrorBorderSubtle,
                  RoundedCornerShape(8.dp)
                )
                .clickable {
                  activeTab = index
                  aiResponse = null
                }
                .padding(vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                color = if (activeTab == index) RgbNeonCyan else Color(0xFF94A3B8)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (activeTab) {
          0 -> { // Quick Reply
            Text(
              "Instant AI smart suggestions for $chatName:",
              fontSize = 12.sp,
              color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val smartReplies = listOf(
              "Sounds good, let's proceed! 👍",
              "Let me review this and get back to you shortly.",
              "Received! I will send the files over."
            )
            smartReplies.forEach { reply ->
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0x3015243B), RoundedCornerShape(10.dp))
                  .border(1.dp, MirrorBorderSubtle, RoundedCornerShape(10.dp))
                  .clickable { onUseDraft(reply) }
                  .padding(10.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(reply, fontSize = 12.sp, color = Color.White, modifier = Modifier.weight(1f))
                  Spacer(modifier = Modifier.width(8.dp))
                  Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = RgbNeonCyan,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(6.dp))
            }
          }
          1 -> { // Summarize
            if (aiResponse == null && !isAiLoading) {
              Text(
                "Analyzes recent messages in this conversation and generates a structured summary.",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0x3000DFD8), RoundedCornerShape(12.dp))
                  .border(1.2.dp, RgbNeonCyan, RoundedCornerShape(12.dp))
                  .clickable {
                    isAiLoading = true
                    coroutineScope.launch {
                      val summary = EliteAiService.summarizeConversation(recentMessages)
                      aiResponse = summary
                      isAiLoading = false
                    }
                  }
                  .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
              ) {
                MirrorText("Summarize with AI", fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
          2 -> { // Polish
            if (aiResponse == null && !isAiLoading) {
              Text(
                if (currentDraft.isNotBlank()) "Draft: \"$currentDraft\""
                else "Type a message in the chat box or generate a professional response:",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0x30AC44CF), RoundedCornerShape(12.dp))
                  .border(1.2.dp, Color(0xFFAC44CF), RoundedCornerShape(12.dp))
                  .clickable {
                    val prompt = if (currentDraft.isNotBlank()) {
                      "Rewrite this draft professionally for a messaging app: $currentDraft"
                    } else {
                      "Generate a concise, polite status update message for a team conversation."
                    }
                    isAiLoading = true
                    coroutineScope.launch {
                      val response = EliteAiService.askEliteDetailed(prompt, engine = selectedEngine)
                      aiResponse = response.answer
                      aiThinking = response.thinkingChain
                      isAiLoading = false
                    }
                  }
                  .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
              ) {
                MirrorText("Enhance & Polish Draft", fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
          3 -> { // Ask Custom
            if (aiResponse == null && !isAiLoading) {
              OutlinedTextField(
                value = customPrompt,
                onValueChange = { customPrompt = it },
                placeholder = { Text("Command Master AI (create app, build logic, write story, code)...", color = Color(0xFF64748B), fontSize = 12.sp) },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White,
                  focusedBorderColor = RgbNeonCyan,
                  unfocusedBorderColor = MirrorBorderSubtle
                ),
                modifier = Modifier.fillMaxWidth()
              )
              Spacer(modifier = Modifier.height(8.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0x3000DFD8), RoundedCornerShape(12.dp))
                  .border(1.2.dp, RgbNeonCyan, RoundedCornerShape(12.dp))
                  .clickable {
                    if (customPrompt.isNotBlank()) {
                      val p = customPrompt
                      customPrompt = ""
                      isAiLoading = true
                      coroutineScope.launch {
                        val resp = EliteAiService.askEliteDetailed(p, engine = selectedEngine)
                        aiResponse = resp.answer
                        aiThinking = resp.thinkingChain
                        isAiLoading = false
                      }
                    }
                  }
                  .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
              ) {
                MirrorText("Ask AI (${selectedEngine.badge})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        // Loading indicator
        if (isAiLoading) {
          Spacer(modifier = Modifier.height(14.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("AI is generating with ${selectedEngine.displayName}...", fontSize = 12.sp, color = RgbNeonCyan)
          }
        }

        // AI Response display card
        aiResponse?.let { resp ->
          Spacer(modifier = Modifier.height(10.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0x4015243B), RoundedCornerShape(12.dp))
              .border(1.dp, RgbNeonCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
              .padding(12.dp)
          ) {
            Column {
              Text(
                text = resp,
                fontSize = 12.sp,
                color = Color.White,
                lineHeight = 17.sp
              )
              Spacer(modifier = Modifier.height(10.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
              ) {
                IconButton(onClick = { onShareText(resp) }, modifier = Modifier.size(32.dp)) {
                  Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x3000DFD8), RoundedCornerShape(8.dp))
                    .border(1.dp, RgbNeonCyan, RoundedCornerShape(8.dp))
                    .clickable { onUseDraft(resp) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Text("Use in Chat", fontSize = 11.sp, color = RgbNeonCyan, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun EditMessageDialog(
  currentText: String,
  onDismiss: () -> Unit,
  onSave: (String) -> Unit
) {
  var text by remember { mutableStateOf(currentText) }
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 3500)

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(Color(0xF20D1526), RoundedCornerShape(20.dp))
        .border(1.dp, rgbBrush, RoundedCornerShape(20.dp))
        .drawWithContent {
          drawContent()
          drawRect(
            brush = Brush.verticalGradient(
              listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
            ),
            size = size.copy(height = size.height * 0.4f)
          )
        }
        .padding(20.dp)
    ) {
      Column {
        MirrorText(
          text = "EDIT MESSAGE",
          fontSize = 14.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = text,
          onValueChange = { text = it },
          singleLine = false,
          maxLines = 4,
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = RgbNeonCyan,
            unfocusedBorderColor = MirrorBorderSubtle
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onDismiss) {
            Text("Cancel", color = Color(0xFF94A3B8), fontSize = 14.sp)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0x2200F0FF), RoundedCornerShape(14.dp))
              .border(1.2.dp, RgbNeonCyan, RoundedCornerShape(14.dp))
              .clickable {
                if (text.isNotBlank()) onSave(text.trim())
              }
              .drawWithContent {
                drawContent()
                drawRect(
                  brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                  ),
                  size = size.copy(height = size.height * 0.5f)
                )
              }
              .padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            MirrorText("Save", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
