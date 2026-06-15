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
    primary = Color(0xFF818CF8), // Indigo 400
    primaryContainer = Color(0x30312E81), // Translucent indigo
    secondary = Color(0xFFA5B4FC), // Indigo 300
    background = Color(0xFF0F172A), // Slate 900
    surface = Color(0x4D1E293B), // Translucent 30% slate
    surfaceVariant = Color(0x661E293B), // Translucent 40% slate
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF1F5F9), // Slate 100
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFFE2E8F0),
    outline = Color(0x33FFFFFF) // Thin white border stroke for outline
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF4F46E5), // Indigo 600
    primaryContainer = Color(0x33E0E7FF), // Translucent indigo
    secondary = Color(0xFF818CF8), // Indigo 400
    background = Color(0xFFE8EDF2), // Slate background
    surface = Color(0x4DFFFFFF), // Translucent 30% white
    surfaceVariant = Color(0x66FFFFFF), // Translucent 40% white
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A), // Slate 900
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF1E293B),
    outline = Color(0x80FFFFFF) // Thin white border stroke for outline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
