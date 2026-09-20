package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Link
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
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatRepository
import com.example.model.CallRecord
import com.example.model.CallStatus
import com.example.model.CallType
import com.example.ui.components.MirrorText
import com.example.ui.components.PresencePulseDot
import com.example.ui.components.UserAvatar
import com.example.ui.components.rememberAnimatedRgbBrush
import com.example.ui.theme.MirrorBorderGlint
import com.example.ui.theme.MirrorBorderSubtle
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen
import com.example.ui.theme.RgbNeonPink

@Composable
fun CallsScreen(
  repository: ChatRepository,
  modifier: Modifier = Modifier
) {
  val callRecords by repository.callRecords.collectAsState()
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 3800)
  val context = LocalContext.current

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Transparent)
    ) {
    // Create Call Link Section in Mirror Card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp)
          .clip(RoundedCornerShape(16.dp))
          .clickable {
            val callLink = "https://globalstream.app/call/${System.currentTimeMillis()}"
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
              type = "text/plain"
              putExtra(Intent.EXTRA_TEXT, "Join my Global Stream call: $callLink")
            }
            Toast.makeText(context, "Call link generated & ready to share", Toast.LENGTH_SHORT).show()
            context.startActivity(Intent.createChooser(sendIntent, "Share Call Link via"))
          }
          .background(Color(0x28121A28), RoundedCornerShape(16.dp))
          .border(1.dp, MirrorBorderSubtle, RoundedCornerShape(16.dp))
          .drawWithContent {
            drawContent()
            drawRect(
              brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
              ),
              size = size.copy(height = size.height * 0.45f)
            )
          }
          .padding(horizontal = 14.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .background(Color(0xCC0E1A2C), CircleShape)
              .border(1.5.dp, rgbBrush, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Link,
              contentDescription = "Create call link",
              tint = RgbNeonCyan,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            MirrorText(
              text = "Create call link",
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "Share a link for your WebRTC encrypted call",
              fontSize = 13.sp,
              color = Color(0xFF94A3B8)
            )
          }
        }
      }

      MirrorText(
        text = "RECENT CALLS",
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
      )
    }

    items(callRecords) { record ->
      CallRecordItem(
        record = record,
        onStartCall = { isVideo ->
          repository.startCall(record.contact, isVideo)
        }
      )
    }
  }
}

@Composable
fun CallRecordItem(
  record: CallRecord,
  onStartCall: (Boolean) -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 5.dp)
      .clip(RoundedCornerShape(16.dp))
      .clickable { onStartCall(record.callType == CallType.VIDEO) }
      .background(Color(0x1F121A28), RoundedCornerShape(16.dp))
      .border(
        width = 1.dp,
        brush = Brush.linearGradient(
          listOf(
            MirrorBorderGlint.copy(alpha = 0.35f),
            MirrorBorderSubtle,
            Color(0x05FFFFFF)
          )
        ),
        shape = RoundedCornerShape(16.dp)
      )
      .drawWithContent {
        drawContent()
        drawRect(
          brush = Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)
          ),
          size = size.copy(height = size.height * 0.4f)
        )
      }
      .padding(horizontal = 14.dp, vertical = 12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      UserAvatar(
        avatarUrl = record.contact.avatarUrl,
        name = record.contact.name,
        size = 48.dp,
        showOnlineBadge = true,
        isOnline = record.contact.isOnline
      )

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (record.callStatus == CallStatus.MISSED) {
            Text(
              text = record.contact.name,
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold,
              color = RgbNeonPink
            )
          } else {
            MirrorText(
              text = record.contact.name,
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
          if (record.contact.isOnline) {
            Spacer(modifier = Modifier.width(6.dp))
            PresencePulseDot(isOnline = true, size = 6.dp)
          }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          when (record.callStatus) {
            CallStatus.INCOMING -> {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.CallReceived,
                contentDescription = "Incoming Call",
                tint = RgbNeonGreen,
                modifier = Modifier.size(15.dp)
              )
            }
            CallStatus.OUTGOING -> {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.CallMade,
                contentDescription = "Outgoing Call",
                tint = RgbNeonCyan,
                modifier = Modifier.size(15.dp)
              )
            }
            CallStatus.MISSED -> {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.CallMissed,
                contentDescription = "Missed Call",
                tint = RgbNeonPink,
                modifier = Modifier.size(15.dp)
              )
            }
            else -> {}
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${record.formattedTime} (${record.durationFormatted})",
            fontSize = 13.sp,
            color = Color(0xFF94A3B8)
          )
        }
      }

      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(Color(0x30152238))
          .border(1.dp, Color(0x20FFFFFF), CircleShape)
          .drawWithContent {
            drawContent()
            drawRect(
              brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
              ),
              size = size.copy(height = size.height * 0.45f)
            )
          },
        contentAlignment = Alignment.Center
      ) {
        IconButton(
          onClick = { onStartCall(record.callType == CallType.VIDEO) },
          modifier = Modifier.size(40.dp)
        ) {
          Icon(
            imageVector = if (record.callType == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
            contentDescription = "Call",
            tint = if (record.callType == CallType.VIDEO) RgbNeonCyan else RgbNeonGreen,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}
