package com.liveongames.liveon.ui.screens.minigame

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuickQuizMiniGame(numQuestions: Int = 3, onClose: () -> Unit, onResult: (correct: Int, tierMultiplier: Double) -> Unit) {
    var correct by remember { mutableStateOf(0) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Quick Quiz") },
        text = { Column { Text("Answer a few rapid questions.") ; Text("Correct so far: $correct/$numQuestions") } },
        confirmButton = {
            Button(onClick = {
                val mul = when {
                    correct >= numQuestions -> 1.6
                    correct >= (numQuestions - 1) -> 1.35
                    correct >= (numQuestions - 2) -> 1.2
                    else -> 1.0
                }
                onResult(correct, mul)
            }) { Text("Submit") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { if (correct < numQuestions) correct += 1 }) { Text("Mark Correct") }
                TextButton(onClick = onClose) { Text("Cancel") }
            }
        }
    )
}
