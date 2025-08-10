package com.liveongames.liveon.ui.screens.education

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun GpaInfoDialog(show: Boolean, onDismiss: () -> Unit) {
    if (!show) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("GPA Details") },
        text = {
            Text(
                "Your GPA rises when you attend class (+0.10), do homework (+0.15), and study (+0.20). " +
                        "Only the first three uses of the same action before you age up grant benefit (diminishing returns). " +
                        "GPA is clamped between 0.00 and 4.00."
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
    )
}
