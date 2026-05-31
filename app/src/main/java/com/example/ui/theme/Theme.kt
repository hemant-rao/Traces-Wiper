package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NeonGreen,
    secondary = TerminalCyan,
    tertiary = ElectricAmber,
    background = CarbonDarkBg,
    surface = CharcoalSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onPrimary = CarbonDarkBg,
    error = LaserRed
  )

private val LightColorScheme = DarkColorScheme // Always use dark theme for secure look!

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme
  dynamicColor: Boolean = false, // Force custom dark scheme
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
