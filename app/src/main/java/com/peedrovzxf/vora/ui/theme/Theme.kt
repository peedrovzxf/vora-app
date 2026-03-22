package com.peedrovzxf.vora.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Accent colors
val AccentDefault = Color(0xFF6650A4)
val AccentRed = Color(0xFFE53935)
val AccentBlue = Color(0xFF1E88E5)
val AccentGreen = Color(0xFF43A047)
val AccentOrange = Color(0xFFFB8C00)
val AccentPink = Color(0xFFE91E63)
val AccentTeal = Color(0xFF00897B)

fun accentFromKey(key: String): Color = when (key) {
    "red" -> AccentRed
    "blue" -> AccentBlue
    "green" -> AccentGreen
    "orange" -> AccentOrange
    "pink" -> AccentPink
    "teal" -> AccentTeal
    else -> AccentDefault
}

@Composable
fun VoraTheme(
    darkTheme: Boolean = true,
    accentColor: String = "default",
    content: @Composable () -> Unit
) {
    val accent = accentFromKey(accentColor)
    val onAccent = Color.White

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = accent,
            onPrimary = onAccent,
            secondary = accent.copy(alpha = 0.7f),
            background = Color(0xFF0A0A0A),
            surface = Color(0xFF141414),
            surfaceVariant = Color(0xFF1E1E1E),
            onBackground = Color(0xFFF0F0F0),
            onSurface = Color(0xFFF0F0F0),
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = onAccent,
            secondary = accent.copy(alpha = 0.7f),
            background = Color(0xFFFAFAFA),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF0F0F0),
            onBackground = Color(0xFF0A0A0A),
            onSurface = Color(0xFF0A0A0A),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}