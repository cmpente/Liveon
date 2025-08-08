package com.liveongames.liveon.ui.components

// Add this to a new file SirenBrush.kt or inside CrimeScreen.kt
import android.view.ViewConfiguration
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp

@Composable
fun rememberSirenBrush(
    enabled: Boolean = true
): Brush {
    val reducedMotion = isSystemInDarkTheme() // Fallback check for reduced motion
    val colors = if (enabled) {
        listOf(
            Color(0xFF3A9BDC), // metallic blue
            Color(0xFF9ED1FF), // highlight blue
            Color(0xFF3A9BDC), // metallic blue
            Color(0xFFFF4C4C), // metallic red
            Color(0xFFFFA3A3), // highlight red
            Color(0xFFFF4C4C), // metallic red
            Color(0xFF3A9BDC), // back to blue
        )
    } else {
        listOf(
            Color(0xFF3A9BDC),
            Color(0xFF9ED1FF),
            Color(0xFF3A9BDC),
            Color(0xFFFF4C4C),
            Color(0xFFFFA3A3),
            Color(0xFFFF4C4C),
            Color(0xFF3A9BDC),
        )
    }

    return if (reducedMotion || !enabled) {
        // Static diagonal striped gradient
        Brush.linearGradient(
            colors = colors,
            start = Offset.Zero,
            end = Offset(50f, 50f)
        )
    } else {
        // Animated sweep
        val transition = rememberInfiniteTransition(label = "siren")
        val offset by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sirenOffset"
        )

        Brush.linearGradient(
            colors = colors,
            start = Offset(offset * 100 - 100, 0f),
            end = Offset(offset * 100, 100f)
        )
    }
}