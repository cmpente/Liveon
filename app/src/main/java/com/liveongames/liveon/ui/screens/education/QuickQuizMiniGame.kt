package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun QuickQuizMiniGame(
    numQuestions: Int,
    onClose: () -> Unit,
    onResult: (Boolean, Double) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Gray)) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Quick Quiz ($numQuestions Questions)", color = Color.White)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { onResult(true, 1.0); onClose() }) {
                Text("Finish Quiz")
            }
        }
    }
}