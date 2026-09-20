package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Data model for an active typing participant in a group chat.
 */
data class TypingUser(
  val userId: String = "",
  val userName: String = "",
  val userAvatar: String = "",
  val isTyping: Boolean = false,
  val timestamp: Long = 0L
)

/**
 * Real-time Firestore service to broadcast and listen to group chat typing indicators.
 * Communicates over Firestore path: group_chats/{chatId}/typing_indicators/{userId}
 */
class FirestoreTypingService(private val context: Context) {
  companion object {
    private const val TAG = "FirestoreTyping"
    private const val COLLECTION_GROUP_CHATS = "group_chats"
    private const val SUB_COLLECTION_TYPING = "typing_indicators"
    private const val TYPING_TIMEOUT_MS = 6000L // Indicators expire after 6 seconds of inactivity
  }

  val firestore: FirebaseFirestore? by lazy {
    try {
      // The real Firebase app is already initialized at process start from
      // google-services.json (via the Google Services Gradle plugin), so we
      // just grab that default instance here — same real project as LiveDatabaseService.
      FirebaseFirestore.getInstance()
    } catch (e: Exception) {
      Log.w(TAG, "Firestore not available yet: ${e.message}")
      null
    }
  }

  /**
   * Broadcasts the current user's typing state into Firestore.
   * Path: group_chats/{chatId}/typing_indicators/{userId}
   */
  fun setTypingStatus(chatId: String, user: User, isTyping: Boolean) {
    val db = firestore ?: return
    try {
      val docRef = db.collection(COLLECTION_GROUP_CHATS)
        .document(chatId)
        .collection(SUB_COLLECTION_TYPING)
        .document(user.id)

      val data = hashMapOf(
        "userId" to user.id,
        "userName" to user.name,
        "userAvatar" to user.avatarUrl,
        "isTyping" to isTyping,
        "timestamp" to System.currentTimeMillis()
      )

      docRef.set(data, SetOptions.merge())
        .addOnFailureListener { err ->
          Log.w(TAG, "Firestore setTypingStatus error: ${err.message}")
        }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to broadcast typing status: ${e.message}")
    }
  }

  /**
   * Real-time stream of users who are actively composing a message in this group chat.
   * Filters out the current user and stale indicators.
   */
  fun observeGroupTyping(chatId: String, currentUserId: String): Flow<List<TypingUser>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }

    var registration: ListenerRegistration? = null
    try {
      registration = db.collection(COLLECTION_GROUP_CHATS)
        .document(chatId)
        .collection(SUB_COLLECTION_TYPING)
        .whereEqualTo("isTyping", true)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w(TAG, "SnapshotListener error for chat $chatId: ${error.message}")
            return@addSnapshotListener
          }

          val now = System.currentTimeMillis()
          val typers = snapshot?.documents?.mapNotNull { doc ->
            val userId = doc.getString("userId") ?: doc.id
            val userName = doc.getString("userName") ?: "Someone"
            val userAvatar = doc.getString("userAvatar") ?: ""
            val isTyping = doc.getBoolean("isTyping") ?: false
            val timestamp = doc.getLong("timestamp") ?: 0L

            if (isTyping && userId != currentUserId && (now - timestamp) < TYPING_TIMEOUT_MS) {
              TypingUser(
                userId = userId,
                userName = userName,
                userAvatar = userAvatar,
                isTyping = true,
                timestamp = timestamp
              )
            } else null
          }.orEmpty()

          trySend(typers)
        }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to register snapshot listener: ${e.message}")
      trySend(emptyList())
    }

    awaitClose {
      registration?.remove()
    }
  }

  /**
   * Helper to format list of typing users into a human-readable display string.
   */
  fun formatTypingStatus(users: List<TypingUser>): String? {
    return when (users.size) {
      0 -> null
      1 -> "${users[0].userName} is typing..."
      2 -> "${users[0].userName} and ${users[1].userName} are typing..."
      else -> "${users[0].userName} and ${users.size - 1} others are typing..."
    }
  }
}
