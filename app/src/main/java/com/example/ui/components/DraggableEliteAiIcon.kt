package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ai.EliteAiService
import com.example.data.ChatRepository
import com.example.ui.theme.MirrorBorderSubtle
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen
import com.example.ui.theme.RgbNeonPink
import com.example.ui.theme.RgbNeonPurple
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * An iconic, futuristic floating AI orb that can be smoothly dragged anywhere across the screen,
 * persisting over all tabs, platforms, calls, and chat views.
 * Tapping it opens the Global Elite AI Co-Pilot Hub.
 */
@Composable
fun DraggableEliteAiIcon(
  repository: ChatRepository,
  onOpenChat: (chatId: String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val density = LocalDensity.current

  var showHubDialog by remember { mutableStateOf(false) }
  var isDragging by remember { mutableStateOf(false) }

  // Drag coordinates (-1f initially to flag default positioning)
  var offsetX by remember { mutableFloatStateOf(-1f) }
  var offsetY by remember { mutableFloatStateOf(-1f) }

  // Animated scale when dragged or hovered
  val scale by animateFloatAsState(
    targetValue = if (isDragging) 1.15f else 1.0f,
    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
    label = "drag_scale"
  )

  // Infinite pulsing ring animations for the iconic aura
  val infiniteTransition = rememberInfiniteTransition(label = "elite_orb_halo")
  val rotationAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "halo_rotation"
  )
  val pulseAura by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.12f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "halo_pulse"
  )

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val maxWidthPx = with(density) { maxWidth.toPx() }
    val maxHeightPx = with(density) { maxHeight.toPx() }
    val iconWidthDp = 64.dp
    val iconHeightDp = 64.dp
    val iconWidthPx = with(density) { iconWidthDp.toPx() }
    val iconHeightPx = with(density) { iconHeightDp.toPx() }

    val minMarginX = with(density) { 12.dp.toPx() }
    val maxMarginX = (maxWidthPx - iconWidthPx - with(density) { 12.dp.toPx() }).coerceAtLeast(0f)
    val minMarginY = with(density) { 70.dp.toPx() } // avoid top app bar
    val maxMarginY = (maxHeightPx - iconHeightPx - with(density) { 90.dp.toPx() }).coerceAtLeast(0f) // avoid bottom nav

    // Initialize default position near the right edge at ~65% screen height
    if (offsetX < 0f) {
      offsetX = (maxWidthPx - iconWidthPx - with(density) { 16.dp.toPx() }).coerceIn(minMarginX, maxMarginX)
      offsetY = (maxHeightPx * 0.65f).coerceIn(minMarginY, maxMarginY)
    }

    Box(
      modifier = Modifier
        .offset {
          IntOffset(
            offsetX.roundToInt().coerceIn(0, (maxWidthPx - iconWidthPx).roundToInt().coerceAtLeast(0)),
            offsetY.roundToInt().coerceIn(0, (maxHeightPx - iconHeightPx).roundToInt().coerceAtLeast(0))
          )
        }
        .size(iconWidthDp, iconHeightDp)
        .scale(scale)
        .testTag("floating_elite_ai_icon")
        .pointerInput(maxWidthPx, maxHeightPx) {
          awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val downTime = System.currentTimeMillis()
            var accumulatedDistance = 0f
            var isMoving = false

            while (true) {
              val event = awaitPointerEvent()
              val currentChange = event.changes.firstOrNull { it.id == down.id } ?: break
              if (!currentChange.pressed) {
                break
              }
              val posChange = currentChange.positionChange()
              if (posChange != Offset.Zero) {
                accumulatedDistance += hypot(posChange.x, posChange.y)
                if (accumulatedDistance > 12f) {
                  isMoving = true
                  isDragging = true
                  currentChange.consume()
                  offsetX = (offsetX + posChange.x).coerceIn(minMarginX, maxMarginX)
                  offsetY = (offsetY + posChange.y).coerceIn(minMarginY, maxMarginY)
                }
              }
            }

            isDragging = false
            val elapsed = System.currentTimeMillis() - downTime
            if (!isMoving || (accumulatedDistance < 18f && elapsed < 450L)) {
              // Tapped on the Icon -> Open the AI page directly and start a conversation asking what you want!
              repository.ensureAiGreeting()
              onOpenChat("chat_elite")
            }
          }
        }
    ) {
      // 1. Futuristic Multi-Layered Pulsing Aura
      Box(
        modifier = Modifier
          .matchParentSize()
          .scale(pulseAura)
          .drawBehind {
            drawCircle(
              brush = Brush.radialGradient(
                listOf(
                  Color(0xFF00DFD8).copy(alpha = 0.35f),
                  Color(0xFF7928CA).copy(alpha = 0.22f),
                  Color(0xFFFF0080).copy(alpha = 0.10f),
                  Color.Transparent
                )
              )
            )
          }
      )

      // 2. Rotating Holographic Neon Ring
      Box(
        modifier = Modifier
          .matchParentSize()
          .rotate(rotationAngle)
          .padding(3.dp)
          .border(
            width = 2.2.dp,
            brush = Brush.sweepGradient(
              listOf(
                Color(0xFF00DFD8), // Neon Cyan
                Color(0xFF7928CA), // Deep Violet
                Color(0xFFFF0080), // Neon Pink
                Color(0xFF00FF88), // Neon Emerald
                Color(0xFF00DFD8)
              )
            ),
            shape = CircleShape
          )
      )

      // 3. Cybernetic Orb Body (Deep Obsidian Core with Glassmorphism)
      Box(
        modifier = Modifier
          .matchParentSize()
          .padding(6.dp)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              listOf(
                Color(0xFF1E1B4B), // Indigo core
                Color(0xFF0A0F1D), // Obsidian
                Color(0xFF030712)
              )
            ),
            CircleShape
          )
          .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
          .shadow(elevation = 12.dp, shape = CircleShape),
        contentAlignment = Alignment.Center
      ) {
        // Sparkling central AI Starburst Glyph
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = "Elite AI Co-Pilot",
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      }

      // 4. Online Indicator Dot (Glowing Emerald Green)
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(end = 4.dp, top = 4.dp)
          .size(11.dp)
          .clip(CircleShape)
          .background(RgbNeonGreen, CircleShape)
          .border(1.5.dp, Color(0xFF0A0F1D), CircleShape)
      )

      // 5. Drag Handle Micro Hint
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 2.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(Color(0x900B1220), RoundedCornerShape(4.dp))
          .padding(horizontal = 4.dp, vertical = 1.dp)
      ) {
        Text(
          text = "OMNI 👑",
          fontSize = 8.sp,
          fontWeight = FontWeight.Black,
          color = Color(0xFFFFD700),
          letterSpacing = 0.6.sp
        )
      }
    }
  }

  // Global Elite AI Hub Modal
  if (showHubDialog) {
    GlobalEliteAiHubDialog(
      repository = repository,
      onDismiss = { showHubDialog = false },
      onOpenFullChat = {
        showHubDialog = false
        onOpenChat("chat_elite")
      }
    )
  }
}

