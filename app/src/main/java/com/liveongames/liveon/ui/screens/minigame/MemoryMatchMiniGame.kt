package com.liveongames.liveon.ui.screens.minigame

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MemoryMatchMiniGame(onClose: () -> Unit, onResult: (matched: Int, tierMultiplier: Double) -> Unit) {
    var flips by remember { mutableStateOf(0) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Concept Match") },
        text = { Column { Text("Pretend to match pairs. Fewer flips = better.") ; Text("Flips: $flips") } },
        confirmButton = {
            Button(onClick = {
                val mul = when { flips <= 6 -> 1.3; flips <= 10 -> 1.1; else -> 1.0 }
                onResult(4, mul)
            }) { Text("Finish") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { flips += 1 }) { Text("Flip") }
                TextButton(onClick = onClose) { Text("Cancel") }
            }
        }
    )
}
