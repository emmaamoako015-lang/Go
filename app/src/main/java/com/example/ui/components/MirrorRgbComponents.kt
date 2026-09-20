package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MirrorBorderGlint
import com.example.ui.theme.MirrorBorderSubtle
import com.example.ui.theme.MirrorGlassWhite
import com.example.ui.theme.MirrorObsidian
import com.example.ui.theme.MirrorSpecularGleam
import com.example.ui.theme.RgbNeonBlue
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen
import com.example.ui.theme.RgbNeonOrange
import com.example.ui.theme.RgbNeonPink
import com.example.ui.theme.RgbNeonPurple
import com.example.ui.theme.RgbNeonRed
import com.example.ui.theme.RgbNeonYellow
import com.example.ui.theme.RgbSpectrum
import kotlin.math.cos
import kotlin.math.sin

/**
 * Returns a dynamically cycling RGB spectrum brush
 */
@Composable
fun rememberAnimatedRgbBrush(durationMillis: Int = 4000): Brush {
  val transition = rememberInfiniteTransition(label = "rgb_sweep")
  val phase by transition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rgb_phase"
  )

  val rad = Math.toRadians(phase.toDouble()).toFloat()
  val startX = 0.5f - 0.5f * cos(rad)
  val startY = 0.5f - 0.5f * sin(rad)
  val endX = 0.5f + 0.5f * cos(rad)
  val endY = 0.5f + 0.5f * sin(rad)

  return Brush.linearGradient(
    colors = RgbSpectrum,
    start = Offset(startX * 1000f, startY * 1000f),
    end = Offset(endX * 1000f, endY * 1000f)
  )
}

/**
 * Dynamic Root Container with smooth transitioning chromatic ambient background colors
 * and subtle sweeping mirror reflection sheen
 */
@Composable
fun DynamicChromaticBackground(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit
) {
  val transition = rememberInfiniteTransition(label = "chromatic_ambient")

  // Transitioning ambient color phases
  val colorPhase by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(12000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "ambient_phase"
  )

  // Sweeping mirror reflection glint
  val glintPhase by transition.animateFloat(
    initialValue = -0.5f,
    targetValue = 1.5f,
    animationSpec = infiniteRepeatable(
      animation = tween(7000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "mirror_glint"
  )

  // Calculate transitioning ambient aura colors across deep dark obsidian
  val ambientColor1 = remember(colorPhase) {
    when {
      colorPhase < 0.25f -> lerpColor(Color(0xFF0F1A30), Color(0xFF1E0A2A), colorPhase / 0.25f)
      colorPhase < 0.5f -> lerpColor(Color(0xFF1E0A2A), Color(0xFF062420), (colorPhase - 0.25f) / 0.25f)
      colorPhase < 0.75f -> lerpColor(Color(0xFF062420), Color(0xFF281704), (colorPhase - 0.5f) / 0.25f)
      else -> lerpColor(Color(0xFF281704), Color(0xFF0F1A30), (colorPhase - 0.75f) / 0.25f)
    }
  }

  val ambientColor2 = remember(colorPhase) {
    when {
      colorPhase < 0.25f -> lerpColor(Color(0xFF072B28), Color(0xFF1B0B2E), colorPhase / 0.25f)
      colorPhase < 0.5f -> lerpColor(Color(0xFF1B0B2E), Color(0xFF2E120A), (colorPhase - 0.25f) / 0.25f)
      colorPhase < 0.75f -> lerpColor(Color(0xFF2E120A), Color(0xFF0A1B30), (colorPhase - 0.5f) / 0.25f)
      else -> lerpColor(Color(0xFF0A1B30), Color(0xFF072B28), (colorPhase - 0.75f) / 0.25f)
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MirrorObsidian)
      .drawBehind {
        val width = size.width
        val height = size.height

        // 1. First ambient radial glow (top-right)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(ambientColor1.copy(alpha = 0.55f), Color.Transparent),
            center = Offset(width * 0.85f, height * 0.15f),
            radius = width * 0.9f
          )
        )

        // 2. Second ambient radial glow (bottom-left)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(ambientColor2.copy(alpha = 0.50f), Color.Transparent),
            center = Offset(width * 0.15f, height * 0.85f),
            radius = width * 0.95f
          )
        )

        // 3. Mirror specular sweep line
        val glintX = width * glintPhase
        drawRect(
          brush = Brush.linearGradient(
            colors = listOf(
              Color.Transparent,
              Color.White.copy(alpha = 0.04f),
              Color.White.copy(alpha = 0.09f),
              Color.White.copy(alpha = 0.04f),
              Color.Transparent
            ),
            start = Offset(glintX - width * 0.4f, 0f),
            end = Offset(glintX + width * 0.4f, height)
          )
        )
      }
  ) {
    content()
  }
}

