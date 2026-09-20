package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.components.BouncingDotsIndicator
import com.example.ui.components.DraggableEliteAiIcon
import com.example.ui.components.MirrorIconButton
import com.example.ui.components.MirrorText
import com.example.ui.components.rememberAnimatedMirrorBrush
import com.example.ui.components.rememberAnimatedRgbBrush
import com.example.ui.components.rememberMirrorChromeBrush
import com.example.ui.theme.MirrorBorderGlint
import com.example.ui.theme.MirrorBorderSubtle
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen
import com.example.ui.theme.RgbNeonPink
import com.example.ui.theme.RgbNeonYellow
import com.example.ui.theme.RgbSpectrum
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatRepository
import com.example.model.Chat
import com.example.model.StatusMediaType
import com.example.model.StatusStory
import com.example.ui.components.MessageStatusTick
import com.example.ui.components.PresencePulseDot
import com.example.ui.components.UserAvatar
import com.example.ui.dialogs.AuthPhoneDialog
import com.example.ui.dialogs.CloudBackupDialog
import com.example.ui.dialogs.NewGroupDialog
import com.example.ui.dialogs.StartRealChatDialog
import com.example.ui.dialogs.SecurityE2EEDialog
import com.example.ui.dialogs.StartCallDialog
import com.example.ui.dialogs.StatusCreateDialog
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppTeal

@Composable
fun HomeScreen(
  repository: ChatRepository
) {
  val chats by repository.chats.collectAsState()
  val statusStories by repository.statusStories.collectAsState()
  val activeCall by repository.activeCall.collectAsState()
  val currentUser by repository.currentUser.collectAsState()
  val allMessages by repository.messages.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) } // 0: CHATS, 1: UPDATES, 2: CHANNELS, 3: STREAM, 4: CALLS
  var selectedChatId by remember { mutableStateOf<String?>(null) }
  var viewingStatusStory by remember { mutableStateOf(false) }
  var statusStoryStartIndex by remember { mutableIntStateOf(0) }

  // Dialog states
  var showNewGroupDialog by remember { mutableStateOf(false) }
  var showCloudBackupDialog by remember { mutableStateOf(false) }
  var showAuthPhoneDialog by remember { mutableStateOf(false) }
  var showStartRealChatDialog by remember { mutableStateOf(false) }
  var showSecurityDialog by remember { mutableStateOf(false) }
  var showStatusCreateDialog by remember { mutableStateOf(false) }
  var showStartCallDialog by remember { mutableStateOf(false) }
  var topMenuExpanded by remember { mutableStateOf(false) }
  var isSearching by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }

  val totalUnread = chats.sumOf { it.unreadCount }

  // Dynamic animated RGB spectrum
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 4000)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Transparent)
  ) {
    when {
      activeCall != null -> {
        CallActiveScreen(repository = repository)
      }
      selectedChatId != null -> {
        val activeChat = chats.find { it.id == selectedChatId }
        if (activeChat != null) {
          ChatDetailScreen(
            chat = activeChat,
            repository = repository,
            onBack = { selectedChatId = null }
          )
        } else {
          selectedChatId = null
        }
      }
      viewingStatusStory && statusStories.isNotEmpty() -> {
        StatusViewerScreen(
          stories = statusStories,
          initialIndex = statusStoryStartIndex,
          onClose = { viewingStatusStory = false },
          onReply = { userId, text ->
            val chat = chats.find { it.participants.any { p -> p.id == userId } }
            if (chat != null) {
              repository.sendMessage(chat.id, "Replied to status: $text")
            }
          }
        )
      }
      else -> {
        // Main Home View with WhatsApp Tabs
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .testTag("home_screen")
        ) {
          Column(modifier = Modifier.fillMaxSize()) {
      // Sleek Mirror Top App Bar with Dynamic RGB Lighting & App Icon
      Surface(
        color = Color(0x600B1220),
        modifier = Modifier
          .fillMaxWidth()
          .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
              listOf(MirrorBorderGlint.copy(alpha = 0.65f), Color.Transparent)
            ),
            shape = androidx.compose.ui.graphics.RectangleShape
          )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp)
        ) {
          if (isSearching) {
            // Expanded search bar with mirror glass and RGB border
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(onClick = {
                isSearching = false
                searchQuery = ""
              }) {
                Icon(Icons.Default.Close, contentDescription = "Close search", tint = Color.White)
              }
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(24.dp))
                  .background(Color(0x33FFFFFF))
                  .border(1.2.dp, rgbBrush, RoundedCornerShape(24.dp))
                  .padding(horizontal = 8.dp)
              ) {
                OutlinedTextField(
                  value = searchQuery,
                  onValueChange = { searchQuery = it },
                  placeholder = { Text("Search chats, messages, channels...", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp) },
                  singleLine = true,
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input")
                )
              }
            }
          } else {
            // Standard Mirror Top Bar with App Icon & RGB Glow
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                  painter = painterResource(id = R.drawable.ic_mirror_rgb_launcher_1788945470930),
                  contentDescription = "App Icon",
                  modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.5.dp, rgbBrush, RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  MirrorText(
                    text = "Global Stream",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    animated = true
                  )
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "MIRROR POLISH",
                      color = RgbNeonCyan,
                      fontSize = 8.5.sp,
                      fontWeight = FontWeight.ExtraBold,
                      letterSpacing = 0.8.sp
                    )
                    Text(
                      text = " • ",
                      color = Color(0xFF64748B),
                      fontSize = 8.5.sp
                    )
                    Text(
                      text = currentUser.name,
                      color = RgbNeonGreen,
                      fontSize = 8.5.sp,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                MirrorIconButton(
                  onClick = {
                    showStatusCreateDialog = true
                  },
                  icon = Icons.Default.CameraAlt,
                  contentDescription = "Camera",
                  size = 38.dp
                )

                MirrorIconButton(
                  onClick = { isSearching = true },
                  icon = Icons.Default.Search,
                  contentDescription = "Search",
                  size = 38.dp
                )

                Box {
                  MirrorIconButton(
                    onClick = { topMenuExpanded = true },
                    icon = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    size = 38.dp
                  )

                  DropdownMenu(
                    expanded = topMenuExpanded,
                    onDismissRequest = { topMenuExpanded = false },
                    modifier = Modifier
                      .background(Color(0xF20B1424))
                      .border(1.2.dp, rgbBrush, RoundedCornerShape(16.dp))
                      .padding(vertical = 4.dp)
                  ) {
                    DropdownMenuItem(
                      text = { MirrorText("New group", fontSize = 14.sp) },
                      onClick = {
                        topMenuExpanded = false
                        showNewGroupDialog = true
                      }
                    )
                    DropdownMenuItem(
                      text = { MirrorText("Cloud Backup & Sync", fontSize = 14.sp) },
                      onClick = {
                        topMenuExpanded = false
                        showCloudBackupDialog = true
                      }
                    )
                    DropdownMenuItem(
                      text = { MirrorText("Firebase Account (${currentUser.name})", fontSize = 14.sp) },
                      onClick = {
                        topMenuExpanded = false
                        showAuthPhoneDialog = true
                      }
                    )
                    DropdownMenuItem(
                      text = { MirrorText("E2EE Security Info", fontSize = 14.sp) },
                      onClick = {
                        topMenuExpanded = false
                        showSecurityDialog = true
                      }
                    )
                  }
                }
              }
            }
          }

          // Tabs Row with dynamic RGB indicator
          ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            edgePadding = 8.dp,
            indicator = { tabPositions ->
              Box(
                Modifier
                  .tabIndicatorOffset(tabPositions[selectedTab])
                  .height(3.5.dp)
                  .padding(horizontal = 6.dp)
                  .clip(CircleShape)
                  .background(rgbBrush)
              )
            },
            divider = {}
          ) {
            // Tab 0: CHATS
            Tab(
              selected = selectedTab == 0,
              onClick = { selectedTab = 0 },
              text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (selectedTab == 0) {
                    MirrorText("CHATS", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                  } else {
                    Text("CHATS", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  }
                  if (totalUnread > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                      modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x35FF0055), CircleShape)
                        .border(1.2.dp, rgbBrush, CircleShape)
                        .drawWithContent {
                          drawContent()
                          drawRect(
                            brush = Brush.verticalGradient(
                              listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)
                            ),
                            size = size.copy(height = size.height * 0.5f)
                          )
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = "$totalUnread",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                }
              }
            )

            // Tab 1: UPDATES / STATUS
            Tab(
              selected = selectedTab == 1,
              onClick = { selectedTab = 1 },
              text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (selectedTab == 1) {
                    MirrorText("UPDATES", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                  } else {
                    Text("UPDATES", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  }
                  Spacer(modifier = Modifier.width(4.dp))
                  Box(
                    modifier = Modifier
                      .size(7.dp)
                      .background(RgbNeonGreen, CircleShape)
                      .border(1.dp, Color.White, CircleShape)
                  )
                }
              }
            )

            // Tab 2: CHANNELS
            Tab(
              selected = selectedTab == 2,
              onClick = { selectedTab = 2 },
              text = {
                if (selectedTab == 2) {
                  MirrorText("CHANNELS", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                } else {
                  Text("CHANNELS", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
              }
            )

            // Tab 3: STREAM (Global Video Stream)
            Tab(
              selected = selectedTab == 3,
              onClick = { selectedTab = 3 },
              text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (selectedTab == 3) {
                    MirrorText("STREAM", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                  } else {
                    Text("STREAM", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  }
                  Spacer(modifier = Modifier.width(5.dp))
                  Box(
                    modifier = Modifier
                      .size(7.dp)
                      .background(Color(0xFFFF2B54), CircleShape)
                  )
                }
              }
            )

            // Tab 4: CALLS
            Tab(
              selected = selectedTab == 4,
              onClick = { selectedTab = 4 },
              text = {
                if (selectedTab == 4) {
                  MirrorText("CALLS", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                } else {
                  Text("CALLS", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
              }
            )
          }

          // Dynamic Animated RGB lighting line under the tabs
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(2.5.dp)
              .background(rgbBrush)
          )
        }
      }

      // Tab Content
      when (selectedTab) {
        0 -> {
          // CHATS TAB
          val trimmedQuery = searchQuery.trim()
          val filteredChatResults = remember(chats, allMessages, trimmedQuery) {
            if (trimmedQuery.isEmpty()) {
              chats.map { chat -> ChatSearchResult(chat = chat, matchedSnippet = null, matchedByName = false) }
            } else {
              chats.mapNotNull { chat ->
                // 1. Check contact / group name or participant name match
                val nameMatch = chat.name.contains(trimmedQuery, ignoreCase = true) ||
                  chat.participants.any { it.name.contains(trimmedQuery, ignoreCase = true) }

                // 2. Check last message content match
                val lastMsgMatch = chat.lastMessage?.content?.contains(trimmedQuery, ignoreCase = true) == true

                // 3. Check message content in chat's entire message history
                val matchingHistoryMsg = allMessages[chat.id]?.findLast { msg ->
                  msg.content.contains(trimmedQuery, ignoreCase = true)
                }

                if (nameMatch || lastMsgMatch || matchingHistoryMsg != null) {
                  val snippet = when {
                    lastMsgMatch -> chat.lastMessage?.content
                    matchingHistoryMsg != null -> matchingHistoryMsg.content
                    else -> null
                  }
                  ChatSearchResult(
                    chat = chat,
                    matchedSnippet = snippet,
                    matchedByName = nameMatch
                  )
                } else {
                  null
                }
              }
            }
          }

          Column(modifier = Modifier.fillMaxSize()) {
            // Real-time Search Bar at top of Chat List Screen
            ChatListSearchBar(
              query = searchQuery,
              onQueryChange = { searchQuery = it },
              resultCount = if (trimmedQuery.isNotEmpty()) filteredChatResults.size else null,
              rgbBrush = rgbBrush
            )

            if (trimmedQuery.isNotEmpty() && filteredChatResults.isEmpty()) {
              EmptySearchState(
                query = trimmedQuery,
                onClear = { searchQuery = "" }
              )
            } else {
              LazyColumn(
                modifier = Modifier
                  .fillMaxSize()
                  .testTag("chats_list")
              ) {
                items(filteredChatResults, key = { it.chat.id }) { result ->
                  ChatListItem(
                    chat = result.chat,
                    currentUserId = currentUser.id,
                    onClick = { selectedChatId = result.chat.id },
                    searchQuery = trimmedQuery,
                    matchedSnippet = result.matchedSnippet
                  )
                }

                item {
                  Spacer(modifier = Modifier.height(16.dp))
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.Lock,
                      contentDescription = null,
                      tint = Color(0xFF8696A0),
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Your personal messages are end-to-end encrypted",
                      fontSize = 11.sp,
                      color = Color(0xFF8696A0)
                    )
                  }
                  Spacer(modifier = Modifier.height(60.dp))
                }
              }
            }
          }
        }

        1 -> {
          // STATUS TAB with Mirror Cards and Dynamic RGB Ring
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .testTag("status_list")
          ) {
            // My Status Card
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 6.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .clickable { showStatusCreateDialog = true }
                  .background(Color(0x28121A28), RoundedCornerShape(16.dp))
                  .border(1.dp, MirrorBorderSubtle, RoundedCornerShape(16.dp))
                  .drawWithContent {
                    drawContent()
                    drawRect(
                      brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.10f), Color.Transparent)
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
                  Box {
                    UserAvatar(avatarUrl = currentUser.avatarUrl, name = currentUser.name, size = 52.dp)
                    Box(
                      modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color(0xCC0E1A2C), CircleShape)
                        .border(1.5.dp, rgbBrush, CircleShape),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Status",
                        tint = RgbNeonGreen,
                        modifier = Modifier.size(14.dp)
                      )
                    }
                  }
                  Spacer(modifier = Modifier.width(14.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    MirrorText(
                      text = "My Status",
                      fontSize = 16.sp,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = "Tap to add status update (expires in 24h)",
                      fontSize = 13.sp,
                      color = Color(0xFF94A3B8)
                    )
                  }
                }
              }

              MirrorText(
                text = "RECENT UPDATES",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
              )
            }

            items(statusStories) { story ->
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 5.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .clickable {
                    statusStoryStartIndex = statusStories.indexOf(story)
                    viewingStatusStory = true
                    repository.markStoryViewed(story.id)
                  }
                  .background(Color(0x1C121A28), RoundedCornerShape(16.dp))
                  .border(1.dp, if (!story.isViewed) rgbBrush else androidx.compose.ui.graphics.SolidColor(MirrorBorderSubtle), RoundedCornerShape(16.dp))
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
                  UserAvatar(
                    avatarUrl = story.userAvatar,
                    name = story.userName,
                    size = 52.dp,
                    hasUnviewedStatus = !story.isViewed
                  )
                  Spacer(modifier = Modifier.width(14.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = story.userName,
                      fontSize = 16.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = Color.White
                    )
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(top = 2.dp)
                    ) {
                      when {
                        story.song != null || story.mediaType == StatusMediaType.MUSIC -> {
                          Icon(Icons.Default.MusicNote, contentDescription = null, tint = RgbNeonPink, modifier = Modifier.size(14.dp))
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(story.song?.title ?: "Music track", color = RgbNeonPink, fontSize = 12.sp, maxLines = 1, fontWeight = FontWeight.Medium)
                          Text(" • ${story.formattedTime}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        story.mediaType == StatusMediaType.VOICE -> {
                          Icon(Icons.Default.Mic, contentDescription = null, tint = RgbNeonGreen, modifier = Modifier.size(14.dp))
                          Spacer(modifier = Modifier.width(4.dp))
                          Text("Voice note (0:${story.voiceDurationSeconds.coerceAtLeast(10)})", color = RgbNeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                          Text(" • ${story.formattedTime}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        story.mediaType == StatusMediaType.VIDEO || story.videoUrl.isNotBlank() -> {
                          Icon(Icons.Default.Videocam, contentDescription = null, tint = RgbNeonCyan, modifier = Modifier.size(14.dp))
                          Spacer(modifier = Modifier.width(4.dp))
                          Text("Video status", color = RgbNeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                          Text(" • ${story.formattedTime}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        story.mediaType == StatusMediaType.LAYOUT -> {
                          Icon(Icons.Default.GridView, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(14.dp))
                          Spacer(modifier = Modifier.width(4.dp))
                          Text("Collage layout", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                          Text(" • ${story.formattedTime}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        else -> {
                          Text(
                            text = "${story.formattedTime} • Expires in ${story.expiresHoursLeft}h",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        2 -> {
          // CHANNELS TAB
          ChannelsScreen(repository = repository)
        }

        3 -> {
          // GLOBAL VIDEO STREAM (TikTok-style feed with double-tap heart pop)
          VideoStreamScreen(repository = repository)
        }

        4 -> {
          // CALLS TAB
          CallsScreen(repository = repository)
        }
      }
    }

    // Contextual Floating Action Button with Mirror & Dynamic RGB Border
    Box(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
    ) {
      when (selectedTab) {
        0 -> {
          Column(horizontalAlignment = Alignment.End) {
            // Real chat FAB — looks up an actual other person by phone number via Firestore.
            FloatingActionButton(
              onClick = { showStartRealChatDialog = true },
              containerColor = Color(0xCC152238),
              contentColor = Color.White,
              shape = CircleShape,
              modifier = Modifier
                .size(44.dp)
                .testTag("start_real_chat_fab")
                .border(1.5.dp, MirrorBorderSubtle, CircleShape)
            ) {
              Icon(Icons.Default.PersonSearch, contentDescription = "Find real contact", tint = RgbNeonCyan, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            // New Chat / Group FAB with specular mirror glint and dynamic RGB border
            FloatingActionButton(
              onClick = { showNewGroupDialog = true },
              containerColor = Color(0xCC0E1A2C),
              contentColor = Color.White,
              shape = CircleShape,
              modifier = Modifier
                .testTag("new_chat_fab")
                .border(2.dp, rgbBrush, CircleShape)
                .drawWithContent {
                  drawContent()
                  drawRect(
                    brush = Brush.verticalGradient(
                      listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)
                    ),
                    size = size.copy(height = size.height * 0.45f)
                  )
                }
            ) {
              Icon(Icons.Default.Chat, contentDescription = "New Chat", tint = RgbNeonGreen)
            }
          }
        }
        1 -> {
          // New Status FABs (Pencil & Camera)
          Column(horizontalAlignment = Alignment.End) {
            FloatingActionButton(
              onClick = { showStatusCreateDialog = true },
              containerColor = Color(0xCC152238),
              contentColor = Color.White,
              shape = CircleShape,
              modifier = Modifier
                .size(44.dp)
                .testTag("text_status_fab")
                .border(1.5.dp, MirrorBorderSubtle, CircleShape)
                .drawWithContent {
                  drawContent()
                  drawRect(
                    brush = Brush.verticalGradient(
                      listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                    ),
                    size = size.copy(height = size.height * 0.45f)
                  )
                }
            ) {
              Icon(Icons.Default.Edit, contentDescription = "Text Status", tint = RgbNeonCyan, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            FloatingActionButton(
              onClick = { showStatusCreateDialog = true },
              containerColor = Color(0xCC0E1A2C),
              contentColor = Color.White,
              shape = CircleShape,
              modifier = Modifier
                .testTag("camera_status_fab")
                .border(2.dp, rgbBrush, CircleShape)
                .drawWithContent {
                  drawContent()
                  drawRect(
                    brush = Brush.verticalGradient(
                      listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)
                    ),
                    size = size.copy(height = size.height * 0.45f)
                  )
                }
            ) {
              Icon(Icons.Default.CameraAlt, contentDescription = "Camera Status", tint = RgbNeonGreen)
            }
          }
        }
        2 -> {
          // New Channel FAB
          FloatingActionButton(
            onClick = { /* handled in channels tab */ },
            containerColor = Color(0xCC0E1A2C),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
              .border(2.dp, rgbBrush, CircleShape)
              .drawWithContent {
                drawContent()
                drawRect(
                  brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)
                  ),
                  size = size.copy(height = size.height * 0.45f)
                )
              }
          ) {
            Icon(Icons.Default.Add, contentDescription = "New Channel", tint = RgbNeonCyan)
          }
        }
        3 -> {
          // Stream Tab - immersive full-screen video experience (no blocking FAB)
        }
        4 -> {
          // New Call FAB opens contact call picker
          FloatingActionButton(
            onClick = {
              showStartCallDialog = true
            },
            containerColor = Color(0xCC0E1A2C),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
              .testTag("new_call_fab")
              .border(2.dp, rgbBrush, CircleShape)
              .drawWithContent {
                drawContent()
                drawRect(
                  brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)
                  ),
                  size = size.copy(height = size.height * 0.45f)
                )
              }
          ) {
            Icon(Icons.Default.Call, contentDescription = "New Call", tint = RgbNeonGreen)
          }
        }
      }
    }
  }
      }
    }

    // Universal Iconic Draggable AI - Floats & moves across all platforms/screens!
    DraggableEliteAiIcon(
      repository = repository,
      onOpenChat = { chatId ->
        selectedChatId = chatId
      }
    )
  }

  // Dialogs
  if (showNewGroupDialog) {
    NewGroupDialog(
      repository = repository,
      onDismiss = { showNewGroupDialog = false },
      onGroupCreated = { showNewGroupDialog = false }
    )
  }

  if (showStartRealChatDialog) {
    StartRealChatDialog(
      repository = repository,
      onDismiss = { showStartRealChatDialog = false },
      onChatReady = { chatId ->
        showStartRealChatDialog = false
        selectedChatId = chatId
      }
    )
  }

  if (showCloudBackupDialog) {
    CloudBackupDialog(
      repository = repository,
      onDismiss = { showCloudBackupDialog = false }
    )
  }

  if (showAuthPhoneDialog) {
    AuthPhoneDialog(
      repository = repository,
      onDismiss = { showAuthPhoneDialog = false }
    )
  }

  if (showSecurityDialog) {
    SecurityE2EEDialog(
      contactName = "Contacts & Cloud",
      onDismiss = { showSecurityDialog = false }
    )
  }

  if (showStatusCreateDialog) {
    StatusCreateDialog(
      repository = repository,
      onDismiss = { showStatusCreateDialog = false }
    )
  }

  if (showStartCallDialog) {
    StartCallDialog(
      repository = repository,
      onDismiss = { showStartCallDialog = false }
    )
  }
}

data class ChatSearchResult(
  val chat: Chat,
  val matchedSnippet: String? = null,
  val matchedByName: Boolean = false
)

@Composable
fun ChatListSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  resultCount: Int?,
  rgbBrush: Brush,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(Color(0x28121A28), RoundedCornerShape(20.dp))
        .border(
          width = 1.2.dp,
          brush = if (query.isNotBlank()) rgbBrush else Brush.linearGradient(
            listOf(MirrorBorderGlint.copy(alpha = 0.5f), MirrorBorderSubtle)
          ),
          shape = RoundedCornerShape(20.dp)
        )
        .drawWithContent {
          drawContent()
          drawRect(
            brush = Brush.verticalGradient(
              listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
            ),
            size = size.copy(height = size.height * 0.5f)
          )
        }
        .padding(horizontal = 12.dp, vertical = 2.dp)
        .testTag("chat_search_bar")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Search",
          tint = if (query.isNotBlank()) RgbNeonCyan else Color(0xFF94A3B8),
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
          value = query,
          onValueChange = onQueryChange,
          placeholder = {
            Text(
              text = "Search by contact name or message...",
              color = Color(0xFF94A3B8),
              fontSize = 14.sp
            )
          },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = RgbNeonCyan
          ),
          modifier = Modifier
            .weight(1f)
            .testTag("chat_search_input")
        )
        if (query.isNotEmpty()) {
          IconButton(
            onClick = { onQueryChange("") },
            modifier = Modifier
              .size(32.dp)
              .testTag("chat_search_clear_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Clear search",
              tint = Color(0xFF94A3B8),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }

    if (resultCount != null) {
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (resultCount == 0) "No matching conversations" else "$resultCount conversation${if (resultCount > 1) "s" else ""} found",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = if (resultCount == 0) Color(0xFFFF6B81) else RgbNeonGreen
        )
        Text(
          text = "Real-time filter",
          fontSize = 11.sp,
          color = Color(0xFF64748B)
        )
      }
    }
  }
}

