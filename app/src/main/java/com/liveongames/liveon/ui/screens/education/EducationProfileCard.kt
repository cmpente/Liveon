package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.data.db.entity.TermStateEntity
import com.liveongames.domain.model.Education
import com.liveongames.liveon.R

@Composable
fun EducationProfileCard(
    overallGpa: Double,
    activeEducation: Education?,
    termState: TermStateEntity?,
    modifier: Modifier = Modifier,
    onInfoTap: () -> Unit = {},
    onCompleteTap: () -> Unit = {}
) {
    val bg = colorResource(id = R.color.slate_900)
    val fg = colorResource(id = R.color.white)
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(id = R.drawable.ic_education), contentDescription = null, tint = fg)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Prestige Academia", color = fg, style = MaterialTheme.typography.titleMedium)
                Text("GPA: ${"%.2f".format(overallGpa)}", color = fg, style = MaterialTheme.typography.bodyLarge)
                activeEducation?.let {
                    Text("Active: ${it.name}", color = fg.copy(alpha=0.9f), style = MaterialTheme.typography.bodyMedium)
                }
                termState?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("Phase: ${it.coursePhase} • Focus: ${it.focus}% • Streak: ${it.streakDays}d", color = fg.copy(alpha=0.85f), style = MaterialTheme.typography.bodySmall)
                }
            }
            TextButton(onClick = onInfoTap) { Text("GPA Info") }
            if (activeEducation != null) Button(onClick = onCompleteTap, modifier = Modifier.padding(start = 6.dp)) { Text("Complete") }
        }
    }
}
