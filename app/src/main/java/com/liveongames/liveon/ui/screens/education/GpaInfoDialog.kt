package com.liveongames.liveon.ui.screens.education

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun GpaInfoDialog(show: Boolean, onDismiss: () -> Unit) {
    if (!show) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("GPA Details") },
        text = { Text("Your GPA rises when you complete study actions. Each action has a cooldown (seconds/minutes) and a per-age cap. Performance in mini-games grants higher multipliers. GPA is clamped 0.00–4.00.") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
    )
}