/**
 * Modifier for a sleek mirror-like reflective finish with specular highlights and bevel border
 */
fun Modifier.mirrorFinish(
  shape: Shape = RoundedCornerShape(16.dp),
  backgroundColor: Color = Color(0x2E162032),
  borderHighlight: Color = MirrorBorderGlint,
  borderWidth: Dp = 1.2.dp
): Modifier = this
  .clip(shape)
  .background(
    brush = Brush.verticalGradient(
      colors = listOf(
        backgroundColor.copy(alpha = 0.45f),
        backgroundColor.copy(alpha = 0.22f)
      )
    ),
    shape = shape
  )
  .border(
    width = borderWidth,
    brush = Brush.linearGradient(
      colors = listOf(
        borderHighlight,
        MirrorBorderSubtle,
        Color(0x05FFFFFF),
        borderHighlight.copy(alpha = 0.4f)
      ),
      start = Offset(0f, 0f),
      end = Offset(800f, 800f)
    ),
    shape = shape
  )
  .drawWithContent {
    drawContent()
    // Top specular reflective glass gleam
    drawRect(
      brush = Brush.verticalGradient(
        0f to Color.White.copy(alpha = 0.14f),
        0.35f to Color.White.copy(alpha = 0.02f),
        1f to Color.Transparent
      ),
      size = size.copy(height = size.height * 0.5f)
    )
  }

/**
 * Modifier for dynamic pulsating RGB neon lighting border
 */
fun Modifier.dynamicRgbBorder(
  shape: Shape = RoundedCornerShape(16.dp),
  strokeWidth: Dp = 1.5.dp,
  phaseOffset: Float = 0f
): Modifier = this.drawWithContent {
  drawContent()
}

/**
 * Mirror Card with high-gloss liquid chrome surface, specular glint, and subtle RGB aura
 */
@Composable
fun MirrorCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(18.dp),
  backgroundColor: Color = Color(0x28182234),
  rgbAccentEnabled: Boolean = true,
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val rgbBrush = if (rgbAccentEnabled) rememberAnimatedRgbBrush(durationMillis = 5000) else null

  val clickModifier = if (onClick != null) {
    Modifier.clickable(
      interactionSource = remember { MutableInteractionSource() },
      indication = ripple(color = Color.White),
      onClick = onClick
    )
  } else Modifier

  Box(
    modifier = modifier
      .then(clickModifier)
      .clip(shape)
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            backgroundColor.copy(alpha = 0.50f),
            backgroundColor.copy(alpha = 0.25f)
          )
        ),
        shape = shape
      )
      .border(
        width = 1.2.dp,
        brush = rgbBrush ?: Brush.linearGradient(
          colors = listOf(
            MirrorBorderGlint,
            MirrorBorderSubtle,
            Color(0x10FFFFFF)
          )
        ),
        shape = shape
      )
      .drawWithContent {
        drawContent()
        // Top specular mirror gleam
        drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color.White.copy(alpha = 0.12f),
              Color.Transparent
            )
          ),
          size = size.copy(height = size.height * 0.45f)
        )
      }
      .padding(14.dp)
  ) {
    content()
  }
}

/**
 * Dynamic RGB Glowing Pill / Tag
 */
@Composable
fun RgbGlowingPill(
  text: String,
  modifier: Modifier = Modifier,
  textColor: Color = Color.White
) {
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 3500)

  Box(
    modifier = modifier
      .clip(CircleShape)
      .background(Color(0x3310192A), CircleShape)
      .border(1.5.dp, rgbBrush, CircleShape)
      .padding(horizontal = 10.dp, vertical = 4.dp)
  ) {
    androidx.compose.material3.Text(
      text = text,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    )
  }
}

/**
 * Color linear interpolation helper
 */
private fun lerpColor(c1: Color, c2: Color, fraction: Float): Color {
  val f = fraction.coerceIn(0f, 1f)
  return Color(
    red = c1.red + (c2.red - c1.red) * f,
    green = c1.green + (c2.green - c1.green) * f,
    blue = c1.blue + (c2.blue - c1.blue) * f,
    alpha = c1.alpha + (c2.alpha - c1.alpha) * f
  )
}

/**
 * Mirror Chrome brush for typography, headers, and accents.
 * Creates a high-polish metallic silver / platinum reflection luster.
 */
