package com.example.ui.dialogs

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ChatRepository
import com.example.model.User
import com.example.ui.components.MirrorText
import com.example.ui.components.PresencePulseDot
import com.example.ui.components.UserAvatar
import com.example.ui.components.rememberAnimatedRgbBrush
import com.example.ui.theme.MirrorBorderSubtle
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen

@Composable
fun StartCallDialog(
  repository: ChatRepository,
  onDismiss: () -> Unit
) {
  val chats by repository.chats.collectAsState()
  val currentUser = repository.currentUser.value
  val allContacts = chats.flatMap { it.participants }
    .filter { it.id != currentUser.id }
    .distinctBy { it.id }

  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 3500)

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(22.dp))
        .background(Color(0xF50D1526), RoundedCornerShape(22.dp))
        .border(1.2.dp, rgbBrush, RoundedCornerShape(22.dp))
        .drawWithContent {
          drawContent()
          drawRect(
            brush = Brush.verticalGradient(
              listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
            ),
            size = size.copy(height = size.height * 0.4f)
          )
        }
        .padding(20.dp)
        .testTag("start_call_dialog")
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .background(Color(0xCC0E1A2C), CircleShape)
                .border(1.dp, rgbBrush, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PhoneForwarded,
                contentDescription = null,
                tint = RgbNeonGreen,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            MirrorText(
              text = "Start a Call",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "Select contact for encrypted voice or video call",
          fontSize = 12.sp,
          color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
        ) {
          items(allContacts) { contact ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x2015243B), RoundedCornerShape(12.dp))
                .border(1.dp, MirrorBorderSubtle, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              UserAvatar(
                avatarUrl = contact.avatarUrl,
                name = contact.name,
                size = 40.dp,
                isOnline = contact.isOnline,
                showOnlineBadge = true
              )

              Spacer(modifier = Modifier.width(10.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  MirrorText(text = contact.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                  if (contact.isOnline) {
                    Spacer(modifier = Modifier.width(4.dp))
                    PresencePulseDot(isOnline = true, size = 6.dp)
                  }
                }
                Text(
                  text = if (contact.isOnline) "Online" else "Last seen ${contact.lastSeen}",
                  fontSize = 11.sp,
                  color = if (contact.isOnline) RgbNeonGreen else Color(0xFF64748B)
                )
              }

              // Voice Call Action
              IconButton(
                onClick = {
                  onDismiss()
                  repository.startCall(contact, isVideo = false)
                },
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(0x30152238), CircleShape)
                  .border(1.dp, RgbNeonGreen.copy(alpha = 0.5f), CircleShape)
              ) {
                Icon(
                  imageVector = Icons.Default.Call,
                  contentDescription = "Voice Call",
                  tint = RgbNeonGreen,
                  modifier = Modifier.size(18.dp)
                )
              }

              Spacer(modifier = Modifier.width(6.dp))

              // Video Call Action
              IconButton(
                onClick = {
                  onDismiss()
                  repository.startCall(contact, isVideo = true)
                },
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(0x30152238), CircleShape)
                  .border(1.dp, RgbNeonCyan.copy(alpha = 0.5f), CircleShape)
              ) {
                Icon(
                  imageVector = Icons.Default.Videocam,
                  contentDescription = "Video Call",
                  tint = RgbNeonCyan,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
