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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.ui.components.MirrorText
import com.example.ui.components.PresencePulseDot
import com.example.ui.components.UserAvatar
import com.example.ui.components.rememberAnimatedRgbBrush
import com.example.ui.theme.MirrorBorderSubtle
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen
import com.example.ui.theme.RgbNeonPink

@Composable
fun NewGroupDialog(
  repository: ChatRepository,
  onDismiss: () -> Unit,
  onGroupCreated: () -> Unit
) {
  var groupName by remember { mutableStateOf("") }
  val chats by repository.chats.collectAsState()
  val allContacts = chats.flatMap { it.participants }
    .filter { it.id != repository.currentUser.value.id }
    .distinctBy { it.id }

  val selectedUserIds = remember { mutableStateListOf<String>() }
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
        .testTag("new_group_dialog")
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
                imageVector = Icons.Default.Group,
                contentDescription = null,
                tint = RgbNeonCyan,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            MirrorText(
              text = "New Group Chat",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = groupName,
          onValueChange = { groupName = it },
          label = { Text("Group Subject", color = Color(0xFF94A3B8)) },
          placeholder = { Text("e.g. Firebase Devs", color = Color(0xFF64748B)) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = RgbNeonCyan,
            unfocusedBorderColor = MirrorBorderSubtle
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("group_name_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        MirrorText(
          text = "Select Participants (${selectedUserIds.size} selected)",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
        ) {
          items(allContacts) { contact ->
            val isSelected = selectedUserIds.contains(contact.id)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  if (isSelected) selectedUserIds.remove(contact.id) else selectedUserIds.add(contact.id)
                }
                .padding(vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              UserAvatar(
                avatarUrl = contact.avatarUrl,
                name = contact.name,
                size = 38.dp,
                showOnlineBadge = true,
                isOnline = contact.isOnline
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  MirrorText(
                    text = contact.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                  )
                  if (contact.isOnline) {
                    Spacer(modifier = Modifier.width(6.dp))
                    PresencePulseDot(isOnline = true, size = 6.dp)
                  }
                }
                Text(
                  text = if (contact.isOnline) "online • ${contact.about}" else contact.about,
                  fontSize = 11.sp,
                  color = if (contact.isOnline) RgbNeonGreen else Color(0xFF94A3B8),
                  maxLines = 1
                )
              }
              Checkbox(
                checked = isSelected,
                onCheckedChange = { checked ->
                  if (checked) selectedUserIds.add(contact.id) else selectedUserIds.remove(contact.id)
                },
                colors = CheckboxDefaults.colors(
                  checkedColor = RgbNeonCyan,
                  checkmarkColor = Color.Black,
                  uncheckedColor = MirrorBorderSubtle
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            if (groupName.isNotBlank() && selectedUserIds.isNotEmpty()) {
              repository.createGroupChat(groupName.trim(), selectedUserIds.toList())
              onGroupCreated()
            }
          },
          enabled = groupName.isNotBlank() && selectedUserIds.isNotEmpty(),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xCC0E1A2C),
            disabledContainerColor = Color(0x30152238)
          ),
          shape = RoundedCornerShape(24.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.5.dp, if (groupName.isNotBlank() && selectedUserIds.isNotEmpty()) rgbBrush else Brush.linearGradient(listOf(MirrorBorderSubtle, MirrorBorderSubtle)), RoundedCornerShape(24.dp))
            .drawWithContent {
              drawContent()
              if (groupName.isNotBlank() && selectedUserIds.isNotEmpty()) {
                drawRect(
                  brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)
                  ),
                  size = size.copy(height = size.height * 0.5f)
                )
              }
            }
            .testTag("create_group_button")
        ) {
          Icon(Icons.Default.Check, contentDescription = null, tint = if (groupName.isNotBlank() && selectedUserIds.isNotEmpty()) RgbNeonCyan else Color(0xFF64748B), modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          MirrorText("Create Group", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
