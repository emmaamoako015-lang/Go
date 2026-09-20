package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

enum class SpeechState {
  IDLE,
  PREPARING,
  LISTENING,
  PROCESSING,
  SUCCESS,
  ERROR
}

/**
 * SpeechToTextManager provides robust, real-time speech recognition
 * for composing messages via voice in the chat interface.
 */
class SpeechToTextManager(private val context: Context) {

  companion object {
    private const val TAG = "SpeechToTextManager"

    fun isSpeechRecognitionAvailable(context: Context): Boolean {
      return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun buildSpeechRecognizerIntent(): Intent {
      return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now to compose your message...")
      }
    }
  }

  private var speechRecognizer: SpeechRecognizer? = null

  private val _speechState = MutableStateFlow(SpeechState.IDLE)
  val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

  private val _partialText = MutableStateFlow("")
  val partialText: StateFlow<String> = _partialText.asStateFlow()

  private val _rmsAudioLevel = MutableStateFlow(0f)
  val rmsAudioLevel: StateFlow<Float> = _rmsAudioLevel.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  var onFinalResultListener: ((String) -> Unit)? = null
  var onPartialResultListener: ((String) -> Unit)? = null

  private fun ensureRecognizer(): SpeechRecognizer {
    val existing = speechRecognizer
    if (existing != null) return existing

    val newRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    newRecognizer.setRecognitionListener(object : RecognitionListener {
      override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "onReadyForSpeech")
        _speechState.value = SpeechState.LISTENING
        _errorMessage.value = null
      }

      override fun onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech")
        _speechState.value = SpeechState.LISTENING
      }

      override fun onRmsChanged(rmsdB: Float) {
        // Normalize rmsdB for wave animation (typically -2dB to 10dB)
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1.0f)
        _rmsAudioLevel.value = normalized
      }

      override fun onBufferReceived(buffer: ByteArray?) {}

      override fun onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech")
        _speechState.value = SpeechState.PROCESSING
      }

      override fun onError(error: Int) {
        val errorMsg = when (error) {
          SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
          SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error"
          SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
          SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition"
          SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
          SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please speak clearly and try again."
          SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Retrying..."
          SpeechRecognizer.ERROR_SERVER -> "Speech recognition server error"
          SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard. Tap mic to speak."
          else -> "Speech error ($error)"
        }
        Log.w(TAG, "onError: $errorMsg ($error)")
        _errorMessage.value = errorMsg
        _speechState.value = SpeechState.ERROR
        _rmsAudioLevel.value = 0f
      }

      override fun onResults(results: Bundle?) {
        _speechState.value = SpeechState.SUCCESS
        _rmsAudioLevel.value = 0f
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val bestMatch = matches?.firstOrNull()?.trim() ?: ""
        if (bestMatch.isNotEmpty()) {
          _partialText.value = bestMatch
          onFinalResultListener?.invoke(bestMatch)
        }
      }

      override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partial = matches?.firstOrNull()?.trim() ?: ""
        if (partial.isNotEmpty()) {
          _partialText.value = partial
          onPartialResultListener?.invoke(partial)
        }
      }

      override fun onEvent(eventType: Int, params: Bundle?) {}
    })
    speechRecognizer = newRecognizer
    return newRecognizer
  }

  fun startListening(
    onPartial: ((String) -> Unit)? = null,
    onFinal: ((String) -> Unit)? = null
  ) {
    this.onPartialResultListener = onPartial
    this.onFinalResultListener = onFinal

    _partialText.value = ""
    _errorMessage.value = null
    _speechState.value = SpeechState.PREPARING

    try {
      val recognizer = ensureRecognizer()
      val intent = buildSpeechRecognizerIntent()
      recognizer.startListening(intent)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start listening", e)
      _errorMessage.value = e.localizedMessage ?: "Failed to start voice recognition"
      _speechState.value = SpeechState.ERROR
    }
  }

  fun stopListening() {
    try {
      speechRecognizer?.stopListening()
      _speechState.value = SpeechState.PROCESSING
    } catch (e: Exception) {
      Log.e(TAG, "Failed to stop listening", e)
    }
  }

  fun cancel() {
    try {
      speechRecognizer?.cancel()
      _speechState.value = SpeechState.IDLE
      _rmsAudioLevel.value = 0f
      _partialText.value = ""
      _errorMessage.value = null
    } catch (e: Exception) {
      Log.e(TAG, "Failed to cancel listening", e)
    }
  }

  fun destroy() {
    try {
      speechRecognizer?.destroy()
      speechRecognizer = null
    } catch (e: Exception) {
      Log.e(TAG, "Failed to destroy SpeechRecognizer", e)
    }
  }
}
