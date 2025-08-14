package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.data.model.education.EducationActionDef

@Composable
fun ActionChoiceSheet(
    action: EducationActionDef,
    onChoose: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val step = action.dialog.firstOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                action.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Surface(tonalElevation = 1.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!step?.text.isNullOrBlank()) {
                        Text(step!!.text, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(4.dp))

                    val choices = step?.choices.orEmpty()
                    if (choices.isEmpty()) {
                        Text("No choices available.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        choices.forEach { choice ->
                            Button(
                                onClick = { onChoose(choice.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(choice.label)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
