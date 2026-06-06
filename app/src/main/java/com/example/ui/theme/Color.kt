package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

// Dynamic Theme State for Professional Theme Controls
enum class ThemeMode { SYSTEM, DARK, LIGHT }

object ThemeState {
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
    var systemIsDark by mutableStateOf(true)

    val isDarkTheme: Boolean
        get() = when (themeMode) {
            ThemeMode.SYSTEM -> systemIsDark
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
        }
}

// Professional Palette - Deep Obsidian Slate & Executive Royal Indigo/Emerald (Offline Cyber-Security Vibes)
val CarbonDarkBg: Color 
    get() = if (ThemeState.isDarkTheme) Color(0xFF131A2A) else Color(0xFFE2E8F0)

val CharcoalSurface: Color 
    get() = if (ThemeState.isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC)

val SlateBorder: Color 
    get() = if (ThemeState.isDarkTheme) Color(0xFF243447) else Color(0xFFCBD5E1)

// Accent Colors mapped to semantic variables so no existing component breaks
val NeonGreen: Color 
    get() = if (ThemeState.isDarkTheme) Color(0xFF10B981) else Color(0xFF047857) // Darker, more contrast #047857 for light mode

val TerminalCyan: Color 
    get() = if (ThemeState.isDarkTheme) Color(0xFF22D3EE) else Color(0xFF0284C7) // Stronger Cyan #0284C7 for light mode

val ElectricAmber: Color 
    get() = if (ThemeState.isDarkTheme) Color(0xFFFBBF24) else Color(0xFFD97706) // Stronger Amber #D97706 for light mode

val LaserRed: Color 
    get() = if (ThemeState.isDarkTheme) Color(0xFFEF4444) else Color(0xFFDC2626) // Stronger Red #DC2626 for light mode

val TextPrimary: Color 
    get() = if (ThemeState.isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)

val TextSecondary: Color 
    get() = if (ThemeState.isDarkTheme) Color(0xFF94A3B8) else Color(0xFF475569)

// Material Theme Mappings
val Purple80: Color get() = NeonGreen
val PurpleGrey80: Color get() = CharcoalSurface
val Pink80: Color get() = LaserRed

val Purple40: Color get() = NeonGreen
val PurpleGrey40: Color get() = CharcoalSurface
val Pink40: Color get() = LaserRed
