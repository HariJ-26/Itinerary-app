package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val HighDensityColorScheme = darkColorScheme(
  primary = HighDensityPrimary,
  onPrimary = HighDensityOnPrimary,
  surface = HighDensitySurface,
  onSurface = HighDensityTextMain,
  background = HighDensityBackground,
  onBackground = HighDensityTextMain,
  outline = HighDensityOutline,
  surfaceVariant = HighDensitySurface,
  onSurfaceVariant = HighDensityTextMuted,
)

@Composable
fun TravelPlannerTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = HighDensityColorScheme,
    typography = Typography,
    content = content
  )
}

