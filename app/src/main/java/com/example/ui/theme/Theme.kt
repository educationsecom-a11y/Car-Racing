package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = RacingRed,
  onPrimary = Color.White,
  primaryContainer = RacingRedDark,
  onPrimaryContainer = Color.White,
  secondary = NitroAmber,
  onSecondary = Color.Black,
  secondaryContainer = DarkSurfaceElevated,
  onSecondaryContainer = NitroAmber,
  tertiary = NeonCyan,
  onTertiary = Color.Black,
  background = DarkCarbon,
  onBackground = Color.White,
  surface = DarkSurface,
  onSurface = Color.White,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = Color(0xFFB0B7C6),
  outline = DarkBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
