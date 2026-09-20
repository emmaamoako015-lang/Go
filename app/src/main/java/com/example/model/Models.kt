package com.example.model

enum class MessageType {
  TEXT,
  IMAGE,
  VIDEO,
  AUDIO,
  DOCUMENT,
  LOCATION,
  SYSTEM
}

enum class MessageStatus {
  SENDING,
  SENT,
  DELIVERED,
  READ
}

enum class CallType {
  VOICE,
  VIDEO
}

enum class CallStatus {
  INCOMING,
  OUTGOING,
  CONNECTED,
  MISSED,
  ENDED
}

enum class BackupFrequency {
  DAILY,
  WEEKLY,
  MONTHLY,
  OFF
}

data class User(
  val id: String,
  val name: String,
  val phoneNumber: String,
  val email: String = "",
  val avatarUrl: String = "",
  val about: String = "Hey there! I am using Global Stream.",
  val isOnline: Boolean = true,
  val lastSeen: String = "online",
  val e2eeKeyFingerprint: String = "8492-9381-0192-3847"
)

data class Message(
  val id: String,
  val chatId: String,
  val senderId: String,
  val senderName: String,
  val content: String,
  val timestamp: Long,
  val formattedTime: String,
  val type: MessageType = MessageType.TEXT,
  val mediaUrl: String = "",
  val mediaDurationSeconds: Int = 0,
  val mediaSizeFormatted: String = "",
  val fileName: String = "",
  val status: MessageStatus = MessageStatus.READ,
  val isEdited: Boolean = false,
  val isDeletedForEveryone: Boolean = false,
  val isDeletedForMe: Boolean = false,
  val replyToText: String? = null,
  val replyToSender: String? = null,
  val reactions: Map<String, List<String>> = emptyMap(), // emoji -> list of userNames
  val isEndToEndEncrypted: Boolean = true
)

data class Chat(
  val id: String,
  val name: String,
  val isGroup: Boolean = false,
  val avatarUrl: String = "",
  val participants: List<User> = emptyList(),
  val lastMessage: Message? = null,
  val unreadCount: Int = 0,
  val isPinned: Boolean = false,
  val isMuted: Boolean = false,
  val typingStatus: String? = null // e.g. "typing...", "recording audio..."
)

enum class StatusMediaType {
  TEXT,
  IMAGE,
  VIDEO,
  MUSIC,
  VOICE,
  LAYOUT
}

data class SongTrack(
  val id: String,
  val title: String,
  val artist: String,
  val coverUrl: String = "",
  val durationSeconds: Int = 30,
  val genre: String = "Afrobeats"
)

data class StatusStory(
  val id: String,
  val userId: String,
  val userName: String,
  val userAvatar: String = "",
  val text: String = "",
  val imageUrl: String = "",
  val videoUrl: String = "",
  val mediaType: StatusMediaType = StatusMediaType.TEXT,
  val song: SongTrack? = null,
  val voiceDurationSeconds: Int = 0,
  val layoutImages: List<String> = emptyList(),
  val bgHex: Long = 0xFF075E54,
  val timestamp: Long = System.currentTimeMillis(),
  val formattedTime: String = "Today, 10:30 AM",
  val expiresHoursLeft: Int = 24,
  val isViewed: Boolean = false
)

data class Channel(
  val id: String,
  val name: String,
  val description: String,
  val avatarUrl: String = "",
  val verified: Boolean = true,
  val followerCountFormatted: String = "1.2M followers",
  val isFollowing: Boolean = false,
  val posts: List<ChannelPost> = emptyList()
)

data class ChannelPost(
  val id: String,
  val channelId: String,
  val channelName: String,
  val text: String,
  val imageUrl: String = "",
  val formattedTime: String,
  val likesCount: Int = 142,
  val hasLiked: Boolean = false
)

data class CallRecord(
  val id: String,
  val contact: User,
  val callType: CallType,
  val callStatus: CallStatus,
  val formattedTime: String,
  val durationFormatted: String = "3m 12s"
)

data class CloudBackupInfo(
  val lastBackupDate: String = "Today, 02:00 AM",
  val sizeFormatted: String = "28.4 MB",
  val autoFrequency: BackupFrequency = BackupFrequency.DAILY,
  val isEncrypted: Boolean = true,
  val isBackingUp: Boolean = false,
  val progressPercent: Float = 0f,
  val cloudProvider: String = "Firebase Studio & Google Drive"
)

data class VideoStreamItem(
  val id: String,
  val authorName: String,
  val authorHandle: String,
  val authorAvatarUrl: String = "",
  val isVerified: Boolean = true,
  val isFollowing: Boolean = false,
  val description: String,
  val tags: List<String> = emptyList(),
  val musicTrackTitle: String,
  val musicArtist: String,
  val musicCoverUrl: String = "",
  val videoGradientColors: List<Long> = listOf(0xFF1E3C72, 0xFF2A5298, 0xFF0B192C),
  val videoPreviewUrl: String = "",
  val likesCount: Long = 124500,
  val isLiked: Boolean = false,
  val commentsCount: Long = 3420,
  val sharesCount: Long = 890,
  val bookmarksCount: Long = 15200,
  val isBookmarked: Boolean = false,
  val category: String = "Trending",
  val comments: List<VideoComment> = emptyList()
)

data class VideoComment(
  val id: String,
  val userName: String,
  val userAvatar: String = "",
  val text: String,
  val timestampFormatted: String,
  val likesCount: Int = 12,
  val isLiked: Boolean = false
)
