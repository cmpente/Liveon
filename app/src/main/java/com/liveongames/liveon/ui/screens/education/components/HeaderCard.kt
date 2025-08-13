// app/src/main/java/com/liveongames/liveon/ui/screens/education/components/HeaderCard.kt
package com.liveongames.liveon.ui.screens.education.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.domain.model.Enrollment // DOMAIN model
import com.liveongames.domain.model.EducationProgram // Use domain model
import com.liveongames.domain.model.groupFromPeriod
import com.liveongames.domain.model.periodFromProgress
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LocalLiveonTheme

@Composable
fun HeaderCard(
    enrollment: Enrollment?, // DOMAIN model
    programs: List<EducationProgram>, // Use domain model
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalLiveonTheme.current
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_education), // Ensure drawable exists
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = theme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    val title = enrollment?.let { e ->
                        programs.firstOrNull { it.id == e.programId }?.title // Use .title instead of .name
                    } ?: "Prestige Academia"

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = theme.text
                    )
                }

                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "GPA Info",
                        tint = theme.text
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (enrollment == null) {
                Text(
                    "Enrolled: Not enrolled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.text.copy(alpha = 0.8f)
                )
            } else {
                val p = periodFromProgress(enrollment.progressPct, enrollment.schema.totalPeriods)
                val g = groupFromPeriod(p, enrollment.schema.periodsPerYear)
                val groupLabel = enrollment.schema.groupingLabel ?: enrollment.schema.displayPeriodName
                val programTitle =
                    programs.firstOrNull { it.id == enrollment.programId }?.title ?: "Program" // Use .title

                Text(
                    "Enrolled: $programTitle",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.text.copy(alpha = 0.8f)
                )
                Text(
                    "Term: $groupLabel $g • ${enrollment.schema.displayPeriodName} $p • Progress ${enrollment.progressPct}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.text.copy(alpha = 0.8f)
                )
            }

            Spacer(Modifier.height(8.dp))
            TimelineRail(
                schema = enrollment?.schema,
                progressPct = enrollment?.progressPct ?: 0,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}