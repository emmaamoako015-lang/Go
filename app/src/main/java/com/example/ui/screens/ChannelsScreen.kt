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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import com.example.model.Channel
import com.example.ui.components.MirrorText
import com.example.ui.components.UserAvatar
import com.example.ui.components.rememberAnimatedRgbBrush
import com.example.ui.theme.MirrorBorderGlint
import com.example.ui.theme.MirrorBorderSubtle
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen
import com.example.ui.theme.RgbNeonPink
import com.example.ui.theme.RgbNeonYellow
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppTeal

@Composable
fun ChannelsScreen(
  repository: ChatRepository,
  modifier: Modifier = Modifier
) {
  val channels by repository.channels.collectAsState()
  var showCreateChannelDialog by remember { mutableStateOf(false) }
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 4000)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Transparent)
      .testTag("channels_tab_screen")
  ) {
    // Top banner for Channels in sleek mirror finish
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(Color(0x28121A28), RoundedCornerShape(16.dp))
        .border(1.dp, MirrorBorderSubtle, RoundedCornerShape(16.dp))
        .drawWithContent {
          drawContent()
          drawRect(
            brush = Brush.verticalGradient(
              listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
            ),
            size = size.copy(height = size.height * 0.4f)
          )
        }
        .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          MirrorText(
            text = "Stay updated on topics you care about",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Follow verified channels or broadcast your updates",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Button(
          onClick = { showCreateChannelDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC0E1A2C)),
          shape = RoundedCornerShape(18.dp),
          modifier = Modifier
            .border(1.5.dp, rgbBrush, RoundedCornerShape(18.dp))
            .drawWithContent {
              drawContent()
              drawRect(
                brush = Brush.verticalGradient(
                  listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)
                ),
                size = size.copy(height = size.height * 0.5f)
              )
            }
            .testTag("create_channel_btn")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, tint = RgbNeonCyan, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          MirrorText("New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
      items(channels) { channel ->
        ChannelItemCard(
          channel = channel,
          onToggleFollow = { repository.toggleChannelFollow(channel.id) },
          onLikePost = { postId -> repository.likeChannelPost(channel.id, postId) }
        )
        Spacer(modifier = Modifier.height(10.dp))
      }
    }
  }

  if (showCreateChannelDialog) {
    CreateChannelDialog(
      onDismiss = { showCreateChannelDialog = false },
      onCreate = { name, desc ->
        repository.createChannel(name, desc)
        showCreateChannelDialog = false
      }
    )
  }
}

@Composable
fun ChannelItemCard(
  channel: Channel,
  onToggleFollow: () -> Unit,
  onLikePost: (String) -> Unit
) {
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 4200)

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(Color(0x25121A28), RoundedCornerShape(16.dp))
      .border(
        width = 1.dp,
        brush = Brush.linearGradient(
          listOf(
            MirrorBorderGlint.copy(alpha = 0.45f),
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
            listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
          ),
          size = size.copy(height = size.height * 0.4f)
        )
      }
      .padding(14.dp)
  ) {
    Column {
      // Channel Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        UserAvatar(avatarUrl = channel.avatarUrl, name = channel.name, size = 44.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            MirrorText(
              text = channel.name,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
            if (channel.verified) {
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "Verified Channel",
                tint = RgbNeonCyan,
                modifier = Modifier.size(16.dp)
              )
            }
          }
          Text(
            text = channel.followerCountFormatted,
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
          )
        }

        if (channel.isFollowing) {
          OutlinedButton(
            onClick = onToggleFollow,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = RgbNeonCyan),
            modifier = Modifier
              .border(1.dp, RgbNeonCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
              .drawWithContent {
                drawContent()
                drawRect(
                  brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.18f), Color.Transparent)
                  ),
                  size = size.copy(height = size.height * 0.45f)
                )
              }
          ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = RgbNeonCyan, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Following", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }
        } else {
          Button(
            onClick = onToggleFollow,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC0E1A2C)),
            modifier = Modifier
              .border(1.2.dp, rgbBrush, RoundedCornerShape(16.dp))
              .drawWithContent {
                drawContent()
                drawRect(
                  brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)
                  ),
                  size = size.copy(height = size.height * 0.45f)
                )
              }
          ) {
            MirrorText("Follow", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = channel.description,
        fontSize = 12.sp,
        color = Color(0xFF94A3B8),
        lineHeight = 16.sp
      )

      // Latest Post
      channel.posts.firstOrNull()?.let { post ->
        Spacer(modifier = Modifier.height(10.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x30152238), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = post.text,
              fontSize = 13.sp,
              color = Color.White,
              lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = post.formattedTime,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
              )
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clickable { onLikePost(post.id) }
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (post.hasLiked) Color(0x30FF0055) else Color(0x20FFFFFF), RoundedCornerShape(12.dp))
                  .border(1.dp, if (post.hasLiked) RgbNeonPink.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(12.dp))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(
                  imageVector = if (post.hasLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                  contentDescription = "Like",
                  tint = if (post.hasLiked) RgbNeonPink else Color(0xFF94A3B8),
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${post.likesCount}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = if (post.hasLiked) Color.White else Color(0xFF94A3B8)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun CreateChannelDialog(
  onDismiss: () -> Unit,
  onCreate: (String, String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 3500)

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(22.dp))
        .background(Color(0xE60D1526), RoundedCornerShape(22.dp))
        .border(1.5.dp, rgbBrush, RoundedCornerShape(22.dp))
        .drawWithContent {
          drawContent()
          drawRect(
            brush = Brush.verticalGradient(
              listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)
            ),
            size = size.copy(height = size.height * 0.35f)
          )
        }
        .padding(20.dp)
        .testTag("create_channel_dialog")
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
                .size(36.dp)
                .background(Color(0x3300FFCC), CircleShape)
                .border(1.dp, RgbNeonCyan, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = RgbNeonCyan,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            MirrorText(
              text = "Create Broadcast Channel",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Channels allow you to reach unlimited audiences with announcements, updates, and media broadcasts.",
          fontSize = 12.sp,
          color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Channel Name", color = Color(0xFF94A3B8)) },
          placeholder = { Text("e.g. Firebase AI Developers", color = Color(0xFF64748B)) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = RgbNeonCyan,
            unfocusedBorderColor = Color(0x30FFFFFF),
            focusedLabelColor = RgbNeonCyan
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("channel_name_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Channel Description", color = Color(0xFF94A3B8)) },
          placeholder = { Text("Describe what updates you will share", color = Color(0xFF64748B)) },
          maxLines = 3,
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = RgbNeonCyan,
            unfocusedBorderColor = Color(0x30FFFFFF),
            focusedLabelColor = RgbNeonCyan
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("channel_desc_input")
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = {
            if (name.isNotBlank()) {
              onCreate(name.trim(), description.trim())
            }
          },
          enabled = name.isNotBlank(),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC0E1A2C)),
          shape = RoundedCornerShape(24.dp),
          modifier = Modifier
            .fillMaxWidth()
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
            .testTag("submit_create_channel_btn")
        ) {
          MirrorText("Create Channel", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
