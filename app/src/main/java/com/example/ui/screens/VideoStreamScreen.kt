package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ChatRepository
import com.example.model.VideoStreamItem
import com.example.ui.components.UserAvatar
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppTeal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.roundToInt

/**
 * Represents an active heart pop burst on double-tap
 */
private data class HeartPop(
  val id: Long,
  val position: Offset,
  val rotation: Float,
  val scale: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
  val alpha: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
  val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoStreamScreen(
  repository: ChatRepository,
  onShareToChat: (VideoStreamItem) -> Unit = {}
) {
  val videoStreams by repository.videoStreams.collectAsState()
  val chats by repository.chats.collectAsState()
  val currentUser by repository.currentUser.collectAsState()

  var currentFeedType by remember { mutableIntStateOf(1) } // 0: Following, 1: For You
  val filteredVideos = remember(videoStreams, currentFeedType) {
    if (currentFeedType == 0) {
      val following = videoStreams.filter { it.isFollowing }
      if (following.isNotEmpty()) following else videoStreams
    } else {
      videoStreams
    }
  }

  val pagerState = rememberPagerState(pageCount = { filteredVideos.size })
  var isMuted by remember { mutableStateOf(false) }

  // Sheet states
  var commentSheetVideo by remember { mutableStateOf<VideoStreamItem?>(null) }
  var shareSheetVideo by remember { mutableStateOf<VideoStreamItem?>(null) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
      .testTag("video_stream_screen")
  ) {
    if (filteredVideos.isNotEmpty()) {
      VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
      ) { pageIndex ->
        val videoItem = filteredVideos[pageIndex]
        VideoPageItem(
          item = videoItem,
          isMuted = isMuted,
          onToggleMute = { isMuted = !isMuted },
          onDoubleTapLike = { offset ->
            repository.toggleLikeVideo(videoItem.id, forceLike = true)
          },
          onSingleTapLike = {
            repository.toggleLikeVideo(videoItem.id, forceLike = false)
          },
          onToggleFollow = {
            repository.toggleFollowVideoAuthor(videoItem.id)
          },
          onToggleBookmark = {
            repository.toggleBookmarkVideo(videoItem.id)
          },
          onOpenComments = {
            commentSheetVideo = videoItem
          },
          onOpenShare = {
            shareSheetVideo = videoItem
          }
        )
      }
    } else {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Text("No video streams available", color = Color.White)
      }
    }

    // Top Navigation Overlay (Following | For You + Live & Sound)
    TopStreamHeader(
      currentFeedType = currentFeedType,
      onSelectFeed = { currentFeedType = it },
      isMuted = isMuted,
      onToggleMute = { isMuted = !isMuted }
    )

    // Comments Bottom Sheet
    commentSheetVideo?.let { targetVideo ->
      val updatedVideo = videoStreams.find { it.id == targetVideo.id } ?: targetVideo
      VideoCommentsSheet(
        video = updatedVideo,
        onDismiss = { commentSheetVideo = null },
        onAddComment = { text ->
          repository.addVideoComment(updatedVideo.id, text)
        },
        onLikeComment = { commentId ->
          repository.toggleLikeComment(updatedVideo.id, commentId)
        }
      )
    }

    // Share to WhatsApp Dialog / Sheet
    shareSheetVideo?.let { targetVideo ->
      VideoShareSheet(
        video = targetVideo,
        chats = chats,
        onDismiss = { shareSheetVideo = null },
        onShareToChat = { chatId ->
          repository.shareVideoToChat(targetVideo.id, chatId)
          shareSheetVideo = null
        }
      )
    }
  }
}

/**
 * Top floating bar with Following / For You tabs and audio / live indicators
 */