/**
 * The Global Elite AI Co-Pilot Hub that opens anywhere on screen.
 * Delivers dual Gemini 3.5 & Claude 3.7 intelligence capabilities:
 * coding, architectural design, deep reasoning, creative writing, multi-lingual
 * translation, conversation summarization, and interactive chat assistance.
 */
@Composable
fun GlobalEliteAiHubDialog(
  repository: ChatRepository,
  onDismiss: () -> Unit,
  onOpenFullChat: () -> Unit
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val scope = rememberCoroutineScope()
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 3500)

  var userQuery by remember { mutableStateOf("") }
  var isAiGenerating by remember { mutableStateOf(false) }
  var aiDetailedResponse by remember { mutableStateOf<com.example.ai.AiResponse?>(null) }
  var selectedEngine by remember { mutableStateOf(com.example.ai.AiEngine.MASTER) }
  var enableDeepThinking by remember { mutableStateOf(true) }
  var isThinkingExpanded by remember { mutableStateOf(true) }
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Dual AI Ask, 1: Deep Capabilities, 2: Mindset Specs

  fun shareExternal(text: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share AI Response via"))
  }

  fun copyToClipboard(text: String) {
    clipboardManager.setText(AnnotatedString(text))
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
  }

  fun executeQuery(query: String) {
    if (query.isBlank() || isAiGenerating) return
    isAiGenerating = true
    scope.launch {
      val response = EliteAiService.askEliteDetailed(
        prompt = query,
        engine = selectedEngine,
        enableDeepThinking = enableDeepThinking
      )
      aiDetailedResponse = response
      isAiGenerating = false
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(26.dp))
        .border(1.8.dp, rgbBrush, RoundedCornerShape(26.dp)),
      color = Color(0xF6090E1A)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
      ) {
        // --- Dual Engine Brand Header ---
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                  Brush.sweepGradient(
                    listOf(
                      Color(0xFF00DFD8),
                      Color(0xFF7928CA),
                      Color(0xFFFF0080),
                      Color(0xFF00FF88),
                      Color(0xFF00DFD8)
                    )
                  ),
                  CircleShape
                )
                .border(1.5.dp, Color.White, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                MirrorText(
                  text = "Omni Master AI",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x35FFD700), RoundedCornerShape(6.dp))
                    .border(0.8.dp, Color(0xFFFFD700), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text("FULL ACCESS", fontSize = 8.5.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD700))
                }
              }
              Text(
                text = "Unrestricted Creation Engine • Master Directives Active",
                fontSize = 10.5.sp,
                color = RgbNeonCyan
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color(0xFF94A3B8),
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Engine Mindset Selector ---
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          com.example.ai.AiEngine.values().forEach { engine ->
            val isSelected = selectedEngine == engine
            val engineColor = when (engine) {
              com.example.ai.AiEngine.MASTER -> Color(0xFFFFD700) // Royal Gold
              com.example.ai.AiEngine.GEMINI -> RgbNeonCyan
              com.example.ai.AiEngine.CLAUDE -> Color(0xFFF97316) // Coral Amber
              com.example.ai.AiEngine.HYBRID -> Color(0xFFD946EF) // Fused Neon Pink
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(
                  if (isSelected) engineColor.copy(alpha = 0.22f) else Color(0x2015243B),
                  RoundedCornerShape(10.dp)
                )
                .border(
                  width = if (isSelected) 1.5.dp else 0.8.dp,
                  color = if (isSelected) engineColor else MirrorBorderSubtle,
                  shape = RoundedCornerShape(10.dp)
                )
                .clickable {
                  selectedEngine = engine
                }
                .padding(vertical = 7.dp, horizontal = 4.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = engine.badge,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else Color(0xFF94A3B8)
                )
                Text(
                  text = engine.displayName,
                  fontSize = 8.5.sp,
                  color = if (isSelected) engineColor else Color(0xFF64748B)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- Deep Thinking & Feature Mode Bar ---
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Deep Thinking Toggle Pill
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(
                if (enableDeepThinking) Color(0x357928CA) else Color(0x2015243B),
                RoundedCornerShape(8.dp)
              )
              .border(
                1.dp,
                if (enableDeepThinking) Color(0xFFC084FC) else MirrorBorderSubtle,
                RoundedCornerShape(8.dp)
              )
              .clickable { enableDeepThinking = !enableDeepThinking }
              .padding(horizontal = 8.dp, vertical = 5.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = if (enableDeepThinking) Color(0xFFE879F9) else Color(0xFF64748B),
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (enableDeepThinking) "Extended Thinking: ON" else "Extended Thinking: OFF",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (enableDeepThinking) Color.White else Color(0xFF94A3B8)
              )
            }
          }

          // Section Tabs: Ask / Capabilities
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("Ask", "Capabilities").forEachIndexed { idx, title ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (selectedTab == idx) Color(0x3000DFD8) else Color.Transparent)
                  .clickable { selectedTab = idx }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = title,
                  fontSize = 11.sp,
                  fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal,
                  color = if (selectedTab == idx) RgbNeonCyan else Color(0xFF64748B)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- Tab 0: Ask Dual AI ---
        if (selectedTab == 0) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState())
              .heightIn(max = 380.dp)
          ) {
            // Input field
            OutlinedTextField(
              value = userQuery,
              onValueChange = { userQuery = it },
              placeholder = {
                Text(
                  text = "Ask code, reasoning, writing, math, or translation...",
                  color = Color(0xFF64748B),
                  fontSize = 12.sp
                )
              },
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = RgbNeonCyan,
                unfocusedBorderColor = MirrorBorderSubtle,
                focusedContainerColor = Color(0x3015243B),
                unfocusedContainerColor = Color(0x200B1220)
              ),
              shape = RoundedCornerShape(14.dp),
              trailingIcon = {
                IconButton(
                  onClick = {
                    val p = userQuery
                    userQuery = ""
                    executeQuery(p)
                  },
                  enabled = userQuery.isNotBlank() && !isAiGenerating
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (userQuery.isNotBlank()) RgbNeonCyan else Color(0xFF475569)
                  )
                }
              },
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
              keyboardActions = KeyboardActions(
                onSend = {
                  val p = userQuery
                  userQuery = ""
                  executeQuery(p)
                }
              ),
              modifier = Modifier.fillMaxWidth()
            )

            // Loading state
            if (isAiGenerating) {
              Spacer(modifier = Modifier.height(14.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(18.dp),
                  color = RgbNeonCyan,
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "Dual AI (${selectedEngine.displayName}) is synthesizing...",
                  fontSize = 12.sp,
                  color = RgbNeonCyan
                )
              }
            }

            // AI Answer display card
            aiDetailedResponse?.let { resp ->
              Spacer(modifier = Modifier.height(14.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(16.dp))
                  .background(Color(0x4015243B), RoundedCornerShape(16.dp))
                  .border(1.2.dp, RgbNeonCyan.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                  .padding(14.dp)
              ) {
                Column {
                  // Engine info badge & actions
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = resp.modelTag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RgbNeonCyan
                      )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                      IconButton(
                        onClick = { copyToClipboard(resp.answer) },
                        modifier = Modifier.size(28.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.ContentCopy,
                          contentDescription = "Copy",
                          tint = Color.White,
                          modifier = Modifier.size(15.dp)
                        )
                      }
                      IconButton(
                        onClick = { shareExternal(resp.answer) },
                        modifier = Modifier.size(28.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.Share,
                          contentDescription = "Share",
                          tint = Color(0xFF38BDF8),
                          modifier = Modifier.size(15.dp)
                        )
                      }
                    }
                  }

                  // Collapsible Thinking Process Block (like Claude Extended Thinking & Gemini Thinking)
                  if (!resp.thinkingChain.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x351F2937), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF4B5563), RoundedCornerShape(10.dp))
                        .clickable { isThinkingExpanded = !isThinkingExpanded }
                        .padding(10.dp)
                    ) {
                      Column {
                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                          Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                              imageVector = Icons.Default.Psychology,
                              contentDescription = null,
                              tint = Color(0xFFC084FC),
                              modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                              text = "💭 Extended Chain-of-Thought",
                              fontSize = 11.sp,
                              fontWeight = FontWeight.Bold,
                              color = Color(0xFFE5E7EB)
                            )
                          }
                          Icon(
                            imageVector = if (isThinkingExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(16.dp)
                          )
                        }

                        if (isThinkingExpanded) {
                          Spacer(modifier = Modifier.height(6.dp))
                          Text(
                            text = resp.thinkingChain,
                            fontSize = 11.sp,
                            color = Color(0xFFD1D5DB),
                            lineHeight = 15.sp
                          )
                        }
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(8.dp))

                  Text(
                    text = resp.answer,
                    fontSize = 13.sp,
                    color = Color.White,
                    lineHeight = 18.sp
                  )
                }
              }
            }
          }
        }

        // --- Tab 1: Deep Capabilities Matrix ---
        if (selectedTab == 1) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState())
              .heightIn(max = 380.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val capabilities = listOf(
              Triple("👑 Master Omni Creation", "Create full application architecture, algorithms, and system modules as commanded by Master", Icons.Default.AutoAwesome),
              Triple("💻 Code & Architecture", "Generate Kotlin coroutines, thread-safe cache, and unit tests", Icons.Default.Code),
              Triple("🧠 Deep Logic & Game Theory", "Analyze Prisoner's Dilemma, Nash Equilibrium & deduction", Icons.Default.Psychology),
              Triple("✍️ Creative Writing & Prose", "Draft an evocative sci-fi prologue in an orbital megacity", Icons.Default.AutoAwesome),
              Triple("📊 Executive Brief", "Generate structured summaries with key takeaways and action items", Icons.Default.ElectricBolt),
              Triple("🌐 Polyglot Translation", "Translate seamlessly into Spanish, French, German, and Japanese", Icons.Default.Translate),
              Triple("⚡ Chat Co-Pilot", "Draft an articulate, polite reply to confirm our deployment timeline", Icons.Default.Lightbulb)
            )

            capabilities.forEach { (title, subtitle, icon) ->
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .background(Color(0x3015243B), RoundedCornerShape(14.dp))
                  .border(1.dp, MirrorBorderSubtle, RoundedCornerShape(14.dp))
                  .clickable {
                    userQuery = subtitle
                    selectedTab = 0
                    executeQuery(subtitle)
                  }
                  .padding(12.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(Color(0x2500DFD8), CircleShape)
                      .border(1.dp, RgbNeonCyan.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = icon,
                      contentDescription = null,
                      tint = RgbNeonCyan,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Footer Action: Jump into dedicated Elite AI Chat thread
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
              Brush.horizontalGradient(
                listOf(
                  Color(0xFF7928CA),
                  Color(0xFF00DFD8)
                )
              ),
              RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onOpenFullChat)
            .padding(vertical = 11.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Chat,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Open Full Dual AI Chat Screen",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }
    }
  }
}
