package com.liveongames.liveon.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Uses the LiveonTheme data class from GameThemes.kt
val LocalLiveonTheme = staticCompositionLocalOf {
    LiveonTheme(
        name = "Default",
        background = Color(0xFF0F1115),
        surface = Color(0xFF171A1F),
        primary = Color(0xFF6C7CFF),
        accent = Color(0xFF22C1DC),
        secondary = Color(0xFF9AA0A6),
        text = Color(0xFFECEFF4),
        surfaceVariant = Color(0xFF23262C),
        surfaceElevated = Color(0xFF1B1F25)
    )
}

private val LiveonShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small      = RoundedCornerShape(10.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** Overload #1 — choose a theme (first from AllGameThemes by default) */
@Composable
fun LiveonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val selectedTheme = AllGameThemes.firstOrNull() ?: LocalLiveonTheme.current
    ProvideLiveonTheme(selectedTheme, darkTheme, content)
}

/** Overload #2 — provide an explicit theme instance */
@Composable
fun LiveonTheme(
    theme: LiveonTheme,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    ProvideLiveonTheme(theme, darkTheme, content)
}

@Composable
private fun ProvideLiveonTheme(
    theme: LiveonTheme,
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val cs = if (darkTheme) {
        darkColorScheme(
            primary = theme.primary,
            secondary = theme.secondary,
            tertiary = theme.accent,
            background = theme.background,
            surface = theme.surface,
            surfaceVariant = theme.surfaceVariant,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = theme.text,
            onSurface = theme.text,
            onSurfaceVariant = theme.text.copy(alpha = 0.78f)
        )
    } else {
        lightColorScheme(
            primary = theme.primary,
            secondary = theme.secondary,
            tertiary = theme.accent,
            background = theme.background,
            surface = theme.surface,
            surfaceVariant = theme.surfaceVariant,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = theme.text,
            onSurface = theme.text,
            onSurfaceVariant = theme.text.copy(alpha = 0.78f)
        )
    }

    CompositionLocalProvider(LocalLiveonTheme provides theme) {
        MaterialTheme(
            colorScheme = cs,
            typography = Typography(),
            shapes = LiveonShapes,
            content = content
        )
    }
}

@Composable
private fun ProvideLiveonTheme( // Modified overload for system theme
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    // Use the default theme from LocalLiveonTheme as fallback
    val defaultTheme = LocalLiveonTheme.current
    val cs = if (darkTheme) {
        darkColorScheme(
            primary = defaultTheme.primary,
            secondary = defaultTheme.secondary,
            tertiary = defaultTheme.accent,
            background = defaultTheme.background,
            surface = defaultTheme.surface,
            surfaceVariant = defaultTheme.surfaceVariant,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = defaultTheme.text,
            onSurface = defaultTheme.text,
            onSurfaceVariant = defaultTheme.text.copy(alpha = 0.78f)
        )
    } else {
        lightColorScheme(
            primary = defaultTheme.primary,
            secondary = defaultTheme.secondary,
            tertiary = defaultTheme.accent,
            background = defaultTheme.background,
            surface = defaultTheme.surface,
            surfaceVariant = defaultTheme.surfaceVariant,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = defaultTheme.text,
            onSurface = defaultTheme.text,
            onSurfaceVariant = defaultTheme.text.copy(alpha = 0.78f)
        )
    }

    CompositionLocalProvider(LocalLiveonTheme provides defaultTheme) {
        MaterialTheme(
            colorScheme = cs,
            typography = Typography(),
            shapes = LiveonShapes,
            content = content
        )
    }
}