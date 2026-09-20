package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MirrorBorderGlint
import com.example.ui.theme.MirrorBorderSubtle
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen
import com.example.ui.theme.RgbNeonPink
import com.example.ui.theme.RgbNeonYellow
import com.example.util.SpeechState

/**
 * Animated audio waveform visualizer that dynamically animates bars based on voice RMS audio level
 */
@Composable
fun SpeechWaveformVisualizer(
  rmsAudioLevel: Float,
  isListening: Boolean,
  modifier: Modifier = Modifier
) {
  val transition = rememberInfiniteTransition(label = "wave_oscillation")
  val wavePhase by transition.animateFloat(
    initialValue = 0f,
    targetValue = 6.283f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "wave_phase"
  )

  Canvas(modifier = modifier.height(36.dp).fillMaxWidth()) {
    val barCount = 24
    val barWidth = 4.dp.toPx()
    val gap = (size.width - (barCount * barWidth)) / (barCount + 1).coerceAtLeast(1)
    val centerY = size.height / 2f
    val maxBarHeight = size.height * 0.9f

    for (i in 0 until barCount) {
      val x = gap + i * (barWidth + gap)
      val sineFactor = kotlin.math.sin(wavePhase + i * 0.35f).toFloat().coerceIn(-1f, 1f)
      val heightFactor = if (isListening) {
        (0.2f + (rmsAudioLevel * 0.75f) * (0.5f + 0.5f * kotlin.math.abs(sineFactor))).coerceIn(0.15f, 1.0f)
      } else {
        0.1f
      }
      val barH = maxBarHeight * heightFactor
      val top = centerY - barH / 2f

      val color = when (i % 4) {
        0 -> RgbNeonCyan
        1 -> RgbNeonGreen
        2 -> Color(0xFFFFD700)
        else -> RgbNeonPink
      }

      drawRoundRect(
        color = color.copy(alpha = if (isListening) 0.95f else 0.4f),
        topLeft = Offset(x, top),
        size = Size(barWidth, barH),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
      )
    }
  }
}

/**
 * Interactive Speech-to-Text panel shown when user is composing a message with their voice.
 */
@Composable
fun SpeechToTextPanel(
  speechState: SpeechState,
  partialText: String,
  composedText: String,
  rmsAudioLevel: Float,
  errorMessage: String?,
  onStopAndAccept: () -> Unit,
  onSendNow: () -> Unit,
  onRetry: () -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse_mic")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.16f,
    animationSpec = infiniteRepeatable(
      animation = tween(700, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  val borderBrush = Brush.horizontalGradient(
    listOf(RgbNeonCyan, Color(0xFFFFD700), RgbNeonGreen, RgbNeonPink)
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .background(Color(0xF00A1322), RoundedCornerShape(20.dp))
      .border(1.5.dp, borderBrush, RoundedCornerShape(20.dp))
      .padding(14.dp)
      .testTag("speech_to_text_panel")
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header: Live Voice Status Badge & Cancel Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(
                if (speechState == SpeechState.LISTENING) RgbNeonGreen
                else if (speechState == SpeechState.ERROR) RgbNeonPink
                else Color(0xFFFFD700)
              )
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = when (speechState) {
              SpeechState.PREPARING -> "INITIALIZING MICROPHONE..."
              SpeechState.LISTENING -> "🎙️ LISTENING • SPEAK NOW"
              SpeechState.PROCESSING -> "PROCESSING VOICE..."
              SpeechState.SUCCESS -> "SPEECH RECOGNIZED"
              SpeechState.ERROR -> "VOICE INPUT ERROR"
              SpeechState.IDLE -> "READY TO DICTATE"
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (speechState == SpeechState.LISTENING) RgbNeonGreen
                    else if (speechState == SpeechState.ERROR) RgbNeonPink
                    else Color(0xFFFFD700),
            letterSpacing = 0.5.sp
          )
        }

        IconButton(
          onClick = onCancel,
          modifier = Modifier.size(28.dp).testTag("cancel_speech_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cancel voice typing",
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Live Audio Waveform Animation
      SpeechWaveformVisualizer(
        rmsAudioLevel = rmsAudioLevel,
        isListening = speechState == SpeechState.LISTENING,
        modifier = Modifier.padding(horizontal = 8.dp)
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Live Transcription Preview Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0x77060D17), RoundedCornerShape(12.dp))
          .border(0.8.dp, MirrorBorderSubtle, RoundedCornerShape(12.dp))
          .padding(horizontal = 12.dp, vertical = 10.dp)
      ) {
        val displayText = when {
          partialText.isNotBlank() -> partialText
          composedText.isNotBlank() -> composedText
          speechState == SpeechState.ERROR -> errorMessage ?: "Could not capture audio. Tap retry to try again."
          speechState == SpeechState.LISTENING -> "Listening... Speak your message clearly into the microphone."
          else -> "Speak now to convert your voice to text..."
        }

        Text(
          text = displayText,
          color = if (speechState == SpeechState.ERROR) Color(0xFFFF8B94)
                  else if (partialText.isNotBlank() || composedText.isNotBlank()) Color.White
                  else Color(0xFF94A3B8),
          fontSize = 14.sp,
          fontWeight = if (partialText.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
          maxLines = 4,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action Buttons Row: Retry, Mic Pulse, Stop/Accept, Send
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Left Action: Retry / Restart
        IconButton(
          onClick = onRetry,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0x331E293B), CircleShape)
            .testTag("retry_speech_button")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Restart listening",
            tint = Color(0xFF94A3B8)
          )
        }

        // Center Pulsing Mic Indicator
        Box(
          modifier = Modifier
            .size(50.dp)
            .scale(if (speechState == SpeechState.LISTENING) pulseScale else 1f)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                listOf(
                  if (speechState == SpeechState.LISTENING) RgbNeonCyan else Color(0x663B82F6),
                  Color(0x220E1A2C)
                )
              ),
              CircleShape
            )
            .border(
              1.5.dp,
              if (speechState == SpeechState.LISTENING) RgbNeonCyan else MirrorBorderSubtle,
              CircleShape
            )
            .clickable {
              if (speechState == SpeechState.LISTENING) onStopAndAccept() else onRetry()
            },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (speechState == SpeechState.LISTENING) Icons.Default.Mic else Icons.Default.MicOff,
            contentDescription = "Microphone Status",
            tint = if (speechState == SpeechState.LISTENING) Color.White else Color(0xFF94A3B8),
            modifier = Modifier.size(24.dp)
          )
        }

        // Right Actions: Accept Text & Send Button
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Accept & Keep in Text Field
          IconButton(
            onClick = onStopAndAccept,
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Color(0x3310B981), CircleShape)
              .border(1.dp, RgbNeonGreen.copy(alpha = 0.5f), CircleShape)
              .testTag("accept_speech_button")
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Insert speech to message",
              tint = RgbNeonGreen
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Send Immediately
          val hasTextToSend = partialText.isNotBlank() || composedText.isNotBlank()
          IconButton(
            onClick = {
              if (hasTextToSend) onSendNow()
            },
            enabled = hasTextToSend,
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(
                if (hasTextToSend) Color(0xEE0E1A2C) else Color(0x331E293B),
                CircleShape
              )
              .border(
                1.5.dp,
                if (hasTextToSend) RgbNeonCyan else Color.Transparent,
                CircleShape
              )
              .testTag("send_speech_now_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send speech message immediately",
              tint = if (hasTextToSend) RgbNeonGreen else Color(0xFF64748B)
            )
          }
        }
      }
    }
  }
}
