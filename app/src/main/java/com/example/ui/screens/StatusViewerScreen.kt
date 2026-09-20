package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.StatusMediaType
import com.example.model.StatusStory
import com.example.ui.components.UserAvatar
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen
import com.example.ui.theme.RgbNeonPink
import com.example.ui.theme.WhatsAppLightGreen

@Composable
fun StatusViewerScreen(
  stories: List<StatusStory>,
  initialIndex: Int = 0,
  onClose: () -> Unit,
  onReply: (String, String) -> Unit
) {
  var currentIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, (stories.size - 1).coerceAtLeast(0))) }
  val currentStory = stories.getOrNull(currentIndex) ?: run {
    onClose()
    return
  }

  val progress = remember { Animatable(0f) }
  var isPaused by remember { mutableStateOf(false) }
  var replyText by remember { mutableStateOf("") }

  LaunchedEffect(currentIndex, isPaused) {
    if (!isPaused) {
      progress.snapTo(0f)
      progress.animateTo(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
      )
      if (currentIndex < stories.size - 1) {
        currentIndex++
      } else {
        onClose()
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
      .pointerInput(Unit) {
        detectTapGestures(
          onPress = {
            isPaused = true
            tryAwaitRelease()
            isPaused = false
          },
          onTap = { offset ->
            if (offset.x < size.width * 0.35f) {
              if (currentIndex > 0) currentIndex--
            } else {
              if (currentIndex < stories.size - 1) currentIndex++ else onClose()
            }
          }
        )
      }
      .testTag("status_viewer_screen")
  ) {
    // Story Content
    val infiniteTransition = rememberInfiniteTransition(label = "status_anim")
    val eqHeight1 by infiniteTransition.animateFloat(
      initialValue = 0.3f,
      targetValue = 0.95f,
      animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
      label = "eq1"
    )
    val eqHeight2 by infiniteTransition.animateFloat(
      initialValue = 0.8f,
      targetValue = 0.2f,
      animationSpec = infiniteRepeatable(tween(350), RepeatMode.Reverse),
      label = "eq2"
    )
    val eqHeight3 by infiniteTransition.animateFloat(
      initialValue = 0.4f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse),
      label = "eq3"
    )

    when {
      // 1. COLLAGE / LAYOUT STORY
      currentStory.mediaType == StatusMediaType.LAYOUT && currentStory.layoutImages.isNotEmpty() -> {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .background(Color(currentStory.bgHex))
            .padding(top = 100.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val images = currentStory.layoutImages
          if (images.size >= 4) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              AsyncImage(model = images[0], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(14.dp)))
              AsyncImage(model = images[1], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(14.dp)))
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              AsyncImage(model = images[2], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(14.dp)))
              AsyncImage(model = images[3], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(14.dp)))
            }
          } else {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              images.forEach { img ->
                AsyncImage(model = img, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(14.dp)))
              }
            }
          }
        }
      }

      // 2. VOICE NOTE STORY
      currentStory.mediaType == StatusMediaType.VOICE -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color(currentStory.bgHex))
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(24.dp))
              .background(Color(0x70071322), RoundedCornerShape(24.dp))
              .border(1.5.dp, RgbNeonGreen.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
              .padding(24.dp)
          ) {
            Box(
              modifier = Modifier
                .size(72.dp)
                .background(Color(0xCC0E1A2C), CircleShape)
                .border(2.dp, RgbNeonGreen, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Mic, contentDescription = null, tint = RgbNeonGreen, modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Voice Status Note", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Duration 0:${currentStory.voiceDurationSeconds.coerceAtLeast(12).toString().padStart(2, '0')}", color = RgbNeonGreen, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(18.dp))

            // Audio Waveform Visualizer
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 8.dp),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically
            ) {
              listOf(0.4f, 0.7f, 1f, 0.6f, 0.85f, 0.4f, 0.9f, 0.5f, 0.75f, 1f, 0.3f, 0.7f, 0.9f, 0.6f).forEachIndexed { idx, barH ->
                val dynamicH = if (idx % 2 == 0) barH * eqHeight1 else barH * eqHeight2
                Box(
                  modifier = Modifier
                    .width(4.dp)
                    .height((40 * dynamicH).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (idx < 8) RgbNeonGreen else Color(0x6094A3B8))
                )
              }
            }
          }
        }
      }

      // 3. IMAGE OR VIDEO OR MUSIC STORY
      currentStory.imageUrl.isNotBlank() || currentStory.videoUrl.isNotBlank() || currentStory.song != null -> {
        Box(modifier = Modifier.fillMaxSize()) {
          if (currentStory.imageUrl.isNotBlank()) {
            AsyncImage(
              model = currentStory.imageUrl,
              contentDescription = "Status photo",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          } else {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(Color(currentStory.bgHex))
            )
          }

          // Dark cinematic gradient overlay
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color.Black.copy(alpha = 0.4f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.7f)
                  )
                )
              )
          )

          // Music Tag / Sticker if present
          if (currentStory.song != null) {
            Box(
              modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xCC0D1526), RoundedCornerShape(20.dp))
                .border(1.2.dp, RgbNeonPink, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .background(Color(0x40FF007F), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.MusicNote, contentDescription = null, tint = RgbNeonPink, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(currentStory.song.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                  Text("${currentStory.song.artist} • ${currentStory.song.genre}", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                // Equalizer wave bars
                Row(
                  horizontalArrangement = Arrangement.spacedBy(2.dp),
                  verticalAlignment = Alignment.Bottom,
                  modifier = Modifier.height(16.dp)
                ) {
                  Box(modifier = Modifier.width(3.dp).height((14 * eqHeight1).dp).background(RgbNeonPink, RoundedCornerShape(1.dp)))
                  Box(modifier = Modifier.width(3.dp).height((14 * eqHeight2).dp).background(RgbNeonCyan, RoundedCornerShape(1.dp)))
                  Box(modifier = Modifier.width(3.dp).height((14 * eqHeight3).dp).background(RgbNeonGreen, RoundedCornerShape(1.dp)))
                }
              }
            }
          }

          // Video Play indicator badge
          if (currentStory.mediaType == StatusMediaType.VIDEO || currentStory.videoUrl.isNotBlank()) {
            Box(
              modifier = Modifier
                .align(Alignment.Center)
                .size(64.dp)
                .background(Color(0x80000000), CircleShape)
                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = "Play Video", tint = Color.White, modifier = Modifier.size(36.dp))
            }
          }
        }
      }

      // 4. TEXT ONLY STORY
      else -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color(currentStory.bgHex))
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = currentStory.text,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
          )
        }
      }
    }

    // Caption overlay if any
    if (currentStory.text.isNotBlank() && currentStory.mediaType != StatusMediaType.TEXT && currentStory.mediaType != StatusMediaType.VOICE) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .padding(bottom = 90.dp, start = 20.dp, end = 20.dp)
          .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
          .padding(14.dp)
      ) {
        Text(
          text = currentStory.text,
          color = Color.White,
          fontSize = 15.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    // Top Controls & Segmented Progress Bar
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color.Black.copy(alpha = 0.35f))
        .padding(top = 40.dp, start = 12.dp, end = 12.dp, bottom = 12.dp)
    ) {
      // Progress Bars
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        stories.forEachIndexed { index, _ ->
          val segmentProgress = when {
            index < currentIndex -> 1f
            index == currentIndex -> progress.value
            else -> 0f
          }
          LinearProgressIndicator(
            progress = { segmentProgress },
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f),
            modifier = Modifier
              .weight(1f)
              .height(3.dp)
              .clip(RoundedCornerShape(1.5.dp))
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // User Info Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          UserAvatar(
            avatarUrl = currentStory.userAvatar,
            name = currentStory.userName,
            size = 40.dp
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = currentStory.userName,
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${currentStory.formattedTime} • Expires in ${currentStory.expiresHoursLeft}h",
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 11.sp
            )
          }
        }

        IconButton(onClick = onClose) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close status",
            tint = Color.White
          )
        }
      }
    }

    // Bottom Reply Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .background(Color.Black.copy(alpha = 0.4f))
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = replyText,
        onValueChange = { replyText = it },
        placeholder = { Text("Reply to status...", color = Color.White.copy(alpha = 0.6f)) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = Color.White.copy(alpha = 0.7f),
          unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("status_reply_input")
      )

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = {
          if (replyText.isNotBlank()) {
            onReply(currentStory.userId, replyText.trim())
            replyText = ""
            onClose()
          }
        },
        modifier = Modifier
          .size(44.dp)
          .background(WhatsAppLightGreen, CircleShape)
      ) {
        Icon(
          imageVector = Icons.Default.Send,
          contentDescription = "Send Reply",
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
