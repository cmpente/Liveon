package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.liveongames.liveon.ui.theme.LiveonTheme
import com.liveongames.liveon.ui.theme.PremiumSleek

@Composable
fun GpaInfoDialog(show: Boolean, theme: LiveonTheme = PremiumSleek, onDismiss: () -> Unit) {
    if (!show) return

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(theme.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Understanding GPA", fontWeight = FontWeight.Bold, color = theme.text)
            Spacer(Modifier.height(12.dp))
            Text(
                "Your GPA reflects your academic performance over time.\n\n" +
                        "Higher GPAs unlock prestigious courses and special abilities.",
                color = theme.text
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = theme.primary)) {
                Text("Got It")
            }
        }
    }
}