@Composable
fun rememberMirrorChromeBrush(): Brush {
  return remember {
    Brush.linearGradient(
      colors = listOf(
        Color(0xFFFFFFFF), // 100% white specular glint
        Color(0xFFE2E8F0), // Platinum chrome
        Color(0xFFFFFFFF), // Midpoint shine
        Color(0xFF94A3B8), // Metallic cool silver depth
        Color(0xFFCBD5E1), // Shimmer finish
        Color(0xFFFFFFFF)  // Edge glint
      ),
      start = Offset(0f, 0f),
      end = Offset(400f, 100f)
    )
  }
}

/**
 * Animated shimmering liquid mirror brush with continuous specular sweep
 */
@Composable
fun rememberAnimatedMirrorBrush(durationMillis: Int = 3200): Brush {
  val transition = rememberInfiniteTransition(label = "mirror_sweep")
  val glintOffset by transition.animateFloat(
    initialValue = -200f,
    targetValue = 600f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "mirror_glint_offset"
  )

  return Brush.linearGradient(
    colors = listOf(
      Color(0xFFE2E8F0),
      Color(0xFFFFFFFF),
      Color(0xFF94A3B8),
      Color(0xFFFFFFFF),
      Color(0xFFE2E8F0)
    ),
    start = Offset(glintOffset, 0f),
    end = Offset(glintOffset + 350f, 120f)
  )
}

/**
 * High-polish Mirror Typography Text component.
 * Renders text with reflective chrome luster, high contrast, and polished specular finish.
 */
@Composable
fun MirrorText(
  text: String,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = 16.sp,
  fontWeight: FontWeight = FontWeight.Bold,
  fontFamily: FontFamily? = null,
  textAlign: TextAlign? = null,
  maxLines: Int = Int.MAX_VALUE,
  overflow: TextOverflow = TextOverflow.Clip,
  letterSpacing: TextUnit = TextUnit.Unspecified,
  animated: Boolean = false
) {
  val brush = if (animated) rememberAnimatedMirrorBrush() else rememberMirrorChromeBrush()

  Text(
    text = text,
    modifier = modifier,
    fontSize = fontSize,
    fontWeight = fontWeight,
    fontFamily = fontFamily,
    textAlign = textAlign,
    maxLines = maxLines,
    overflow = overflow,
    letterSpacing = letterSpacing,
    style = TextStyle(brush = brush)
  )
}

/**
 * Polished Mirror Button with liquid glass specular reflection, chrome bevel border,
 * tactile touch ripple, and crisp typography.
 */
@Composable
fun MirrorButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = RoundedCornerShape(24.dp),
  backgroundColor: Color = Color(0xDD0D1627),
  borderBrush: Brush? = null,
  rgbBorder: Boolean = false,
  icon: ImageVector? = null,
  iconTint: Color = Color.White,
  text: String? = null,
  textColor: Color? = null,
  content: (@Composable RowScope.() -> Unit)? = null
) {
  val animatedRgb = if (rgbBorder) rememberAnimatedRgbBrush(durationMillis = 3500) else null
  val chromeBrush = rememberMirrorChromeBrush()
  val activeBorder = borderBrush ?: animatedRgb ?: Brush.linearGradient(
    listOf(
      MirrorBorderGlint,
      MirrorBorderSubtle,
      Color(0x10FFFFFF),
      MirrorBorderGlint.copy(alpha = 0.5f)
    )
  )

  Box(
    modifier = modifier
      .defaultMinSize(minHeight = 44.dp)
      .clip(shape)
      .background(
        brush = Brush.verticalGradient(
          colors = if (enabled) {
            listOf(backgroundColor, Color(0xF0070D18))
          } else {
            listOf(Color(0x30101826), Color(0x200B101A))
          }
        ),
        shape = shape
      )
      .border(
        width = 1.2.dp,
        brush = if (enabled) activeBorder else SolidColor(MirrorBorderSubtle),
        shape = shape
      )
      .drawWithContent {
        drawContent()
        if (enabled) {
          // Top specular mirror reflection gleam
          drawRect(
            brush = Brush.verticalGradient(
              listOf(
                Color.White.copy(alpha = 0.22f),
                Color.White.copy(alpha = 0.04f),
                Color.Transparent
              )
            ),
            size = size.copy(height = size.height * 0.45f)
          )
        }
      }
      .clickable(
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = Color.White),
        onClick = onClick
      )
      .padding(horizontal = 18.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    if (content != null) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        content()
      }
    } else {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        if (icon != null) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) iconTint else Color(0xFF64748B),
            modifier = Modifier.size(18.dp)
          )
          if (!text.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
          }
        }
        if (!text.isNullOrBlank()) {
          if (textColor != null) {
            Text(
              text = text,
              color = if (enabled) textColor else Color(0xFF64748B),
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          } else {
            MirrorText(
              text = text,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

/**
 * Compact Polished Mirror Icon Button for app bars, toolbars, and quick actions
 */
@Composable
fun MirrorIconButton(
  onClick: () -> Unit,
  icon: ImageVector,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  tint: Color = Color.White,
  shape: Shape = CircleShape,
  size: Dp = 40.dp,
  rgbBorder: Boolean = false
) {
  val rgbBrush = if (rgbBorder) rememberAnimatedRgbBrush(durationMillis = 3500) else null
  val defaultBorder = Brush.linearGradient(
    listOf(MirrorBorderGlint.copy(alpha = 0.6f), MirrorBorderSubtle)
  )

  Box(
    modifier = modifier
      .size(size)
      .clip(shape)
      .background(Color(0x600E192B), shape)
      .border(1.dp, rgbBrush ?: defaultBorder, shape)
      .drawWithContent {
        drawContent()
        drawRect(
          brush = Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.18f), Color.Transparent)
          ),
          size = this.size.copy(height = this.size.height * 0.45f)
        )
      }
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true, color = Color.White),
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = tint,
      modifier = Modifier.size(size * 0.52f)
    )
  }
}

