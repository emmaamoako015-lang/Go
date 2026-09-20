package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ChatRepository
import com.example.model.BackupFrequency
import com.example.ui.components.MirrorText
import com.example.ui.components.rememberAnimatedRgbBrush
import com.example.ui.theme.MirrorBorderSubtle
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen
import com.example.ui.theme.RgbNeonPink

@Composable
fun CloudBackupDialog(
  repository: ChatRepository,
  onDismiss: () -> Unit
) {
  val backupInfo by repository.cloudBackupInfo.collectAsState()
  var backupOverCellular by remember { mutableStateOf(false) }
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 3500)

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(22.dp))
        .background(Color(0xF20D1526), RoundedCornerShape(22.dp))
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
        .testTag("cloud_backup_dialog")
    ) {
      Column {
        // Header
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
                imageVector = Icons.Default.CloudSync,
                contentDescription = null,
                tint = RgbNeonCyan,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              MirrorText(
                text = "Cloud Backup & Sync",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Firebase Studio Multi-Platform",
                fontSize = 12.sp,
                color = RgbNeonCyan,
                fontWeight = FontWeight.Medium
              )
            }
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Connected Firebase Account Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x30152238), RoundedCornerShape(12.dp))
            .border(1.dp, MirrorBorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.MarkEmailRead,
              contentDescription = null,
              tint = RgbNeonGreen,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Connected Firebase Account",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF94A3B8)
              )
              Text(
                text = "emmaamoako015@gmail.com",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = RgbNeonCyan
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sync Devices Status Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x35152238), RoundedCornerShape(12.dp))
            .border(1.dp, MirrorBorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Devices,
              contentDescription = null,
              tint = RgbNeonCyan,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Cross-Platform Real-time Sync",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
              )
              Text(
                text = "Active on Android, iOS & Web through Firebase Studio",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Last Backup Stats
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(text = "Last Backup", fontSize = 12.sp, color = Color(0xFF94A3B8))
            Text(
              text = backupInfo.lastBackupDate,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(text = "Total Size", fontSize = 12.sp, color = Color(0xFF94A3B8))
            Text(
              text = backupInfo.sizeFormatted,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = RgbNeonGreen
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Backup Progress (when backing up)
        if (backupInfo.isBackingUp) {
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Uploading encrypted Firestore & Storage media...",
                fontSize = 12.sp,
                color = RgbNeonCyan,
                fontWeight = FontWeight.Medium
              )
              Text(
                text = "${(backupInfo.progressPercent * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RgbNeonGreen
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
              progress = { backupInfo.progressPercent },
              color = RgbNeonCyan,
              trackColor = Color(0x30152238),
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
            )
          }
        } else {
          Button(
            onClick = { repository.triggerCloudBackup() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC0E1A2C)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .border(1.5.dp, rgbBrush, RoundedCornerShape(24.dp))
              .drawWithContent {
                drawContent()
                drawRect(
                  brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)
                  ),
                  size = size.copy(height = size.height * 0.45f)
                )
              }
              .testTag("backup_now_button")
          ) {
            Icon(
              imageVector = Icons.Default.CloudUpload,
              contentDescription = null,
              tint = RgbNeonCyan,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            MirrorText(text = "Back Up Now", fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Frequency selector
        MirrorText(
          text = "Auto-Backup Frequency",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(BackupFrequency.DAILY, BackupFrequency.WEEKLY, BackupFrequency.MONTHLY, BackupFrequency.OFF).forEach { freq ->
            val isSelected = backupInfo.autoFrequency == freq
            FilterChip(
              selected = isSelected,
              onClick = { repository.setBackupFrequency(freq) },
              label = {
                Text(
                  text = freq.name.lowercase().replaceFirstChar { it.uppercase() },
                  fontSize = 11.sp,
                  color = if (isSelected) Color.Black else Color.White
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RgbNeonCyan,
                containerColor = Color(0x30152238)
              ),
              border = FilterChipDefaults.filterChipBorder(
                borderColor = if (isSelected) RgbNeonCyan else MirrorBorderSubtle,
                enabled = true,
                selected = isSelected
              ),
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier.drawWithContent {
                drawContent()
                if (isSelected) {
                  drawRect(
                    brush = Brush.verticalGradient(
                      listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)
                    ),
                    size = size.copy(height = size.height * 0.5f)
                  )
                }
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // E2EE backup switch
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = RgbNeonCyan,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "End-to-End Encrypted Backup",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
              )
              Text(
                text = "Secured with client-side 64-digit key",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
              )
            }
          }
          Switch(
            checked = backupInfo.isEncrypted,
            onCheckedChange = { repository.toggleBackupEncryption() },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = RgbNeonCyan,
              uncheckedThumbColor = Color(0xFF94A3B8),
              uncheckedTrackColor = Color(0x30152238)
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Backup over cellular
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Back up using cellular data",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.White
            )
            Text(
              text = "Include mobile networks when Wi-Fi is unavailable",
              fontSize = 11.sp,
              color = Color(0xFF94A3B8)
            )
          }
          Switch(
            checked = backupOverCellular,
            onCheckedChange = { backupOverCellular = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = RgbNeonCyan,
              uncheckedThumbColor = Color(0xFF94A3B8),
              uncheckedTrackColor = Color(0x30152238)
            )
          )
        }
      }
    }
  }
}
