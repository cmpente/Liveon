// app/src/main/java/com/liveongames/liveon/ui/theme/Theme.kt

package com.liveongames.liveon.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. Define the CompositionLocal for your custom theme
val LocalLiveonTheme = staticCompositionLocalOf<LiveonTheme> {
    error("No LiveonTheme provided! Wrap your content with LiveonTheme().")
}

// Fallback Material Color Schemes (using some of your base colors)
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor, // From Color.kt
    onPrimary = androidx.compose.ui.graphics.Color.White, // Or theme-specific onPrimary if you define it
    secondary = SecondaryColor,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = BackgroundColor,
    surface = CardBackgroundColor,
    onSurface = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = SecondaryColor,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = BackgroundColor, // Might need light variant
    surface = CardBackgroundColor, // Might need light variant
    onSurface = TextPrimary // Might need light variant
)

// 2. Main Theme Wrapper Composable
@Composable
fun LiveonTheme(
    liveonTheme: LiveonTheme = PremiumSleek, // Use selected theme
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic for custom control
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    // 3. Provide your custom theme data
    CompositionLocalProvider(LocalLiveonTheme provides liveonTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = androidx.compose.material3.Typography(), // Use default or your custom Typography
            content = content
        )
    }
}