/**
 * Polished Mirror List Item / Row container ("ul element")
 * Provides liquid glass reflection, top gleam, and chrome border for lists and feeds.
 */
@Composable
fun MirrorListItem(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(16.dp),
  backgroundColor: Color = Color(0x28121A28),
  borderHighlight: Color = MirrorBorderSubtle,
  rgbAccentEnabled: Boolean = false,
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val rgbBrush = if (rgbAccentEnabled) rememberAnimatedRgbBrush(durationMillis = 4000) else null

  val clickModifier = if (onClick != null) {
    Modifier.clickable(
      interactionSource = remember { MutableInteractionSource() },
      indication = ripple(color = Color.White),
      onClick = onClick
    )
  } else Modifier

  Box(
    modifier = modifier
      .clip(shape)
      .then(clickModifier)
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            backgroundColor.copy(alpha = 0.50f),
            backgroundColor.copy(alpha = 0.22f)
          )
        ),
        shape = shape
      )
      .border(
        width = 1.dp,
        brush = rgbBrush ?: Brush.linearGradient(
          listOf(
            borderHighlight,
            Color(0x10FFFFFF),
            borderHighlight.copy(alpha = 0.3f)
          )
        ),
        shape = shape
      )
      .drawWithContent {
        drawContent()
        // Top specular mirror gleam
        drawRect(
          brush = Brush.verticalGradient(
            listOf(
              Color.White.copy(alpha = 0.11f),
              Color.Transparent
            )
          ),
          size = size.copy(height = size.height * 0.42f)
        )
      }
      .padding(14.dp)
  ) {
    content()
  }
}

/**
 * Polished Mirror Chip / Filter Pill with specular glint
 */
@Composable
fun MirrorChip(
  selected: Boolean,
  onClick: () -> Unit,
  text: String,
  modifier: Modifier = Modifier,
  leadingIcon: ImageVector? = null,
  badgeText: String? = null
) {
  val rgbBrush = rememberAnimatedRgbBrush(durationMillis = 3500)

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .background(
        if (selected) Color(0xD90E1F35) else Color(0x30101928),
        RoundedCornerShape(18.dp)
      )
      .border(
        width = if (selected) 1.5.dp else 1.dp,
        brush = if (selected) rgbBrush else SolidColor(MirrorBorderSubtle),
        shape = RoundedCornerShape(18.dp)
      )
      .drawWithContent {
        drawContent()
        if (selected) {
          drawRect(
            brush = Brush.verticalGradient(
              listOf(Color.White.copy(alpha = 0.18f), Color.Transparent)
            ),
            size = size.copy(height = size.height * 0.45f)
          )
        }
      }
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = Color.White),
        onClick = onClick
      )
      .padding(horizontal = 14.dp, vertical = 7.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (leadingIcon != null) {
        Icon(
          imageVector = leadingIcon,
          contentDescription = null,
          tint = if (selected) RgbNeonCyan else Color(0xFF94A3B8),
          modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
      }
      if (selected) {
        MirrorText(
          text = text,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
      } else {
        Text(
          text = text,
          color = Color(0xFF94A3B8),
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      }
      if (badgeText != null) {
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) RgbNeonCyan else Color(0x4015243B), CircleShape)
            .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
          Text(
            text = badgeText,
            color = if (selected) Color.Black else Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

