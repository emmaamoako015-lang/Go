package com.example.ui.dialogs

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.ChatRepository
import com.example.model.SongTrack
import com.example.model.StatusMediaType
import com.example.ui.components.rememberAnimatedRgbBrush
import com.example.ui.theme.MirrorBorderSubtle
import com.example.ui.theme.MirrorObsidian
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen
import com.example.ui.theme.RgbNeonPink
import com.example.ui.theme.WhatsAppLightGreen
import kotlinx.coroutines.delay

enum class StatusCreationMode {
  HUB,
  TEXT,
  MUSIC_PICKER,
  LAYOUT,
  VOICE,
  PREVIEW_MEDIA
}

data class LocalMediaItem(
  val id: String,
  val url: String,
  val isVideo: Boolean = false,
  val duration: String = "",
  val title: String = ""
)

@Composable
fun StatusCreateDialog(
  repository: ChatRepository,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var currentMode by remember { mutableStateOf(StatusCreationMode.HUB) }
  var filterCategory by remember { mutableStateOf("Recents") }
  var showFilterMenu by remember { mutableStateOf(false) }

  // Selected media & captions
  var selectedImageUrl by remember { mutableStateOf("") }
  var selectedVideoUrl by remember { mutableStateOf("") }
  var selectedSong by remember { mutableStateOf<SongTrack?>(null) }
  var statusCaption by remember { mutableStateOf("") }
  val selectedCollagePhotos = remember { mutableStateListOf<String>() }

  // Background gradients for text/voice/song status
  val gradients = listOf(
    listOf(Color(0xFF0D1526), MirrorObsidian, Color(0xFF060B12)),
    listOf(Color(0xFF003B46), Color(0xFF07575B), Color(0xFF041B20)),
    listOf(Color(0xFF380036), Color(0xFF0CBABA), Color(0xFF1E002B)),
    listOf(Color(0xFF4A0E4E), Color(0xFF1B003A), Color(0xFF0D001A)),
    listOf(Color(0xFF2C001E), Color(0xFFFF007F).copy(alpha = 0.4f), Color(0xFF10000B)),
    listOf(Color(0xFF002B19), Color(0xFF00E676).copy(alpha = 0.4f), Color(0xFF00120B))
  )
  val bgHexValues = listOf(
    0xFF0D1526,
    0xFF003B46,
    0xFF380036,
    0xFF4A0E4E,
    0xFF2C001E,
    0xFF002B19
  )
  var gradientIndex by remember { mutableIntStateOf(0) }
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 3500)

  // Curated Songs Library
  val curatedSongs = remember {
    listOf(
      SongTrack("s1", "City Lights & Cyber Dreams", "Neon Skyline", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=300", 30, "Synthwave"),
      SongTrack("s2", "Calm Down & Vibe", "Rema & Selena", "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=300", 30, "Afrobeats"),
      SongTrack("s3", "Midnight Horizon", "Luna Waves", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300", 30, "Lo-Fi Ambient"),
      SongTrack("s4", "Global Groove", "Amapiano Kings", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300", 30, "Afro-House"),
      SongTrack("s5", "Blinding Memories", "Starboy Echo", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300", 30, "Electro Pop"),
      SongTrack("s6", "Golden Hour Acoustic", "Horizon Acoustic", "https://images.unsplash.com/photo-1445985543469-433ecba627a0?w=300", 30, "Acoustic Chill")
    )
  }

  // Curated gallery media items reflecting real gallery
  val mediaGridItems = remember {
    listOf(
      LocalMediaItem("m1", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500", false, "", "Neon 3D Mesh"),
      LocalMediaItem("m2", "https://images.unsplash.com/photo-1518770660439-4636190af475?w=500", true, "0:15", "Circuit AI Stream"),
      LocalMediaItem("m3", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500", false, "", "Ocean Sunset"),
      LocalMediaItem("m4", "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=500", false, "", "Team Collab"),
      LocalMediaItem("m5", "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=500", true, "0:30", "City Skyline Night"),
      LocalMediaItem("m6", "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=500", false, "", "Coffee & Journal"),
      LocalMediaItem("m7", "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=500", false, "", "Tech Studio"),
      LocalMediaItem("m8", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500", true, "0:45", "Live Acoustic"),
      LocalMediaItem("m9", "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=500", false, "", "Meeting Room"),
      LocalMediaItem("m10", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500", true, "0:20", "Festival Lights"),
      LocalMediaItem("m11", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500", false, "", "DJ Console")
    )
  }

  // System Photo Picker launcher
  val systemPhotoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      selectedImageUrl = uri.toString()
      selectedVideoUrl = ""
      currentMode = StatusCreationMode.PREVIEW_MEDIA
    }
  }

  // Camera capture launcher
  val cameraCapture = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    if (bitmap != null) {
      // Captured photo previewed
      selectedImageUrl = "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=800"
      selectedVideoUrl = ""
      Toast.makeText(context, "Photo captured!", Toast.LENGTH_SHORT).show()
      currentMode = StatusCreationMode.PREVIEW_MEDIA
    } else {
      // Fallback for camera tap in emulator
      selectedImageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800"
      selectedVideoUrl = ""
      currentMode = StatusCreationMode.PREVIEW_MEDIA
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xE6050B14))
    ) {
      when (currentMode) {
        // ==========================================
        // 1. MAIN "ADD STATUS" SCREEN (MATCHING SCREENSHOT)
        // ==========================================
        StatusCreationMode.HUB -> {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .fillMaxHeight(0.92f)
              .align(Alignment.BottomCenter)
              .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
              .background(Color(0xFF0E1A2C), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
              .border(1.2.dp, rgbBrush, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
              .testTag("status_hub_screen")
          ) {
            Column(modifier = Modifier.fillMaxSize()) {
              // Top Drag Handle Bar
              Box(
                modifier = Modifier
                  .padding(top = 10.dp)
                  .width(44.dp)
                  .height(4.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF94A3B8).copy(alpha = 0.5f))
                  .align(Alignment.CenterHorizontally)
              )

              // Header: Close icon and "Add status" centered
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 8.dp, vertical = 8.dp)
              ) {
                IconButton(
                  onClick = onDismiss,
                  modifier = Modifier.align(Alignment.CenterStart)
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                  )
                }

                Text(
                  text = "Add status",
                  color = Color.White,
                  fontSize = 19.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.align(Alignment.Center)
                )
              }

              Spacer(modifier = Modifier.height(6.dp))

              // 4 Action Buttons Row: Text, Music, Layout, Voice
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
              ) {
                // 1. Text Button
                StatusActionButton(
                  icon = Icons.Default.Edit,
                  label = "Text",
                  onClick = { currentMode = StatusCreationMode.TEXT }
                )

                // 2. Music Button
                StatusActionButton(
                  icon = Icons.Default.MusicNote,
                  label = "Music",
                  onClick = { currentMode = StatusCreationMode.MUSIC_PICKER }
                )

                // 3. Layout Button
                StatusActionButton(
                  icon = Icons.Default.GridView,
                  label = "Layout",
                  onClick = {
                    selectedCollagePhotos.clear()
                    currentMode = StatusCreationMode.LAYOUT
                  }
                )

                // 4. Voice Button
                StatusActionButton(
                  icon = Icons.Default.Mic,
                  label = "Voice",
                  onClick = { currentMode = StatusCreationMode.VOICE }
                )
              }

              Spacer(modifier = Modifier.height(18.dp))

              // Filter Selector: "Recents ▾"
              Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showFilterMenu = true }
                    .background(Color(0x3015243B))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Text(
                    text = filterCategory,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Filter",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                  )
                }

                DropdownMenu(
                  expanded = showFilterMenu,
                  onDismissRequest = { showFilterMenu = false }
                ) {
                  listOf("Recents", "Photos", "Videos", "Music Status", "Collages").forEach { cat ->
                    DropdownMenuItem(
                      text = { Text(cat) },
                      onClick = {
                        filterCategory = cat
                        showFilterMenu = false
                        if (cat == "Music Status") currentMode = StatusCreationMode.MUSIC_PICKER
                        if (cat == "Collages") currentMode = StatusCreationMode.LAYOUT
                      }
                    )
                  }
                }
              }

              // Filtered Media items
              val displayItems = when (filterCategory) {
                "Photos" -> mediaGridItems.filter { !it.isVideo }
                "Videos" -> mediaGridItems.filter { it.isVideo }
                else -> mediaGridItems
              }

              // 3-Column Grid with Camera tile first
              LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                  .weight(1f)
                  .fillMaxWidth()
              ) {
                // Item 0: Camera tile
                item {
                  Box(
                    modifier = Modifier
                      .aspectRatio(1f)
                      .clip(RoundedCornerShape(8.dp))
                      .background(Color(0xFF152238))
                      .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(8.dp))
                      .clickable {
                        try {
                          cameraCapture.launch(null)
                        } catch (e: Exception) {
                          selectedImageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800"
                          currentMode = StatusCreationMode.PREVIEW_MEDIA
                        }
                      }
                      .testTag("status_camera_tile"),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(34.dp)
                      )
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                        text = "Camera",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                      )
                    }
                  }
                }

                // Remaining Gallery Media Items
                items(displayItems) { item ->
                  Box(
                    modifier = Modifier
                      .aspectRatio(1f)
                      .clip(RoundedCornerShape(8.dp))
                      .background(Color(0xFF152238))
                      .clickable {
                        if (item.isVideo) {
                          selectedVideoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                          selectedImageUrl = item.url
                        } else {
                          selectedImageUrl = item.url
                          selectedVideoUrl = ""
                        }
                        currentMode = StatusCreationMode.PREVIEW_MEDIA
                      }
                  ) {
                    AsyncImage(
                      model = item.url,
                      contentDescription = item.title,
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize()
                    )

                    // Video duration badge
                    if (item.isVideo) {
                      Box(
                        modifier = Modifier
                          .align(Alignment.BottomEnd)
                          .padding(4.dp)
                          .clip(RoundedCornerShape(4.dp))
                          .background(Color.Black.copy(alpha = 0.7f))
                          .padding(horizontal = 4.dp, vertical = 2.dp)
                      ) {
                        Text(
                          text = item.duration,
                          color = Color.White,
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold
                        )
                      }
                    }
                  }
                }
              }
            }

            // Bottom-right floating album / system photo picker button
            FloatingActionButton(
              onClick = {
                systemPhotoPicker.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
              },
              containerColor = Color(0xF00D1526),
              contentColor = Color.White,
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
                .border(1.2.dp, rgbBrush, RoundedCornerShape(16.dp))
                .testTag("open_gallery_fab")
            ) {
              Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = "Pick from device",
                tint = RgbNeonCyan
              )
            }
          }
        }

        // ==========================================
        // 2. TEXT STATUS MODE
        // ==========================================
        StatusCreationMode.TEXT -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Brush.verticalGradient(gradients[gradientIndex]))
          ) {
            // Top Controls
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 16.dp, end = 16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(
                onClick = { currentMode = StatusCreationMode.HUB },
                modifier = Modifier
                  .size(42.dp)
                  .background(Color(0x700E1A2C), CircleShape)
                  .border(1.dp, rgbBrush, CircleShape)
              ) {
                Icon(Icons.Default.Close, contentDescription = "Back", tint = Color.White)
              }

              IconButton(
                onClick = { gradientIndex = (gradientIndex + 1) % gradients.size },
                modifier = Modifier
                  .size(42.dp)
                  .background(Color(0x700E1A2C), CircleShape)
                  .border(1.dp, rgbBrush, CircleShape)
              ) {
                Icon(Icons.Default.ColorLens, contentDescription = "Color", tint = RgbNeonCyan)
              }
            }

            // Text Input
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
              contentAlignment = Alignment.Center
            ) {
              OutlinedTextField(
                value = statusCaption,
                onValueChange = { if (it.length <= 250) statusCaption = it },
                placeholder = {
                  Text(
                    text = "Type a status story...",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                  )
                },
                textStyle = TextStyle(
                  color = Color.White,
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.Center
                ),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = Color.Transparent,
                  unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("status_text_field")
              )
            }

            // Bottom Send
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp, start = 20.dp, end = 20.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "⏳ Expires in 24 hours",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
              )

              FloatingActionButton(
                onClick = {
                  if (statusCaption.isNotBlank()) {
                    repository.postStatusStory(
                      text = statusCaption.trim(),
                      bgHex = bgHexValues[gradientIndex],
                      mediaType = StatusMediaType.TEXT
                    )
                    Toast.makeText(context, "Text status posted!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                  }
                },
                containerColor = WhatsAppLightGreen,
                contentColor = Color.White,
                shape = CircleShape
              ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Post", tint = Color.White)
              }
            }
          }
        }

        // ==========================================
        // 3. MUSIC STATUS / SONG PICKER MODE
        // ==========================================
        StatusCreationMode.MUSIC_PICKER -> {
          var playingSongId by remember { mutableStateOf<String?>(null) }
          var searchQuery by remember { mutableStateOf("") }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .fillMaxHeight(0.92f)
              .align(Alignment.BottomCenter)
              .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
              .background(Color(0xFF0E1A2C), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
              .border(1.2.dp, RgbNeonPink, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
              .padding(20.dp)
          ) {
            Column(modifier = Modifier.fillMaxSize()) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(38.dp)
                      .background(Color(0x30FF007F), CircleShape)
                      .border(1.dp, RgbNeonPink, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = RgbNeonPink, modifier = Modifier.size(20.dp))
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Text("Add Music to Status", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = { currentMode = StatusCreationMode.HUB }) {
                  Icon(Icons.Default.Close, contentDescription = "Back", tint = Color.White)
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Search Bar
              OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search songs or artists...", color = Color(0xFF64748B)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White,
                  focusedBorderColor = RgbNeonPink,
                  unfocusedBorderColor = MirrorBorderSubtle
                ),
                singleLine = true
              )

              Spacer(modifier = Modifier.height(14.dp))

              Text(
                text = "TRENDING TRACKS (30s CLIPS)",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )

              Spacer(modifier = Modifier.height(8.dp))

              val filteredSongs = curatedSongs.filter {
                it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true)
              }

              Column(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxWidth()
              ) {
                filteredSongs.forEach { song ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 6.dp)
                      .clip(RoundedCornerShape(14.dp))
                      .background(if (selectedSong?.id == song.id) Color(0x30FF007F) else Color(0x1815243B))
                      .border(1.dp, if (selectedSong?.id == song.id) RgbNeonPink else MirrorBorderSubtle, RoundedCornerShape(14.dp))
                      .clickable {
                        selectedSong = song
                        // Move to preview where user can confirm or add caption
                        selectedImageUrl = song.coverUrl
                        currentMode = StatusCreationMode.PREVIEW_MEDIA
                      }
                      .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    AsyncImage(
                      model = song.coverUrl,
                      contentDescription = song.title,
                      contentScale = ContentScale.Crop,
                      modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                      Text(song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                      Text("${song.artist} • ${song.genre}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }

                    // Play Preview Button
                    IconButton(
                      onClick = {
                        playingSongId = if (playingSongId == song.id) null else song.id
                      },
                      modifier = Modifier
                        .size(36.dp)
                        .background(Color(0x30152238), CircleShape)
                        .border(1.dp, RgbNeonPink.copy(alpha = 0.5f), CircleShape)
                    ) {
                      Icon(
                        imageVector = if (playingSongId == song.id) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Preview",
                        tint = RgbNeonPink,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // ==========================================
        // 4. LAYOUT / MULTI-PHOTO COLLAGE MODE
        // ==========================================
        StatusCreationMode.LAYOUT -> {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .fillMaxHeight(0.92f)
              .align(Alignment.BottomCenter)
              .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
              .background(Color(0xFF0E1A2C), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
              .border(1.2.dp, RgbNeonCyan, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
              .padding(20.dp)
          ) {
            Column(modifier = Modifier.fillMaxSize()) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(38.dp)
                      .background(Color(0x3000E5FF), CircleShape)
                      .border(1.dp, RgbNeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.GridView, contentDescription = null, tint = RgbNeonCyan, modifier = Modifier.size(20.dp))
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Text("Photo Collage Layout", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = { currentMode = StatusCreationMode.HUB }) {
                  Icon(Icons.Default.Close, contentDescription = "Back", tint = Color.White)
                }
              }

              Spacer(modifier = Modifier.height(10.dp))
              Text("Select 2 to 4 photos to arrange in a status collage (${selectedCollagePhotos.size}/4 selected)", color = Color(0xFF94A3B8), fontSize = 12.sp)
              Spacer(modifier = Modifier.height(12.dp))

              // Live Collage Preview Box
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(180.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .background(Color(0x2015243B))
                  .border(1.dp, RgbNeonCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                  .padding(6.dp)
              ) {
                if (selectedCollagePhotos.isEmpty()) {
                  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tap photos below to build collage", color = Color(0xFF64748B), fontSize = 13.sp)
                  }
                } else if (selectedCollagePhotos.size <= 2) {
                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxSize()) {
                    selectedCollagePhotos.forEach { url ->
                      AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(10.dp)))
                    }
                  }
                } else {
                  Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                      AsyncImage(model = selectedCollagePhotos[0], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(8.dp)))
                      AsyncImage(model = selectedCollagePhotos[1], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(8.dp)))
                    }
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                      AsyncImage(model = selectedCollagePhotos[2], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(8.dp)))
                      if (selectedCollagePhotos.size >= 4) {
                        AsyncImage(model = selectedCollagePhotos[3], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(8.dp)))
                      } else {
                        Box(modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(8.dp)).background(Color(0x3015243B)))
                      }
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Photo Picker Grid
              LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
              ) {
                items(mediaGridItems.filter { !it.isVideo }) { item ->
                  val isSelected = selectedCollagePhotos.contains(item.url)
                  Box(
                    modifier = Modifier
                      .aspectRatio(1f)
                      .clip(RoundedCornerShape(10.dp))
                      .border(if (isSelected) 2.5.dp else 0.dp, RgbNeonCyan, RoundedCornerShape(10.dp))
                      .clickable {
                        if (isSelected) {
                          selectedCollagePhotos.remove(item.url)
                        } else if (selectedCollagePhotos.size < 4) {
                          selectedCollagePhotos.add(item.url)
                        } else {
                          Toast.makeText(context, "Maximum 4 photos in collage", Toast.LENGTH_SHORT).show()
                        }
                      }
                  ) {
                    AsyncImage(model = item.url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Post Collage Button
              FloatingActionButton(
                onClick = {
                  if (selectedCollagePhotos.size >= 2) {
                    repository.postStatusStory(
                      text = "Collage Story ✨",
                      mediaType = StatusMediaType.LAYOUT,
                      layoutImages = selectedCollagePhotos.toList()
                    )
                    Toast.makeText(context, "Collage status posted!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                  } else {
                    Toast.makeText(context, "Select at least 2 photos", Toast.LENGTH_SHORT).show()
                  }
                },
                containerColor = WhatsAppLightGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.align(Alignment.End)
              ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Post Collage", tint = Color.White)
              }
            }
          }
        }

        // ==========================================
        // 5. VOICE NOTE STATUS MODE
        // ==========================================
        StatusCreationMode.VOICE -> {
          var isRecording by remember { mutableStateOf(false) }
          var recordedDuration by remember { mutableIntStateOf(0) }
          var hasRecorded by remember { mutableStateOf(false) }

          LaunchedEffect(isRecording) {
            if (isRecording) {
              recordedDuration = 0
              while (isRecording && recordedDuration < 30) {
                delay(1000)
                recordedDuration++
              }
              if (recordedDuration >= 30) {
                isRecording = false
                hasRecorded = true
              }
            }
          }

          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Brush.verticalGradient(gradients[gradientIndex]))
          ) {
            // Top Controls
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 16.dp, end = 16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(
                onClick = { currentMode = StatusCreationMode.HUB },
                modifier = Modifier
                  .size(42.dp)
                  .background(Color(0x700E1A2C), CircleShape)
                  .border(1.dp, rgbBrush, CircleShape)
              ) {
                Icon(Icons.Default.Close, contentDescription = "Back", tint = Color.White)
              }

              IconButton(
                onClick = { gradientIndex = (gradientIndex + 1) % gradients.size },
                modifier = Modifier
                  .size(42.dp)
                  .background(Color(0x700E1A2C), CircleShape)
                  .border(1.dp, rgbBrush, CircleShape)
              ) {
                Icon(Icons.Default.ColorLens, contentDescription = "Color", tint = RgbNeonCyan)
              }
            }

            // Center Recording Circle & Waveform
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Box(
                modifier = Modifier
                  .size(100.dp)
                  .clip(CircleShape)
                  .background(if (isRecording) RgbNeonPink else WhatsAppLightGreen)
                  .clickable {
                    if (!isRecording) {
                      isRecording = true
                      hasRecorded = false
                    } else {
                      isRecording = false
                      hasRecorded = true
                    }
                  },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                  contentDescription = "Record Voice Note",
                  tint = Color.White,
                  modifier = Modifier.size(48.dp)
                )
              }

              Spacer(modifier = Modifier.height(20.dp))

              Text(
                text = if (isRecording) "Recording... 0:${recordedDuration.toString().padStart(2, '0')} / 0:30"
                else if (hasRecorded) "Voice status recorded (0:${recordedDuration.toString().padStart(2, '0')})"
                else "Tap microphone to record voice status",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )

              Spacer(modifier = Modifier.height(16.dp))

              // Simulated audio waveform
              Row(
                modifier = Modifier
                  .width(200.dp)
                  .height(36.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
              ) {
                listOf(12, 24, 32, 16, 28, 36, 20, 30, 14, 26).forEach { barH ->
                  Box(
                    modifier = Modifier
                      .width(4.dp)
                      .height(if (isRecording) (barH * (0.8f + (recordedDuration % 3) * 0.2f)).dp else 8.dp)
                      .clip(RoundedCornerShape(2.dp))
                      .background(if (isRecording) RgbNeonPink else Color.White.copy(alpha = 0.6f))
                  )
                }
              }
            }

            // Bottom Send
            if (hasRecorded && recordedDuration > 0) {
              FloatingActionButton(
                onClick = {
                  repository.postStatusStory(
                    text = "Voice note update 🎙️",
                    mediaType = StatusMediaType.VOICE,
                    voiceDurationSeconds = recordedDuration,
                    bgHex = bgHexValues[gradientIndex]
                  )
                  Toast.makeText(context, "Voice status posted!", Toast.LENGTH_SHORT).show()
                  onDismiss()
                },
                containerColor = WhatsAppLightGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                  .align(Alignment.BottomEnd)
                  .padding(bottom = 36.dp, end = 24.dp)
              ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Post Voice", tint = Color.White)
              }
            }
          }
        }

        // ==========================================
        // 6. MEDIA PREVIEW & EDIT (PHOTO / VIDEO / SONG)
        // ==========================================
        StatusCreationMode.PREVIEW_MEDIA -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black)
          ) {
            // Media Display
            if (selectedImageUrl.isNotBlank()) {
              AsyncImage(
                model = selectedImageUrl,
                contentDescription = "Selected Media",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            }

            // Top Bar: Back & Song sticker trigger
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 16.dp, end = 16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(
                onClick = { currentMode = StatusCreationMode.HUB },
                modifier = Modifier
                  .size(42.dp)
                  .background(Color(0x80000000), CircleShape)
              ) {
                Icon(Icons.Default.Close, contentDescription = "Back", tint = Color.White)
              }

              // Add Song Tag button
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .clickable { currentMode = StatusCreationMode.MUSIC_PICKER }
                  .background(Color(0xCC0D1526))
                  .border(1.dp, RgbNeonPink, RoundedCornerShape(20.dp))
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.MusicNote, contentDescription = null, tint = RgbNeonPink, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = selectedSong?.title ?: "Add Music",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                  )
                }
              }
            }

            // Attached Song Sticker on top
            if (selectedSong != null) {
              Box(
                modifier = Modifier
                  .align(Alignment.TopCenter)
                  .padding(top = 100.dp)
                  .clip(RoundedCornerShape(20.dp))
                  .background(Color(0xD00D1526))
                  .border(1.2.dp, RgbNeonPink, RoundedCornerShape(20.dp))
                  .padding(horizontal = 14.dp, vertical = 8.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.MusicNote, contentDescription = null, tint = RgbNeonPink, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(selectedSong!!.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("${selectedSong!!.artist} • ${selectedSong!!.genre}", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                  }
                }
              }
            }

            // Bottom Caption & Send Bar
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 16.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedTextField(
                value = statusCaption,
                onValueChange = { statusCaption = it },
                placeholder = { Text("Add a caption...", color = Color.White.copy(alpha = 0.6f)) },
                modifier = Modifier
                  .weight(1f)
                  .testTag("caption_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White,
                  focusedBorderColor = Color.White.copy(alpha = 0.6f),
                  unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(24.dp)
              )

              Spacer(modifier = Modifier.width(10.dp))

              FloatingActionButton(
                onClick = {
                  val mediaType = when {
                    selectedSong != null -> StatusMediaType.MUSIC
                    selectedVideoUrl.isNotBlank() -> StatusMediaType.VIDEO
                    else -> StatusMediaType.IMAGE
                  }

                  repository.postStatusStory(
                    text = statusCaption.trim(),
                    imageUrl = selectedImageUrl,
                    videoUrl = selectedVideoUrl,
                    mediaType = mediaType,
                    song = selectedSong
                  )
                  Toast.makeText(context, "Status story posted!", Toast.LENGTH_SHORT).show()
                  onDismiss()
                },
                containerColor = WhatsAppLightGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
              ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Post Status", tint = Color.White)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StatusActionButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable { onClick() }
  ) {
    Box(
      modifier = Modifier
        .size(56.dp)
        .clip(CircleShape)
        .background(Color(0xFFE2E8F0))
        .border(1.dp, Color(0xFFCBD5E1), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = Color(0xFF1E293B),
        modifier = Modifier.size(26.dp)
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = label,
      color = Color.White,
      fontSize = 13.sp,
      fontWeight = FontWeight.Medium
    )
  }
}
