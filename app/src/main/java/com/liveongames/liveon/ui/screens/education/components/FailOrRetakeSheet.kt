// app/src/main/java/com/liveongames/liveon/ui/screens/education/components/FailOrRetakeSheet.kt
package com.liveongames.liveon.ui.screens.education.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.liveon.ui.theme.LocalLiveonTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FailOrRetakeSheet(onRetake: () -> Unit, onFail: () -> Unit) {
    val theme = LocalLiveonTheme.current

    ModalBottomSheet(
        // --- CORRECTED: Dismissal is controlled by ViewModel state, not user swipe here ---
        onDismissRequest = { /* Handled by ViewModel */ }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Graduation Failed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = theme.text
            )
            Text(
                "Your GPA is too low to graduate.",
                style = MaterialTheme.typography.bodyMedium,
                color = theme.text
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onRetake()
                    // ViewModel will update state to hide this sheet
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
            ) {
                // --- CORRECTED: Use theme token for text color ---
                Text("Retake Program", color = theme.text)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    onFail()
                    // ViewModel will update state to hide this sheet
                },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, theme.secondary) // Use theme.secondary for border
            ) {
                // --- CORRECTED: Use theme token for text color ---
                Text("Fail & Drop Out", color = theme.text)
            }
        }
    }
}