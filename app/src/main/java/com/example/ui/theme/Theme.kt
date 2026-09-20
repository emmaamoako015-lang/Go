package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = RgbNeonGreen,
    secondary = RgbNeonCyan,
    tertiary = RgbNeonPurple,
    background = MirrorObsidian,
    surface = Color(0x35121E30),
    surfaceVariant = Color(0x281B2B44),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFE2E8F0),
    outline = MirrorBorderGlint,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = WhatsAppTeal,
    secondary = WhatsAppLightGreen,
    tertiary = WhatsAppDarkTeal,
    background = Color(0xFFF7F8FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F2F5),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = WhatsAppTextPrimary,
    onSurface = WhatsAppTextPrimary,
    outline = WhatsAppBorder,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek mirror RGB theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

