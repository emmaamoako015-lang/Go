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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ChatRepository
import com.example.ui.components.MirrorText
import com.example.ui.components.rememberAnimatedRgbBrush
import com.example.ui.theme.MirrorBorderSubtle
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen
import com.example.ui.theme.RgbNeonPink

@Composable
fun AuthPhoneDialog(
  repository: ChatRepository,
  onDismiss: () -> Unit
) {
  var step by remember { mutableStateOf(1) }
  val currentUser = repository.currentUser.value

  var selectedCountryCode by remember { mutableStateOf("+1") }
  var countryMenuExpanded by remember { mutableStateOf(false) }
  var phoneNumber by remember { mutableStateOf(currentUser.phoneNumber.removePrefix("+1 ").replace("-", "")) }
  var otpCode by remember { mutableStateOf("") }
  var profileName by remember { mutableStateOf(currentUser.name) }
  var profileEmail by remember { mutableStateOf(currentUser.email.ifBlank { "emmaamoako015@gmail.com" }) }
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 3500)

  val countries = listOf(
    Pair("United States", "+1"),
    Pair("United Kingdom", "+44"),
    Pair("Ghana", "+233"),
    Pair("India", "+91"),
    Pair("Germany", "+49"),
    Pair("Nigeria", "+234"),
    Pair("Canada", "+1")
  )

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
        .testTag("auth_phone_dialog")
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
                imageVector = if (step == 1) Icons.Default.Phone else Icons.Default.Sms,
                contentDescription = null,
                tint = RgbNeonCyan,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            MirrorText(
              text = when (step) {
                1 -> "Firebase Phone Login"
                2 -> "Verify Phone Number"
                else -> "Profile Info"
              },
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (step) {
          1 -> {
            Text(
              text = "Global Stream will send an SMS message to verify your phone number via Firebase Authentication.",
              fontSize = 13.sp,
              color = Color(0xFF94A3B8),
              lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Country Selector
            Box {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0x30152238), RoundedCornerShape(12.dp))
                  .border(1.dp, MirrorBorderSubtle, RoundedCornerShape(12.dp))
                  .clickable { countryMenuExpanded = true }
                  .padding(horizontal = 14.dp, vertical = 13.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = countries.find { it.second == selectedCountryCode }?.first ?: "Country",
                    fontSize = 14.sp,
                    color = Color.White
                  )
                  Text(
                    text = selectedCountryCode,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = RgbNeonCyan
                  )
                }
              }

              DropdownMenu(
                expanded = countryMenuExpanded,
                onDismissRequest = { countryMenuExpanded = false }
              ) {
                countries.forEach { (name, code) ->
                  DropdownMenuItem(
                    text = { Text("$name ($code)") },
                    onClick = {
                      selectedCountryCode = code
                      countryMenuExpanded = false
                    }
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
              value = phoneNumber,
              onValueChange = { phoneNumber = it },
              label = { Text("Phone Number", color = Color(0xFF94A3B8)) },
              placeholder = { Text("555 0199", color = Color(0xFF64748B)) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = RgbNeonCyan,
                unfocusedBorderColor = MirrorBorderSubtle
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("phone_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
              onClick = { step = 2 },
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
                .testTag("send_otp_button")
            ) {
              MirrorText("Next", fontWeight = FontWeight.Bold)
            }
          }

          2 -> {
            Text(
              text = "Waiting to automatically detect an SMS sent to $selectedCountryCode $phoneNumber. Enter 6-digit code:",
              fontSize = 13.sp,
              color = Color(0xFF94A3B8),
              lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
              value = otpCode,
              onValueChange = {
                if (it.length <= 6) otpCode = it
                if (it.length == 6) {
                  step = 3
                }
              },
              label = { Text("6-Digit Code", color = Color(0xFF94A3B8)) },
              placeholder = { Text("123456", color = Color(0xFF64748B)) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = RgbNeonCyan,
                unfocusedBorderColor = MirrorBorderSubtle
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("otp_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Resend SMS in 0:45",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
              )
              Text(
                text = "Auto-fill 748291",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RgbNeonGreen,
                modifier = Modifier.clickable {
                  otpCode = "748291"
                  step = 3
                }
              )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
              onClick = { step = 3 },
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
                .testTag("verify_otp_button")
            ) {
              MirrorText("Verify Code", fontWeight = FontWeight.Bold)
            }
          }

          3 -> {
            Text(
              text = "Please provide your name and an optional profile picture for your contacts to recognize you.",
              fontSize = 13.sp,
              color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
              value = profileName,
              onValueChange = { profileName = it },
              label = { Text("Your Name", color = Color(0xFF94A3B8)) },
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = RgbNeonCyan,
                unfocusedBorderColor = MirrorBorderSubtle
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_name_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = profileEmail,
              onValueChange = { profileEmail = it },
              label = { Text("Firebase Account Email", color = Color(0xFF94A3B8)) },
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = RgbNeonCyan,
                unfocusedBorderColor = MirrorBorderSubtle
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_email_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
              onClick = {
                repository.updateCurrentUserProfile(
                  name = profileName.ifBlank { "Emma Amoako" },
                  phoneNumber = "$selectedCountryCode $phoneNumber",
                  email = profileEmail.ifBlank { "emmaamoako015@gmail.com" }
                )
                onDismiss()
              },
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
                .testTag("finish_auth_button")
            ) {
              Icon(Icons.Default.Verified, contentDescription = null, tint = RgbNeonGreen, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              MirrorText("Save & Connect", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
