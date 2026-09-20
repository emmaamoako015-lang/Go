package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TypingUser
import com.example.ui.theme.MirrorBorderSubtle
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen

/**
 * Animated 3-dot wave visualizer for typing indicators.
 */
@Composable
fun BouncingDotsIndicator(
  modifier: Modifier = Modifier,
  dotSize: Dp = 6.dp,
  dotColor: Color = RgbNeonGreen,
  bounceHeight: Dp = 4.dp
) {
  val transition = rememberInfiniteTransition(label = "BouncingDots")

  val dot1Offset by transition.animateFloat(
    initialValue = 0f,
    targetValue = -bounceHeight.value,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "Dot1"
  )

  val dot2Offset by transition.animateFloat(
    initialValue = 0f,
    targetValue = -bounceHeight.value,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 400, delayMillis = 130, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "Dot2"
  )

  val dot3Offset by transition.animateFloat(
    initialValue = 0f,
    targetValue = -bounceHeight.value,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 400, delayMillis = 260, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "Dot3"
  )

  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(3.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .offset(y = dot1Offset.dp)
        .size(dotSize)
        .clip(CircleShape)
        .background(dotColor)
    )
    Box(
      modifier = Modifier
        .offset(y = dot2Offset.dp)
        .size(dotSize)
        .clip(CircleShape)
        .background(dotColor.copy(alpha = 0.9f))
    )
    Box(
      modifier = Modifier
        .offset(y = dot3Offset.dp)
        .size(dotSize)
        .clip(CircleShape)
        .background(dotColor.copy(alpha = 0.8f))
    )
  }
}

/**
 * Sleek mirror-glass typing bubble rendered right above the input bar in group chats.
 */
@Composable
fun GroupTypingIndicatorBubble(
  typingUsers: List<TypingUser>,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = typingUsers.isNotEmpty(),
    enter = fadeIn() + slideInVertically { it },
    exit = fadeOut() + slideOutVertically { it }
  ) {
    val displayText = when (typingUsers.size) {
      0 -> ""
      1 -> "${typingUsers[0].userName} is typing"
      2 -> "${typingUsers[0].userName} & ${typingUsers[1].userName} are typing"
      else -> "${typingUsers[0].userName} & ${typingUsers.size - 1} others are typing"
    }

    Box(
      modifier = modifier
        .padding(horizontal = 14.dp, vertical = 4.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(Color(0xDD0D1627), RoundedCornerShape(16.dp))
        .border(1.dp, MirrorBorderSubtle, RoundedCornerShape(16.dp))
        .drawWithContent {
          drawContent()
          drawRect(
            brush = Brush.verticalGradient(
              listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
            ),
            size = size.copy(height = size.height * 0.5f)
          )
        }
        .padding(horizontal = 12.dp, vertical = 7.dp)
        .testTag("group_typing_indicator_bubble")
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
      ) {
        // Render first typing user avatar
        val firstUser = typingUsers.firstOrNull()
        if (firstUser != null) {
          UserAvatar(
            avatarUrl = firstUser.userAvatar,
            name = firstUser.userName,
            size = 20.dp
          )
          Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
          text = displayText,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = RgbNeonCyan
        )

        Spacer(modifier = Modifier.width(8.dp))

        BouncingDotsIndicator(
          dotSize = 5.dp,
          dotColor = RgbNeonGreen,
          bounceHeight = 3.dp
        )
      }
    }
  }
}
