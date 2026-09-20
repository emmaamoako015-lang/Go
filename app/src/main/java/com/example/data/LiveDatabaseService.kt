package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.Message
import com.example.model.MessageStatus
import com.example.model.MessageType
import com.example.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Live Firestore Database Service for syncing real-time chats, messages, and user presence.
 * Collections:
 * - "users": User profiles and real-time online presence / last seen
 * - "chats": Chat metadata, active participants, and last message snippet
 * - "chats/{chatId}/messages": Real-time message logs synced across live clients
 */
class LiveDatabaseService(private val context: Context? = null) {

  companion object {
    private const val TAG = "LiveDatabaseService"
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_CHATS = "chats"
    private const val SUBCOLLECTION_MESSAGES = "messages"
  }

  val firestore: FirebaseFirestore? by lazy {
    try {
      if (context != null && FirebaseApp.getApps(context).isEmpty()) {
        try {
          FirebaseApp.initializeApp(context)
        } catch (e: Exception) {
          val options = FirebaseOptions.Builder()
            .setApplicationId("1:246278353299:android:f7b5fdb47788a940913866")
            .setProjectId("globalstream-c74fc")
            .setApiKey("AIzaSyCDI9xUlxLt02JXWO3pUYIZPrdEK2Jy4bM")
            .setStorageBucket("globalstream-c74fc.firebasestorage.app")
            .build()
          FirebaseApp.initializeApp(context, options)
        }
      }
      FirebaseFirestore.getInstance()
    } catch (e: Exception) {
      Log.w(TAG, "Standard Firestore initialization notice: ${e.message}")
      try {
        val options = FirebaseOptions.Builder()
          .setApplicationId("1:246278353299:android:f7b5fdb47788a940913866")
          .setProjectId("globalstream-c74fc")
          .setApiKey("AIzaSyCDI9xUlxLt02JXWO3pUYIZPrdEK2Jy4bM")
          .setStorageBucket("globalstream-c74fc.firebasestorage.app")
          .build()
        val app = if (context != null) {
          FirebaseApp.initializeApp(context, options, "globalstream_app")
        } else null
        if (app != null) FirebaseFirestore.getInstance(app) else null
      } catch (ex: Exception) {
        Log.w(TAG, "Secondary Firestore initialization error: ${ex.message}")
        null
      }
    }
  }

  private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

  /**
   * Syncs user profile and online status to Firestore
   */
  fun syncUserPresence(user: User, isOnline: Boolean) {
    val db = firestore ?: return
    try {
      val data = hashMapOf(
        "id" to user.id,
        "name" to user.name,
        "phoneNumber" to user.phoneNumber,
        "email" to user.email,
        "avatarUrl" to user.avatarUrl,
        "about" to user.about,
        "isOnline" to isOnline,
        "lastSeen" to if (isOnline) "online" else "last seen recently",
        "lastSeenTimestamp" to System.currentTimeMillis()
      )
      db.collection(COLLECTION_USERS).document(user.id).set(data, SetOptions.merge())
    } catch (e: Exception) {
      Log.w(TAG, "Failed to sync user presence: ${e.message}")
    }
  }

  /**
   * Sends a real-time message to Firestore
   */
  fun sendMessage(chatId: String, message: Message) {
    val db = firestore ?: return
    try {
      val msgMap = hashMapOf(
        "id" to message.id,
        "chatId" to chatId,
        "senderId" to message.senderId,
        "senderName" to message.senderName,
        "content" to message.content,
        "timestamp" to message.timestamp,
        "formattedTime" to message.formattedTime,
        "type" to message.type.name,
        "status" to message.status.name,
        "mediaUrl" to message.mediaUrl,
        "fileName" to (message.fileName ?: ""),
        "mediaSizeFormatted" to (message.mediaSizeFormatted ?: ""),
        "replyToText" to (message.replyToText ?: ""),
        "replyToSender" to (message.replyToSender ?: ""),
        "isEndToEndEncrypted" to message.isEndToEndEncrypted
      )

      // 1. Write message document to chat messages subcollection
      db.collection(COLLECTION_CHATS)
        .document(chatId)
        .collection(SUBCOLLECTION_MESSAGES)
        .document(message.id)
        .set(msgMap, SetOptions.merge())

      // 2. Update chat parent document with last message
      val chatUpdate = hashMapOf(
        "lastMessageId" to message.id,
        "lastMessageContent" to message.content,
        "lastMessageSenderId" to message.senderId,
        "lastMessageTimestamp" to message.timestamp,
        "lastMessageFormattedTime" to message.formattedTime,
        "lastMessageStatus" to message.status.name
      )
      db.collection(COLLECTION_CHATS).document(chatId).set(chatUpdate, SetOptions.merge())
    } catch (e: Exception) {
      Log.w(TAG, "Failed to send live message to Firestore: ${e.message}")
    }
  }

