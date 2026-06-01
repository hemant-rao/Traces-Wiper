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
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = NeonGreen,
    secondary = TerminalCyan,
    tertiary = ElectricAmber,
    background = CarbonDarkBg,
    surface = CharcoalSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onPrimary = Color.White,
    error = LaserRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NeonGreen,
    secondary = TerminalCyan,
    tertiary = ElectricAmber,
    background = CarbonDarkBg,
    surface = CharcoalSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onPrimary = Color.White,
    error = LaserRed
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = ThemeState.isDarkTheme,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (ThemeState.isDarkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
