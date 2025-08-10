package com.liveongames.liveon.ui.screens.education

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.domain.model.Education
import com.liveongames.domain.model.EducationLevel
import com.liveongames.liveon.R
import com.liveongames.liveon.model.EducationCourse
import java.text.SimpleDateFormat
import java.util.Locale

private val HonorsThreshold = 3.7

private enum class AchvFilter(val label: String) { ALL("All"), CERTS("Certificates"), DEGREES("Degrees"), HONORS("Honors"), RECENT("Recent") }

private data class DiplomaUi(
    val id: String, val name: String, val level: EducationLevel, val gpa: Double,
    val completedAt: Long?, val durationMonths: Int, val flavor: String?, val boosts: List<String>
)

@Composable
fun EducationAchievementsShelf(completed: List<Education>, modifier: Modifier = Modifier, courseResolver: (String) -> EducationCourse? = { null }) {
    val text = colorResource(id = R.color.white)
    val shelfCard = colorResource(id = R.color.slate_900)
    var activeFilter by remember { mutableStateOf(AchvFilter.ALL) }
    var selected: DiplomaUi? by remember { mutableStateOf(null) }

    val uis = remember(completed) {
        completed.sortedByDescending { it.completionDate ?: 0L }.map { edu ->
            val course = courseResolver(edu.id)
            DiplomaUi(
                id = edu.id, name = edu.name, level = edu.level, gpa = edu.currentGPA, completedAt = edu.completionDate,
                durationMonths = edu.duration, flavor = course?.flavorText ?: edu.description, boosts = course?.careerBoosts ?: emptyList()
            )
        }
    }

    val filtered = remember(uis, activeFilter) {
        when (activeFilter) {
            AchvFilter.ALL -> uis
            AchvFilter.CERTS -> uis.filter { it.level == EducationLevel.CERTIFICATION }
            AchvFilter.DEGREES -> uis.filter { it.level in setOf(EducationLevel.ASSOCIATE, EducationLevel.BACHELOR, EducationLevel.MASTER, EducationLevel.DOCTORATE) }
            AchvFilter.HONORS -> uis.filter { it.gpa >= HonorsThreshold }
            AchvFilter.RECENT -> { val cutoff = System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000; uis.filter { (it.completedAt ?: 0L) >= cutoff } }
        }
    }

    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = shelfCard), shape = CardDefaults.shape) {
        Column(Modifier.padding(14.dp)) {
            Text("Diplomas & Certificates", color = text, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AchvFilter.values().forEach { f -> FilterChip(selected = activeFilter == f, onClick = { activeFilter = f }, label = { Text(f.label) }) }
            }
            Spacer(Modifier.height(10.dp))

            if (uis.isEmpty()) { Text("No certificates earned yet.", color = text.copy(alpha = 0.85f)); return@Card }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered, key = { it.id }) { item ->
                    DiplomaCard(item = item, onClick = { selected = item })
                }
            }
        }
    }

    AnimatedVisibility(visible = selected != null, enter = fadeIn(), exit = fadeOut()) {
        selected?.let { item -> DiplomaDetailDialog(item = item, onDismiss = { selected = null }) }
    }
}

@Composable
private fun DiplomaCard(item: DiplomaUi, onClick: () -> Unit) {
    val text = colorResource(id = R.color.white)
    Card(onClick = onClick) {
        Column(Modifier.padding(12.dp).widthIn(min = 220.dp)) {
            Text(item.name, color = text, fontWeight = FontWeight.SemiBold)
            val sdf = remember { SimpleDateFormat("yyyy", Locale.getDefault()) }
            Text("Year: " + (item.completedAt?.let { sdf.format(it) } ?: "—"), color = text.copy(alpha = 0.85f))
            Text("Level: ${item.level.displayName}", color = text.copy(alpha = 0.85f))
            Text("GPA: ${"%.2f".format(item.gpa)}", color = text.copy(alpha = 0.85f))
        }
    }
}

@Composable
private fun DiplomaDetailDialog(item: DiplomaUi, onDismiss: () -> Unit) {
    val bg = colorResource(id = R.color.slate_900); val fg = colorResource(id = R.color.white)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.name, color = fg) },
        text = {
            Column {
                Text("Level: ${item.level.displayName}", color = fg)
                Text("GPA: ${"%.2f".format(item.gpa)}", color = fg)
                Text("Duration: ${monthsToNiceString(item.durationMonths)}", color = fg)
                item.flavor?.takeIf { it.isNotBlank() }?.let { Text(it, color = fg.copy(alpha = 0.9f)) }
                if (item.boosts.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Text("Career Boosts", color = fg, fontWeight = FontWeight.SemiBold); item.boosts.forEach { Text("• $it", color = fg) } }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        containerColor = bg
    )
}