  /**
   * Updates message read/delivered receipt in Firestore
   */
  fun updateMessageStatus(chatId: String, messageId: String, status: MessageStatus) {
    val db = firestore ?: return
    try {
      db.collection(COLLECTION_CHATS)
        .document(chatId)
        .collection(SUBCOLLECTION_MESSAGES)
        .document(messageId)
        .update("status", status.name)
    } catch (e: Exception) {
      Log.w(TAG, "Failed to update live message status: ${e.message}")
    }
  }

  /**
   * Observes live messages for a specific chat from Firestore
   */
  fun observeMessages(chatId: String): Flow<List<Message>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val registration: ListenerRegistration = db.collection(COLLECTION_CHATS)
      .document(chatId)
      .collection(SUBCOLLECTION_MESSAGES)
      .orderBy("timestamp", Query.Direction.ASCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "Firestore observeMessages listener error: ${error.message}")
          return@addSnapshotListener
        }

        if (snapshot != null) {
          val list = snapshot.documents.mapNotNull { doc -> parseMessage(doc) }
          trySend(list)
        }
      }

    awaitClose {
      registration.remove()
    }
  }

  /**
   * Observes live user presence updates from Firestore
   */
  fun observeUsers(): Flow<List<User>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val registration: ListenerRegistration = db.collection(COLLECTION_USERS)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "Firestore observeUsers error: ${error.message}")
          return@addSnapshotListener
        }

        if (snapshot != null) {
          val users = snapshot.documents.mapNotNull { doc -> parseUser(doc) }
          trySend(users)
        }
      }

    awaitClose {
      registration.remove()
    }
  }

  private fun parseMessage(doc: DocumentSnapshot): Message? {
    return try {
      val typeStr = doc.getString("type") ?: MessageType.TEXT.name
      val statusStr = doc.getString("status") ?: MessageStatus.DELIVERED.name
      Message(
        id = doc.getString("id") ?: doc.id,
        chatId = doc.getString("chatId") ?: "",
        senderId = doc.getString("senderId") ?: "",
        senderName = doc.getString("senderName") ?: "",
        content = doc.getString("content") ?: "",
        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
        formattedTime = doc.getString("formattedTime") ?: timeFormat.format(Date()),
        type = try { MessageType.valueOf(typeStr) } catch (e: Exception) { MessageType.TEXT },
        status = try { MessageStatus.valueOf(statusStr) } catch (e: Exception) { MessageStatus.DELIVERED },
        mediaUrl = doc.getString("mediaUrl") ?: "",
        fileName = doc.getString("fileName") ?: "",
        mediaSizeFormatted = doc.getString("mediaSizeFormatted") ?: "",
        replyToText = doc.getString("replyToText"),
        replyToSender = doc.getString("replyToSender"),
        isEndToEndEncrypted = doc.getBoolean("isEndToEndEncrypted") ?: true
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun parseUser(doc: DocumentSnapshot): User? {
    return try {
      User(
        id = doc.getString("id") ?: doc.id,
        name = doc.getString("name") ?: "Contact",
        phoneNumber = doc.getString("phoneNumber") ?: "",
        email = doc.getString("email") ?: "",
        avatarUrl = doc.getString("avatarUrl") ?: "",
        about = doc.getString("about") ?: "",
        isOnline = doc.getBoolean("isOnline") ?: false,
        lastSeen = doc.getString("lastSeen") ?: "offline"
      )
    } catch (e: Exception) {
      null
    }
  }
}
