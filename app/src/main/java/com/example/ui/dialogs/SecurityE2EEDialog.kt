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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.MirrorText
import com.example.ui.components.rememberAnimatedRgbBrush
import com.example.ui.theme.MirrorBorderSubtle
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen
import com.example.ui.theme.RgbNeonPink

@Composable
fun SecurityE2EEDialog(
  contactName: String,
  onDismiss: () -> Unit
) {
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
        .testTag("security_e2ee_dialog")
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
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
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = RgbNeonCyan,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            MirrorText(
              text = "Verify Security Code",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulated QR Code box with mirror glass styling
        Box(
          modifier = Modifier
            .size(140.dp)
            .border(1.5.dp, rgbBrush, RoundedCornerShape(14.dp))
            .background(Color(0x4015243B), RoundedCornerShape(14.dp))
            .drawWithContent {
              drawContent()
              drawRect(
                brush = Brush.verticalGradient(
                  listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)
                ),
                size = size.copy(height = size.height * 0.45f)
              )
            },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.QrCode,
            contentDescription = "QR Code for verification",
            tint = Color.White,
            modifier = Modifier.size(100.dp)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Scan the code on $contactName's device, or compare the 60-digit number below to verify end-to-end encryption.",
          fontSize = 12.sp,
          color = Color(0xFF94A3B8),
          textAlign = TextAlign.Center,
          lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 60-digit security code matrix
        val blocks = listOf(
          "28491", "09382", "58193", "74829",
          "10928", "38472", "91827", "47382",
          "62519", "01928", "47362", "81920"
        )

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x3015243B), RoundedCornerShape(10.dp))
            .border(1.dp, MirrorBorderSubtle, RoundedCornerShape(10.dp))
            .drawWithContent {
              drawContent()
              drawRect(
                brush = Brush.verticalGradient(
                  listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                ),
                size = size.copy(height = size.height * 0.5f)
              )
            }
            .padding(10.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          for (row in 0 until 3) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
              horizontalArrangement = Arrangement.SpaceEvenly
            ) {
              for (col in 0 until 4) {
                val index = row * 4 + col
                Text(
                  text = blocks[index],
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = RgbNeonCyan
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.VerifiedUser,
            contentDescription = null,
            tint = RgbNeonGreen,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "E2EE Verified via Firebase Studio Signal Protocol",
            fontSize = 11.sp,
            color = RgbNeonGreen,
            fontWeight = FontWeight.SemiBold
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = onDismiss,
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
                  listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)
                ),
                size = size.copy(height = size.height * 0.5f)
              )
            }
            .testTag("verify_done_button")
        ) {
          MirrorText("Done", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