@Composable
private fun TopStreamHeader(
  currentFeedType: Int,
  onSelectFeed: (Int) -> Unit,
  isMuted: Boolean,
  onToggleMute: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.statusBars)
      .padding(top = 10.dp, start = 16.dp, end = 16.dp)
  ) {
    // Left: Live badge
    Surface(
      color = Color(0x66000000),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .align(Alignment.CenterStart)
        .clickable { }
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
      ) {
        Box(
          modifier = Modifier
            .size(7.dp)
            .background(Color(0xFFFF2B54), CircleShape)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = "LIVE",
          color = Color.White,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // Center: Following | For You tabs
    Row(
      modifier = Modifier.align(Alignment.Center),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Following tab
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) { onSelectFeed(0) }
          .padding(horizontal = 12.dp, vertical = 4.dp)
      ) {
        Text(
          text = "Following",
          color = if (currentFeedType == 0) Color.White else Color(0x99FFFFFF),
          fontSize = if (currentFeedType == 0) 17.sp else 16.sp,
          fontWeight = if (currentFeedType == 0) FontWeight.Bold else FontWeight.Normal
        )
        if (currentFeedType == 0) {
          Spacer(modifier = Modifier.height(3.dp))
          Box(
            modifier = Modifier
              .width(26.dp)
              .height(3.dp)
              .background(Color.White, RoundedCornerShape(2.dp))
          )
        }
      }

      Text(
        text = "|",
        color = Color(0x44FFFFFF),
        fontSize = 14.sp,
        modifier = Modifier.padding(horizontal = 2.dp)
      )

      // For You tab
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) { onSelectFeed(1) }
          .padding(horizontal = 12.dp, vertical = 4.dp)
      ) {
        Text(
          text = "For You",
          color = if (currentFeedType == 1) Color.White else Color(0x99FFFFFF),
          fontSize = if (currentFeedType == 1) 17.sp else 16.sp,
          fontWeight = if (currentFeedType == 1) FontWeight.Bold else FontWeight.Normal
        )
        if (currentFeedType == 1) {
          Spacer(modifier = Modifier.height(3.dp))
          Box(
            modifier = Modifier
              .width(26.dp)
              .height(3.dp)
              .background(Color.White, RoundedCornerShape(2.dp))
          )
        }
      }
    }

    // Right: Mute toggle & Search
    Row(
      modifier = Modifier.align(Alignment.CenterEnd),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onToggleMute,
        modifier = Modifier
          .size(34.dp)
          .background(Color(0x55000000), CircleShape)
      ) {
        Icon(
          imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
          contentDescription = "Mute",
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

/**
 * Single Video Item in the vertical pager with full TikTok double-tap heart pop animation
 */
@Composable
private fun VideoPageItem(
  item: VideoStreamItem,
  isMuted: Boolean,
  onToggleMute: () -> Unit,
  onDoubleTapLike: (Offset) -> Unit,
  onSingleTapLike: () -> Unit,
  onToggleFollow: () -> Unit,
  onToggleBookmark: () -> Unit,
  onOpenComments: () -> Unit,
  onOpenShare: () -> Unit
) {
  val scope = rememberCoroutineScope()
  var isPlaying by remember { mutableStateOf(true) }
  var showPlayPauseIndicator by remember { mutableStateOf(false) }

  // Heart burst list for multi-tap bursting
  val heartPops = remember { mutableStateListOf<HeartPop>() }
  val random = remember { Random() }

  // Simulated video playback progress (loops every 14 seconds)
  var videoProgress by remember { mutableFloatStateOf(0f) }

  LaunchedEffect(isPlaying) {
    if (isPlaying) {
      while (true) {
        delay(100)
        videoProgress = (videoProgress + 0.007f) % 1f
      }
    }
  }

  // Double-tap trigger logic
  fun triggerHeartPop(tapOffset: Offset) {
    val id = System.currentTimeMillis() + random.nextInt(1000)
    val rot = (random.nextFloat() * 40f) - 20f
    val scaleAnim = Animatable(0.2f)
    val alphaAnim = Animatable(1f)
    val colors = listOf(
      Color(0xFFFF2B54),
      Color(0xFFFF1744),
      Color(0xFFFF4081),
      Color(0xFFFF5252)
    )
    val chosenColor = colors[random.nextInt(colors.size)]

    val pop = HeartPop(
      id = id,
      position = tapOffset,
      rotation = rot,
      scale = scaleAnim,
      alpha = alphaAnim,
      color = chosenColor
    )
    heartPops.add(pop)

    scope.launch {
      // Spring pop up then float up and fade out
      launch {
        scaleAnim.animateTo(
          targetValue = 1.35f,
          animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
          )
        )
      }
      delay(400)
      launch {
        alphaAnim.animateTo(
          targetValue = 0f,
          animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        )
      }
      delay(400)
      heartPops.remove(pop)
    }

    onDoubleTapLike(tapOffset)
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = item.videoGradientColors.map { Color(it) }
        )
      )
      .pointerInput(item.id) {
        detectTapGestures(
          onDoubleTap = { tapOffset ->
            triggerHeartPop(tapOffset)
          },
          onTap = {
            isPlaying = !isPlaying
            showPlayPauseIndicator = true
            scope.launch {
              delay(800)
              showPlayPauseIndicator = false
            }
          }
        )
      }
  ) {
    // Background Full-Bleed Video Preview Image / Gradient
    if (item.videoPreviewUrl.isNotEmpty()) {
      AsyncImage(
        model = item.videoPreviewUrl,
        contentDescription = "Video Stream Preview",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    }

    // Dynamic Gradient Scrims for readability
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            0.0f to Color(0x66000000),
            0.15f to Color.Transparent,
            0.55f to Color.Transparent,
            1.0f to Color(0xDD000000)
          )
        )
    )

    // Center Play/Pause Indicator Pop-up
    AnimatedVisibility(
      visible = showPlayPauseIndicator,
      enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
      exit = scaleOut() + fadeOut(),
      modifier = Modifier.align(Alignment.Center)
    ) {
      Box(
        modifier = Modifier
          .size(76.dp)
          .background(Color(0x88000000), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
          contentDescription = if (isPlaying) "Playing" else "Paused",
          tint = Color.White,
          modifier = Modifier.size(46.dp)
        )
      }
    }

    // TikTok-style Double Tap Bursting Hearts Overlay
    heartPops.forEach { pop ->
      Box(
        modifier = Modifier
          .offset {
            IntOffset(
              x = (pop.position.x - 55.dp.toPx()).roundToInt(),
              y = (pop.position.y - 55.dp.toPx()).roundToInt()
            )
          }
          .graphicsLayer {
            scaleX = pop.scale.value
            scaleY = pop.scale.value
            alpha = pop.alpha.value
            rotationZ = pop.rotation
          }
      ) {
        // Glowing heart layer
        Icon(
          imageVector = Icons.Default.Favorite,
          contentDescription = "Like heart pop",
          tint = pop.color,
          modifier = Modifier
            .size(110.dp)
            .shadow(elevation = 16.dp, shape = CircleShape, spotColor = Color(0xFFFF2B54))
        )
        // White inner heart shine
        Icon(
          imageVector = Icons.Default.Favorite,
          contentDescription = null,
          tint = Color(0x66FFFFFF),
          modifier = Modifier
            .size(90.dp)
            .align(Alignment.Center)
        )
      }
    }

    // Right Action Bar (Creator Avatar, Like, Comment, Bookmark, Share, Vinyl Disc)
    RightActionRail(
      item = item,
      onLikeClick = onSingleTapLike,
      onFollowClick = onToggleFollow,
      onCommentClick = onOpenComments,
      onBookmarkClick = onToggleBookmark,
      onShareClick = onOpenShare,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 12.dp, bottom = 28.dp)
    )

    // Bottom Left Metadata (Author, Caption, Tags, Music Track)
    BottomMetadataOverlay(
      item = item,
      modifier = Modifier
        .align(Alignment.BottomStart)
        .fillMaxWidth(0.78f)
        .padding(start = 16.dp, bottom = 28.dp)
    )

    // Bottom Video Timeline Scrubbing Bar
    LinearProgressIndicator(
      progress = { videoProgress },
      modifier = Modifier
        .fillMaxWidth()
        .height(3.dp)
        .align(Alignment.BottomCenter),
      color = Color.White,
      trackColor = Color(0x44FFFFFF),
    )
  }
}

