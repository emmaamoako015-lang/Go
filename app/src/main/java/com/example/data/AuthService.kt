package com.example.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Real Firebase Authentication service.
 *
 * Phase 1 (now): anonymous auth — every real device/install gets its own real,
 * stable Firebase UID the moment the app opens. No fake shared "user_me" id.
 * The UID persists across app restarts (Firebase caches it on-device) so the
 * same install always maps to the same identity in Firestore.
 *
 * Phase 2 (next): swap anonymous sign-in for real phone number verification
 * (Firebase Phone Auth) so identity is tied to a real phone number like
 * WhatsApp, and can be linked from the existing AuthPhoneDialog UI. Phone
 * auth requires the Firebase project to be on the Blaze (pay-as-you-go) plan.
 */
class AuthService {
  companion object {
    private const val TAG = "AuthService"
  }

  private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

  /** Real Firebase UID for this device, once signed in. Null until sign-in completes. */
  val currentUid: String?
    get() = auth.currentUser?.uid

  /**
   * Signs this device in anonymously if it isn't already. Safe to call every app launch —
   * Firebase returns the same existing user instead of creating a new one if a session exists.
   */
  suspend fun ensureSignedIn(): String? {
    return try {
      auth.currentUser?.let { return it.uid }
      val result = auth.signInAnonymously().await()
      val uid = result.user?.uid
      Log.i(TAG, "Signed in anonymously with real Firebase UID: $uid")
      uid
    } catch (e: Throwable) {
      // Catches everything, including FirebaseApp-not-initialized errors from the
      // lazy `auth` getter itself — a broken/misconfigured Firebase setup must
      // never crash the app on launch. The app falls back to the placeholder
      // local identity and chat still works locally; only real cross-device
      // sync is unavailable until this is fixed.
      Log.e(TAG, "Firebase sign-in unavailable: ${e.message}")
      null
    }
  }
}
