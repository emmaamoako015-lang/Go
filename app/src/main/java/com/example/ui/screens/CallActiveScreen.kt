package com.example.ui.screens

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ChatRepository
import com.example.ui.components.UserAvatar
import com.example.ui.components.rememberAnimatedRgbBrush
import com.example.ui.theme.MirrorBorderSubtle
import com.example.ui.theme.MirrorObsidian
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen
import com.example.ui.theme.RgbNeonPink

@Composable
fun CallActiveScreen(
  repository: ChatRepository
) {
  val context = LocalContext.current
  val activeCall by repository.activeCall.collectAsState()
  val call = activeCall ?: return
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 3500)
  var isLocationShared by remember { mutableStateOf(false) }

  val hardwarePermissionsLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { _ -> }

  androidx.compose.runtime.LaunchedEffect(call.callId) {
    val neededPermissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
    if (call.isVideo) {
      neededPermissions.add(Manifest.permission.CAMERA)
    }
    hardwarePermissionsLauncher.launch(neededPermissions.toTypedArray())
  }

  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (granted) {
      isLocationShared = true
    }
  }

  val formattedDuration = String.format(
    "%02d:%02d",
    call.durationSeconds / 60,
    call.durationSeconds % 60
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF0D1526),
            MirrorObsidian,
            Color(0xFF070B12)
          )
        )
      )
      .testTag("call_active_screen")
  ) {
    if (call.isVideo && call.isVideoEnabled) {
      // Simulated remote video feed
      AsyncImage(
        model = call.contact.avatarUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600" },
        contentDescription = "Remote video feed",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )

      // Gradient overlay for legibility
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color.Black.copy(alpha = 0.6f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.85f)
              )
            )
          )
      )

      // Local camera PIP box with RGB border
      Box(
        modifier = Modifier
          .size(width = 110.dp, height = 150.dp)
          .align(Alignment.TopEnd)
          .padding(top = 48.dp, end = 16.dp)
          .clip(RoundedCornerShape(14.dp))
          .border(1.5.dp, rgbBrush, RoundedCornerShape(14.dp))
          .background(Color(0xFF0E1A2C))
          .clickable { repository.flipCamera() }
      ) {
        AsyncImage(
          model = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300",
          contentDescription = "Local Camera PIP",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
        Text(
          text = if (call.isFrontCamera) "Front" else "Back",
          color = Color.White,
          fontSize = 10.sp,
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
        )
      }
    }

    // Call Details (Top Center)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter)
        .padding(top = 56.dp, start = 20.dp, end = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0x700E1A2C), RoundedCornerShape(16.dp))
          .border(1.dp, rgbBrush, RoundedCornerShape(16.dp))
          .padding(horizontal = 12.dp, vertical = 5.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = "Encrypted",
          tint = RgbNeonCyan,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
          text = "End-to-End Encrypted WebRTC",
          color = Color.White,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )
      }

      if (isLocationShared) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x80064E3B), RoundedCornerShape(14.dp))
            .border(1.dp, RgbNeonGreen, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location Shared",
            tint = RgbNeonGreen,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Live Location Shared: 37.7749° N, 122.4194° W",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      if (!call.isVideo || !call.isVideoEnabled) {
        Box(
          modifier = Modifier
            .size(108.dp)
            .clip(CircleShape)
            .border(2.dp, rgbBrush, CircleShape)
            .padding(4.dp)
        ) {
          UserAvatar(
            avatarUrl = call.contact.avatarUrl,
            name = call.contact.name,
            size = 100.dp
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      Text(
        text = call.contact.name,
        color = Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = formattedDuration,
        color = RgbNeonCyan,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
      )
    }

    // Call Action Controls (Bottom)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .padding(bottom = 44.dp, start = 24.dp, end = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Row 1: Toggles in Sleek Mirror Capsule
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(26.dp))
          .background(Color(0xDD0D1627), RoundedCornerShape(26.dp))
          .border(1.2.dp, rgbBrush, RoundedCornerShape(26.dp))
          .drawWithContent {
            drawContent()
            drawRect(
              brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
              ),
              size = size.copy(height = size.height * 0.45f)
            )
          }
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Speaker
          IconButton(
            onClick = { repository.toggleSpeaker() },
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(if (call.isSpeakerOn) RgbNeonCyan.copy(alpha = 0.25f) else Color(0x3015243B), CircleShape)
              .border(1.dp, if (call.isSpeakerOn) RgbNeonCyan else MirrorBorderSubtle, CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.VolumeUp,
              contentDescription = "Speaker",
              tint = if (call.isSpeakerOn) RgbNeonCyan else Color.White
            )
          }

          // Camera Flip (if video call)
          if (call.isVideo) {
            IconButton(
              onClick = { repository.flipCamera() },
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0x3015243B), CircleShape)
                .border(1.dp, MirrorBorderSubtle, CircleShape)
            ) {
              Icon(
                imageVector = Icons.Default.Cameraswitch,
                contentDescription = "Flip Camera",
                tint = Color.White
              )
            }

            // Video Toggle
            IconButton(
              onClick = { repository.toggleVideo() },
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (call.isVideoEnabled) Color(0x3015243B) else RgbNeonPink.copy(alpha = 0.25f), CircleShape)
                .border(1.dp, if (call.isVideoEnabled) MirrorBorderSubtle else RgbNeonPink, CircleShape)
            ) {
              Icon(
                imageVector = if (call.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                contentDescription = "Toggle Video",
                tint = if (call.isVideoEnabled) Color.White else RgbNeonPink
              )
            }
          }

          // Mute Mic
          IconButton(
            onClick = { repository.toggleMute() },
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(if (call.isMuted) RgbNeonPink.copy(alpha = 0.25f) else Color(0x3015243B), CircleShape)
              .border(1.dp, if (call.isMuted) RgbNeonPink else MirrorBorderSubtle, CircleShape)
          ) {
            Icon(
              imageVector = if (call.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
              contentDescription = "Mute",
              tint = if (call.isMuted) RgbNeonPink else Color.White
            )
          }

          // Call Location Share Toggle
          IconButton(
            onClick = {
              if (!isLocationShared) {
                locationPermissionLauncher.launch(
                  arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                  )
                )
              } else {
                isLocationShared = false
              }
            },
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(if (isLocationShared) RgbNeonGreen.copy(alpha = 0.25f) else Color(0x3015243B), CircleShape)
              .border(1.dp, if (isLocationShared) RgbNeonGreen else MirrorBorderSubtle, CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = "Share Call Location",
              tint = if (isLocationShared) RgbNeonGreen else Color.White
            )
          }

          // Share Call to External App
          IconButton(
            onClick = {
              val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                  Intent.EXTRA_TEXT,
                  "Join my Global Stream encrypted call with ${call.contact.name}!\nLink: https://globalstream.app/call/${call.callId}"
                )
              }
              val shareIntent = Intent.createChooser(sendIntent, "Share Call Link via")
              context.startActivity(shareIntent)
            },
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(Color(0x3015243B), CircleShape)
              .border(1.dp, MirrorBorderSubtle, CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share Call Link",
              tint = RgbNeonCyan
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Row 2: End Call Button
      Box(
        modifier = Modifier
          .size(68.dp)
          .clip(CircleShape)
          .background(Color(0xFFE50914), CircleShape)
          .border(2.dp, RgbNeonPink, CircleShape)
          .clickable { repository.endCall() }
          .testTag("end_call_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.CallEnd,
          contentDescription = "End Call",
          tint = Color.White,
          modifier = Modifier.size(32.dp)
        )
      }
    }
  }
}
