package com.liveongames.liveon.ui.screens.education.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LocalLiveonTheme
import com.liveongames.liveon.viewmodel.EducationUiState

@Composable
fun HeaderCard(
    state: EducationUiState,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enrollment = state.enrollment
    val theme = LocalLiveonTheme.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_education),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = theme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    val title = state.programs.firstOrNull { it.id == enrollment?.programId }?.title
                        ?: "Education"
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = theme.text
                    )
                }
                TextButton(onClick = onInfoClick) { Text("GPA", color = theme.text) }
            }

            Spacer(Modifier.height(8.dp))

            if (enrollment == null) {
                Text(
                    "Not enrolled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.text.copy(alpha = 0.8f)
                )
            } else {
                Column {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Grade: ${state.grade}%") }
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text("Progress: ${enrollment.progressPct}%") }
                        )
                    }
                }
            }
        }
    }
}
