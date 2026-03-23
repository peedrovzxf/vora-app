package com.peedrovzxf.vora.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun accentFromKey(key: String): Color = when (key) {
    "red"    -> AccentRed
    "blue"   -> AccentBlue
    "green"  -> AccentGreen
    "orange" -> AccentOrange
    "pink"   -> AccentPink
    "teal"   -> AccentTeal
    else     -> AccentDefault
}

@Composable
fun VoraTheme(
    darkTheme: Boolean = true,
    accentColor: String = "default",
    content: @Composable () -> Unit
) {
    val accent = accentFromKey(accentColor)

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary            = accent,
            onPrimary          = Color.White,
            primaryContainer   = accent.copy(alpha = 0.15f),
            onPrimaryContainer = Color.White,
            secondary          = accent.copy(alpha = 0.7f),
            onSecondary        = Color.White,
            secondaryContainer = Surface3,
            onSecondaryContainer = Color.White,
            background         = Black,
            onBackground       = Color.White,
            surface            = Surface1,
            onSurface          = Color.White,
            surfaceVariant     = Surface2,
            onSurfaceVariant   = Grey400,
            outline            = Surface4,
            outlineVariant     = Surface3,
            scrim              = Color.Black.copy(alpha = 0.7f),
            inverseSurface     = Color(0xFFF0F0F0),
            inverseOnSurface   = Color(0xFF111111),
        )
    } else {
        lightColorScheme(
            primary            = accent,
            onPrimary          = Color.White,
            primaryContainer   = accent.copy(alpha = 0.12f),
            onPrimaryContainer = accent,
            secondary          = accent.copy(alpha = 0.7f),
            onSecondary        = Color.White,
            secondaryContainer = Color(0xFFEEEEEE),
            onSecondaryContainer = Color(0xFF111111),
            background         = Color(0xFFF7F7F7),
            onBackground       = Color(0xFF0A0A0A),
            surface            = Color(0xFFFFFFFF),
            onSurface          = Color(0xFF0A0A0A),
            surfaceVariant     = Color(0xFFEEEEEE),
            onSurfaceVariant   = Color(0xFF555555),
            outline            = Color(0xFFCCCCCC),
            outlineVariant     = Color(0xFFE8E8E8),
            scrim              = Color.Black.copy(alpha = 0.4f),
            inverseSurface     = Color(0xFF1A1A1A),
            inverseOnSurface   = Color(0xFFF0F0F0),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
