package com.liveongames.liveon.ui.screens.minigame

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun TimingTapMiniGame(onClose: () -> Unit, onResult: (score: Int, tierMultiplier: Double) -> Unit) {
    var target by remember { mutableStateOf(Random.nextInt(10, 90)) }
    var slider by remember { mutableStateOf(50f) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Focus Burst") },
        text = {
            Column {
                Text("Stop near the target for a better tier.")
                Slider(value = slider, onValueChange = { slider = it }, valueRange = 0f..100f)
                Text("Target: $target  •  Current: ${slider.toInt()}")
            }
        },
        confirmButton = {
            Button(onClick = {
                val diff = abs(slider - target)
                val score = (100 - diff.toInt()).coerceIn(0, 100)
                val mul = when { score >= 90 -> 1.5; score >= 70 -> 1.25; score >= 40 -> 1.0; else -> 0.7 }
                onResult(score, mul)
            }) { Text("Lock In") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancel") } }
    )
}
