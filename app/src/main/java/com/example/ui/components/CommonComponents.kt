package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.MessageStatus
import com.example.ui.theme.WhatsAppBlueTick
import com.example.ui.theme.WhatsAppGreyTick
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppTeal

@Composable
fun UserAvatar(
  avatarUrl: String,
  name: String,
  size: Dp = 48.dp,
  showOnlineBadge: Boolean = false,
  isOnline: Boolean = false,
  hasUnviewedStatus: Boolean = false,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  val rgbBrush = if (hasUnviewedStatus) rememberAnimatedRgbBrush(durationMillis = 3500) else null

  Box(
    modifier = modifier
      .size(size)
      .then(
        if (hasUnviewedStatus && rgbBrush != null) {
          Modifier.border(2.5.dp, rgbBrush, CircleShape)
        } else Modifier
      )
      .clip(CircleShape)
      .then(
        if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
      )
  ) {
    val isElite = avatarUrl == "elite_ai_system" || name.contains("Elite", ignoreCase = true)

    if (isElite) {
      Box(
        modifier = Modifier
          .matchParentSize()
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
          .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = "Elite AI",
          tint = Color.White,
          modifier = Modifier.size(size * 0.58f)
        )
      }
    } else if (avatarUrl.isNotBlank()) {
      AsyncImage(
        model = avatarUrl,
        contentDescription = "Avatar of $name",
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .matchParentSize()
          .clip(CircleShape)
      )
    } else {
      // Monogram fallback
      val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .ifEmpty { "?" }

      Surface(
        color = WhatsAppTeal.copy(alpha = 0.85f),
        shape = CircleShape,
        modifier = Modifier.matchParentSize()
      ) {
        Box(contentAlignment = Alignment.Center) {
          if (initials == "?") {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(size * 0.6f)
            )
          } else {
            Text(
              text = initials,
              color = Color.White,
              fontSize = (size.value * 0.4f).sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    if (showOnlineBadge && isOnline) {
      Box(
        modifier = Modifier
          .size(size * 0.28f)
          .align(Alignment.BottomEnd)
          .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
          .background(WhatsAppLightGreen, CircleShape)
      )
    }
  }
}

@Composable
fun MessageStatusTick(
  status: MessageStatus,
  modifier: Modifier = Modifier
) {
  when (status) {
    MessageStatus.SENDING -> {
      Icon(
        imageVector = Icons.Default.AccessTime,
        contentDescription = "Sending",
        tint = WhatsAppGreyTick,
        modifier = modifier.size(14.dp)
      )
    }
    MessageStatus.SENT -> {
      Icon(
        imageVector = Icons.Default.Check,
        contentDescription = "Sent",
        tint = WhatsAppGreyTick,
        modifier = modifier.size(15.dp)
      )
    }
    MessageStatus.DELIVERED -> {
      Icon(
        imageVector = Icons.Default.DoneAll,
        contentDescription = "Delivered",
        tint = WhatsAppGreyTick,
        modifier = modifier.size(16.dp)
      )
    }
    MessageStatus.READ -> {
      Icon(
        imageVector = Icons.Default.DoneAll,
        contentDescription = "Read",
        tint = WhatsAppBlueTick,
        modifier = modifier.size(16.dp)
      )
    }
  }
}

@Composable
fun AudioWaveformVisualizer(
  isPlaying: Boolean,
  progress: Float = 0.5f,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "waveform")
  val heights = listOf(0.4f, 0.7f, 0.3f, 0.9f, 0.5f, 0.8f, 0.4f, 1.0f, 0.6f, 0.3f, 0.85f, 0.5f)

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.height(28.dp)
  ) {
    heights.forEachIndexed { index, defaultHeight ->
      val animatedScale by infiniteTransition.animateFloat(
        initialValue = defaultHeight * 0.6f,
        targetValue = defaultHeight,
        animationSpec = infiniteRepeatable(
          animation = tween(400 + (index * 60), easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse
        ),
        label = "bar_$index"
      )

      val barHeight = if (isPlaying) animatedScale else defaultHeight
      val isPast = (index.toFloat() / heights.size) <= progress

      Box(
        modifier = Modifier
          .width(3.dp)
          .fillMaxHeight(barHeight.coerceIn(0.2f, 1.0f))
          .clip(RoundedCornerShape(1.5.dp))
          .background(if (isPast) WhatsAppTeal else WhatsAppGreyTick.copy(alpha = 0.5f))
      )
      Spacer(modifier = Modifier.width(2.5.dp))
    }
  }
}

@Composable
fun E2EEBadge(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 6.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0x700E192B), RoundedCornerShape(12.dp))
      .border(1.dp, Color(0x2500E5FF), RoundedCornerShape(12.dp))
      .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
      .testTag("e2ee_banner")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = "Lock",
        tint = Color(0xFF00E5FF),
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Messages and calls are end-to-end encrypted. No one outside of this chat can read or listen to them. Tap to verify.",
        fontSize = 11.sp,
        lineHeight = 15.sp,
        color = Color(0xFF94A3B8)
      )
    }
  }
}
