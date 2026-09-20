package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MirrorBorderSubtle
import com.example.ui.theme.RgbNeonGreen

/**
 * Animated presence dot with an outer breathing ripple effect when online.
 */
@Composable
fun PresencePulseDot(
  isOnline: Boolean,
  size: Dp = 8.dp,
  modifier: Modifier = Modifier
) {
  if (isOnline) {
    val infiniteTransition = rememberInfiniteTransition(label = "PresencePulse")
    val scale by infiniteTransition.animateFloat(
      initialValue = 1f,
      targetValue = 1.85f,
      animationSpec = infiniteRepeatable(
        animation = tween(1400, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Restart
      ),
      label = "pulseScale"
    )
    val alpha by infiniteTransition.animateFloat(
      initialValue = 0.65f,
      targetValue = 0f,
      animationSpec = infiniteRepeatable(
        animation = tween(1400, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Restart
      ),
      label = "pulseAlpha"
    )

    Box(
      modifier = modifier.size(size * 2f),
      contentAlignment = Alignment.Center
    ) {
      // Expanding ripple ring
      Box(
        modifier = Modifier
          .size(size)
          .scale(scale)
          .background(RgbNeonGreen.copy(alpha = alpha), CircleShape)
      )
      // Solid inner core
      Box(
        modifier = Modifier
          .size(size)
          .background(RgbNeonGreen, CircleShape)
          .border(1.dp, Color(0xFF0B1424), CircleShape)
      )
    }
  } else {
    // Offline status dot
    Box(
      modifier = modifier
        .size(size)
        .background(Color(0xFF64748B), CircleShape)
        .border(1.dp, Color(0xFF0B1424), CircleShape)
    )
  }
}

/**
 * Pill-style chip showing "online" or last seen info with colored status dot.
 */
@Composable
fun PresenceBadge(
  isOnline: Boolean,
  lastSeenText: String = if (isOnline) "online" else "offline",
  compact: Boolean = false,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(
        if (isOnline) Color(0x2E00FF88) else Color(0x1F64748B),
        RoundedCornerShape(12.dp)
      )
      .border(
        width = 0.8.dp,
        color = if (isOnline) RgbNeonGreen.copy(alpha = 0.6f) else MirrorBorderSubtle,
        shape = RoundedCornerShape(12.dp)
      )
      .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = if (compact) 2.dp else 3.dp)
      .testTag(if (isOnline) "presence_badge_online" else "presence_badge_offline"),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      PresencePulseDot(isOnline = isOnline, size = if (compact) 6.dp else 7.dp)
      Spacer(modifier = Modifier.width(if (compact) 4.dp else 6.dp))
      Text(
        text = if (isOnline) "Online" else lastSeenText,
        fontSize = if (compact) 10.sp else 11.sp,
        fontWeight = if (isOnline) FontWeight.Bold else FontWeight.Medium,
        color = if (isOnline) RgbNeonGreen else Color(0xFF94A3B8),
        maxLines = 1
      )
    }
  }
}

/**
 * Presence indicator bar specifically designed for chat headers with real-time status.
 */
@Composable
fun ChatHeaderPresence(
  isOnline: Boolean,
  lastSeenText: String,
  typingText: String? = null,
  isGroup: Boolean = false,
  participantCount: Int = 0,
  isElite: Boolean = false,
  modifier: Modifier = Modifier
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.testTag("chat_header_presence")
  ) {
    when {
      typingText != null -> {
        BouncingDotsIndicator(
          dotSize = 4.dp,
          dotColor = RgbNeonGreen,
          bounceHeight = 2.dp
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = typingText,
          color = RgbNeonGreen,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1
        )
      }
      isElite -> {
        Text(
          text = "👑 Full Creation Access • Ready for your command",
          color = Color(0xFFFFD700),
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1
        )
      }
      isGroup -> {
        Text(
          text = "$participantCount participants",
          color = Color(0xFF94A3B8),
          fontSize = 12.sp,
          fontWeight = FontWeight.Normal,
          maxLines = 1
        )
      }
      isOnline -> {
        PresencePulseDot(isOnline = true, size = 6.5.dp)
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = "online",
          color = RgbNeonGreen,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1
        )
      }
      else -> {
        PresencePulseDot(isOnline = false, size = 6.dp)
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = lastSeenText,
          color = Color(0xFF94A3B8),
          fontSize = 12.sp,
          fontWeight = FontWeight.Normal,
          maxLines = 1
        )
      }
    }
  }
}