@Composable
fun EmptySearchState(
  query: String,
  onClear: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 48.dp, horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(Color(0x22FFFFFF), CircleShape)
        .border(1.dp, MirrorBorderSubtle, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Search,
        contentDescription = null,
        tint = Color(0xFF94A3B8),
        modifier = Modifier.size(32.dp)
      )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = "No results found for \"$query\"",
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Check the spelling or try searching for another contact name or message keyword",
      fontSize = 13.sp,
      color = Color(0xFF94A3B8),
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(18.dp))
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(20.dp))
        .background(Color(0x3310B981), RoundedCornerShape(20.dp))
        .border(1.dp, RgbNeonGreen.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
        .clickable { onClear() }
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .testTag("clear_search_empty_state_button")
    ) {
      Text(
        text = "Clear search",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = RgbNeonGreen
      )
    }
  }
}

@Composable
fun ChatListItem(
  chat: Chat,
  currentUserId: String,
  onClick: () -> Unit,
  searchQuery: String = "",
  matchedSnippet: String? = null
) {
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 4500)
  val isUnread = chat.unreadCount > 0
  val isNameMatched = searchQuery.isNotBlank() && chat.name.contains(searchQuery, ignoreCase = true)

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 5.dp)
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            if (isUnread) Color(0x3C16253C) else Color(0x22121A28),
            if (isUnread) Color(0x22101928) else Color(0x140B101C)
          )
        ),
        shape = RoundedCornerShape(16.dp)
      )
      .border(
        width = if (isUnread) 1.2.dp else 1.dp,
        brush = if (isUnread) rgbBrush else Brush.linearGradient(
          colors = listOf(
            MirrorBorderGlint.copy(alpha = 0.4f),
            MirrorBorderSubtle,
            Color(0x05FFFFFF)
          )
        ),
        shape = RoundedCornerShape(16.dp)
      )
      .drawWithContent {
        drawContent()
        // Top specular mirror gleam
        drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color.White.copy(alpha = 0.08f),
              Color.Transparent
            )
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
      val isChatPartnerOnline = chat.participants.any { it.id != currentUserId && it.isOnline }

      UserAvatar(
        avatarUrl = chat.avatarUrl,
        name = chat.name,
        size = 52.dp,
        showOnlineBadge = true,
        isOnline = isChatPartnerOnline
      )

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (isUnread) {
              MirrorText(
                text = chat.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.weight(1f, fill = false)
              )
            } else {
              Text(
                text = chat.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isNameMatched) RgbNeonCyan else Color.White,
                maxLines = 1,
                modifier = Modifier.weight(1f, fill = false)
              )
            }
            if (isChatPartnerOnline && !chat.isGroup) {
              Spacer(modifier = Modifier.width(6.dp))
              PresencePulseDot(isOnline = true, size = 8.dp)
            }
          }
          chat.lastMessage?.let { last ->
            Text(
              text = last.formattedTime,
              fontSize = 12.sp,
              color = if (chat.unreadCount > 0) RgbNeonGreen else Color(0xFF94A3B8),
              fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Last message snippet OR typing indicator
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            if (chat.typingStatus != null) {
              BouncingDotsIndicator(dotSize = 4.dp, dotColor = RgbNeonCyan, bounceHeight = 2.dp)
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = chat.typingStatus,
                fontSize = 13.sp,
                color = RgbNeonCyan,
                fontWeight = FontWeight.Bold,
                maxLines = 1
              )
            } else {
              chat.lastMessage?.let { last ->
                if (last.senderId == currentUserId) {
                  MessageStatusTick(status = last.status)
                  Spacer(modifier = Modifier.width(4.dp))
                }
                val isMessageMatch = searchQuery.isNotBlank() && last.content.contains(searchQuery, ignoreCase = true)
                Text(
                  text = last.content,
                  fontSize = 13.sp,
                  color = if (isMessageMatch) Color(0xFFE2E8F0) else Color(0xFF94A3B8),
                  fontWeight = if (isMessageMatch) FontWeight.SemiBold else FontWeight.Normal,
                  maxLines = 1
                )
              }
            }
          }

          // Unread badge and pinned pin
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (chat.isPinned) {
              Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = "Pinned",
                tint = RgbNeonYellow,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
            }

            if (chat.unreadCount > 0) {
              Box(
                modifier = Modifier
                  .clip(CircleShape)
                  .background(Color(0x35FF0055), CircleShape)
                  .border(1.2.dp, rgbBrush, CircleShape)
                  .padding(horizontal = 7.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "${chat.unreadCount}",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold
                )
              }
            }
          }
        }

        // Matched message snippet from history if different from last message
        if (searchQuery.isNotBlank() && matchedSnippet != null && (chat.lastMessage == null || !chat.lastMessage.content.contains(searchQuery, ignoreCase = true))) {
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0x2200E5FF))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = RgbNeonCyan,
              modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Matched message: \"$matchedSnippet\"",
              fontSize = 11.sp,
              color = RgbNeonCyan,
              maxLines = 1,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }
  }
}
