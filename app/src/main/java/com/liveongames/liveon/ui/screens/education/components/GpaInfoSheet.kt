// app/src/main/java/com/liveongames/liveon/ui/screens/education/components/GpaInfoSheet.kt
package com.liveongames.liveon.ui.screens.education.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.liveon.ui.theme.LocalLiveonTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpaInfoSheet(onDismiss: () -> Unit) {
    val theme = LocalLiveonTheme.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text(
                "What is GPA?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = theme.text
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "GPA stands for Grade Point Average. It ranges from 0.00 (failing) to 4.00 (perfect). Perform well to meet program requirements and graduate.",
                style = MaterialTheme.typography.bodyMedium,
                color = theme.text
            )
        }
    }
}