// app/src/main/java/com/liveongames/liveon/ui/screens/education/components/TimelineRail.kt
package com.liveongames.liveon.ui.screens.education.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.liveongames.domain.model.AcademicSchema
import com.liveongames.domain.model.periodFromProgress
import com.liveongames.liveon.ui.theme.LocalLiveonTheme

@Composable
fun TimelineRail(
    schema: AcademicSchema?,
    progressPct: Int,
    modifier: Modifier = Modifier
) {
    val theme = LocalLiveonTheme.current
    val totalPeriods = schema?.totalPeriods ?: 8
    // Ensure comparison consistency by lowercasing the JSON value
    val displayPeriodName = schema?.displayPeriodName?.lowercase() ?: "period"
    val currentPeriod = periodFromProgress(progressPct, totalPeriods)

    val labelPrefix = when (displayPeriodName) {
        "semester" -> "S"
        "quarter" -> "Q"
        "milestone" -> "M"
        else -> "P"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(theme.surfaceElevated),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(totalPeriods) { index ->
            val periodNumber = index + 1
            val isActive = periodNumber == currentPeriod
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (isActive) theme.primary else theme.surfaceVariant,
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // --- CORRECTED: Use theme tokens for text color ---
                Text(
                    text = if (displayPeriodName in listOf("semester", "quarter", "milestone")) {
                        "$labelPrefix$periodNumber"
                    } else {
                        "$periodNumber"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    // --- Use theme.text with alpha for contrast ---
                    color = if (isActive) theme.text.copy(alpha = 0.9f) else theme.text.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
    }
}