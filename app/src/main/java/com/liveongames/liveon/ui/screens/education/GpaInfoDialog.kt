package com.liveongames.liveon.ui.screens.education

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.liveongames.liveon.ui.theme.LiveonTheme

@Composable
fun GpaInfoDialog(theme: LiveonTheme, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("GPA System", style = MaterialTheme.typography.headlineSmall, color = theme.primary)
        },
        text = {
            Text(
                "Your GPA changes with actions (minute cooldown, zero returns after 3 consecutive taps):\n\n" +
                        "• Attend Lecture: small boost\n" +
                        "• Homework: small boost\n" +
                        "• Study: small boost\n\n" +
                        "Returns diminish after 3 consecutive uses of the same action.",
                color = theme.text
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK", color = theme.primary) }
        },
        containerColor = theme.surface
    )
}
