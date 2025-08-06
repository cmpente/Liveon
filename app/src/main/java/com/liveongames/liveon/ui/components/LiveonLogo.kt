// app/src/main/java/com/liveongames/liveon/ui/components/LiveonLogo.kt
package com.liveongames.liveon.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LiveonLogo(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Simple, clean infinity symbol in neutral color
        Box(
            modifier = Modifier
                .size(64.dp)
        ) {
            // Capture the primary color in composable context
            val primaryColor = MaterialTheme.colorScheme.primary

            Canvas(modifier = Modifier.size(64.dp)) {
                val path = Path().apply {
                    // Draw infinity symbol
                    moveTo(size.width * 0.3f, size.height * 0.5f)
                    cubicTo(
                        size.width * 0.4f, size.height * 0.3f,
                        size.width * 0.6f, size.height * 0.7f,
                        size.width * 0.7f, size.height * 0.5f
                    )
                    cubicTo(
                        size.width * 0.6f, size.height * 0.3f,
                        size.width * 0.4f, size.height * 0.7f,
                        size.width * 0.3f, size.height * 0.5f
                    )
                }
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Liveon Text - clean and professional
        Text(
            text = "Liveon",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Life Without Limits",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}