/**
 * TikTok Right-side Floating Action Rail
 */
@Composable
private fun RightActionRail(
  item: VideoStreamItem,
  onLikeClick: () -> Unit,
  onFollowClick: () -> Unit,
  onCommentClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  onShareClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Infinite rotation transition for spinning vinyl disc
  val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
  val discAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "disc_angle"
  )

  // Like heart pop bounce animation state
  val heartScale by animateFloatAsState(
    targetValue = if (item.isLiked) 1.25f else 1.0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
    label = "heart_scale"
  )

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Author Avatar with "+" Follow badge
    Box(
      modifier = Modifier.padding(bottom = 6.dp),
      contentAlignment = Alignment.BottomCenter
    ) {
      Box(
        modifier = Modifier
          .size(50.dp)
          .border(1.5.dp, Color.White, CircleShape)
          .padding(2.dp)
      ) {
        UserAvatar(
          avatarUrl = item.authorAvatarUrl,
          name = item.authorName,
          size = 46.dp
        )
      }

      // Plus Follow Button badge
      if (!item.isFollowing) {
        Box(
          modifier = Modifier
            .offset(y = 8.dp)
            .size(22.dp)
            .background(Color(0xFFFF2B54), CircleShape)
            .border(1.dp, Color.White, CircleShape)
            .clickable { onFollowClick() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Follow",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }
      } else {
        Box(
          modifier = Modifier
            .offset(y = 8.dp)
            .size(18.dp)
            .background(WhatsAppLightGreen, CircleShape)
            .border(1.dp, Color.White, CircleShape)
            .clickable { onFollowClick() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Following",
            tint = Color.White,
            modifier = Modifier.size(12.dp)
          )
        }
      }
    }

    // 2. Like Button with Count & Heart Pop effect
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      IconButton(
        onClick = onLikeClick,
        modifier = Modifier
          .size(44.dp)
          .scale(heartScale)
      ) {
        Icon(
          imageVector = if (item.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
          contentDescription = "Like video",
          tint = if (item.isLiked) Color(0xFFFF2B54) else Color.White,
          modifier = Modifier.size(34.dp)
        )
      }
      Text(
        text = formatMetricCount(item.likesCount),
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
      )
    }

    // 3. Comments Button with Count
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      IconButton(
        onClick = onCommentClick,
        modifier = Modifier.size(44.dp)
      ) {
        Icon(
          imageVector = Icons.Default.ChatBubble,
          contentDescription = "Comments",
          tint = Color.White,
          modifier = Modifier.size(32.dp)
        )
      }
      Text(
        text = formatMetricCount(item.commentsCount),
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
      )
    }

    // 4. Bookmark Button with Count
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      IconButton(
        onClick = onBookmarkClick,
        modifier = Modifier.size(44.dp)
      ) {
        Icon(
          imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
          contentDescription = "Bookmark",
          tint = if (item.isBookmarked) Color(0xFFFFB800) else Color.White,
          modifier = Modifier.size(32.dp)
        )
      }
      Text(
        text = formatMetricCount(item.bookmarksCount),
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
      )
    }

    // 5. WhatsApp Share Button
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      IconButton(
        onClick = onShareClick,
        modifier = Modifier.size(44.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Share,
          contentDescription = "Share to WhatsApp",
          tint = Color.White,
          modifier = Modifier.size(32.dp)
        )
      }
      Text(
        text = formatMetricCount(item.sharesCount),
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
      )
    }

    // 6. Spinning Vinyl Record with Album Art & Music notes
    Box(
      modifier = Modifier
        .size(48.dp)
        .rotate(discAngle)
        .background(Color(0xFF1E1E1E), CircleShape)
        .border(4.dp, Color(0xFF2C2C2C), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      if (item.musicCoverUrl.isNotEmpty()) {
        AsyncImage(
          model = item.musicCoverUrl,
          contentDescription = "Music Disc Cover",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
        )
      } else {
        Icon(
          imageVector = Icons.Default.MusicNote,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

/**
 * Bottom-left overlay showing creator handle, description, tags and marquee music info
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BottomMetadataOverlay(
  item: VideoStreamItem,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(false) }

  Column(modifier = modifier) {
    // Creator Handle + Verified checkmark
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(bottom = 6.dp)
    ) {
      Text(
        text = item.authorHandle,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )
      if (item.isVerified) {
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
          imageVector = Icons.Default.Verified,
          contentDescription = "Verified creator",
          tint = Color(0xFF20D5EC),
          modifier = Modifier.size(16.dp)
        )
      }
    }

    // Video Caption / Description
    Text(
      text = item.description,
      color = Color.White,
      fontSize = 14.sp,
      maxLines = if (isExpanded) 6 else 2,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier
        .clickable { isExpanded = !isExpanded }
        .padding(bottom = 6.dp)
    )

    // Hashtags
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.padding(bottom = 8.dp)
    ) {
      item.tags.forEach { tag ->
        Text(
          text = tag,
          color = Color(0xFFE0E0E0),
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    // Music Track Bar (Marquee effect simulation)
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .background(Color(0x44000000), RoundedCornerShape(12.dp))
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Icon(
        imageVector = Icons.Default.MusicNote,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "${item.musicTrackTitle} • ${item.musicArtist}",
        color = Color.White,
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

/**
 * TikTok Comments Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoCommentsSheet(
  video: VideoStreamItem,
  onDismiss: () -> Unit,
  onAddComment: (String) -> Unit,
  onLikeComment: (String) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var newCommentText by remember { mutableStateOf("") }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color(0xFF1E1E1E),
    contentColor = Color.White,
    dragHandle = null,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .height(520.dp)
        .windowInsetsPadding(WindowInsets.ime)
    ) {
      // Header
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Text(
          text = "${formatMetricCount(video.commentsCount)} comments",
          color = Color.White,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.align(Alignment.CenterEnd)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color(0xFFB0B0B0),
            modifier = Modifier.size(20.dp)
          )
        }
      }

      // Comments List
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 16.dp)
      ) {
        items(video.comments, key = { it.id }) { comment ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 10.dp),
            verticalAlignment = Alignment.Top
          ) {
            UserAvatar(
              avatarUrl = comment.userAvatar,
              name = comment.userName,
              size = 38.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = comment.userName,
                color = Color(0xFFB0B0B0),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = comment.text,
                color = Color.White,
                fontSize = 14.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = comment.timestampFormatted,
                  color = Color(0xFF757575),
                  fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                  text = "Reply",
                  color = Color(0xFF9E9E9E),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.clickable { }
                )
              }
            }

            // Comment Like Heart
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.clickable { onLikeComment(comment.id) }
            ) {
              Icon(
                imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Like comment",
                tint = if (comment.isLiked) Color(0xFFFF2B54) else Color(0xFF757575),
                modifier = Modifier.size(16.dp)
              )
              if (comment.likesCount > 0) {
                Text(
                  text = "${comment.likesCount}",
                  color = Color(0xFF757575),
                  fontSize = 10.sp
                )
              }
            }
          }
        }
      }

      // Quick Emoji Row & Add Comment Input
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF141414))
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        // Quick reaction emojis
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          listOf("❤️", "🔥", "😍", "👏", "🙌", "😂", "✨", "💯").forEach { emoji ->
            Text(
              text = emoji,
              fontSize = 20.sp,
              modifier = Modifier
                .clip(CircleShape)
                .clickable {
                  newCommentText += emoji
                }
                .padding(4.dp)
            )
          }
        }

        // Text input field
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = newCommentText,
            onValueChange = { newCommentText = it },
            placeholder = { Text("Add a comment...", color = Color(0xFF757575), fontSize = 14.sp) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF262626),
              unfocusedContainerColor = Color(0xFF262626),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              focusedBorderColor = Color.Transparent,
              unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.weight(1f)
          )

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(
            onClick = {
              if (newCommentText.isNotBlank()) {
                onAddComment(newCommentText)
                newCommentText = ""
              }
            },
            enabled = newCommentText.isNotBlank(),
            modifier = Modifier
              .size(44.dp)
              .background(
                if (newCommentText.isNotBlank()) Color(0xFFFF2B54) else Color(0xFF333333),
                CircleShape
              )
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send comment",
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}

/**
 * Share Video to WhatsApp Chat sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoShareSheet(
  video: VideoStreamItem,
  chats: List<com.example.model.Chat>,
  onDismiss: () -> Unit,
  onShareToChat: (String) -> Unit
) {
  val sheetState = rememberModalBottomSheetState()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color.White,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 10.dp)
        .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Share to WhatsApp Chat",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF111B21)
        )
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF667781))
        }
      }

      Text(
        text = "Send video by ${video.authorName} directly to your conversations",
        fontSize = 13.sp,
        color = Color(0xFF667781),
        modifier = Modifier.padding(bottom = 16.dp)
      )

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .height(300.dp)
      ) {
        items(chats, key = { it.id }) { chat ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onShareToChat(chat.id) }
              .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            UserAvatar(
              avatarUrl = chat.avatarUrl,
              name = chat.name,
              size = 46.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = chat.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111B21)
              )
              Text(
                text = if (chat.isGroup) "Group" else "Contact",
                fontSize = 13.sp,
                color = Color(0xFF667781)
              )
            }
            Surface(
              color = WhatsAppLightGreen,
              shape = RoundedCornerShape(16.dp)
            ) {
              Text(
                text = "Send",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
              )
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

/**
 * Formats large counts into human-readable metric strings (e.g. 1.2K, 345.8K, 1.4M)
 */
private fun formatMetricCount(count: Long): String {
  return when {
    count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
    count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
    else -> count.toString()
  }
}
