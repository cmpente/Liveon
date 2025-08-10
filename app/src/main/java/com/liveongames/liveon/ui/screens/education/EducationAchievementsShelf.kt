package com.liveongames.liveon.ui.screens.education

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.liveongames.domain.model.Education
import com.liveongames.domain.model.EducationLevel
import com.liveongames.liveon.R
import com.liveongames.liveon.model.EducationCourse
import java.text.SimpleDateFormat
import java.util.Locale

private val HonorsThreshold = 3.7

private enum class AchvFilter(val label: String) {
    ALL("All"),
    CERTS("Certificates"),
    DEGREES("Degrees"),
    HONORS("Honors"),
    RECENT("Recent")
}

private data class DiplomaUi(
    val id: String,
    val name: String,
    val level: EducationLevel,
    val gpa: Double,
    val completedAt: Long?,
    val durationMonths: Int,
    val flavor: String?,
    val boosts: List<String>
)

@Composable
fun EducationAchievementsShelf(
    completed: List<Education>,
    modifier: Modifier = Modifier,
    // Optional resolver to enrich details from the catalog (boosts, flavor)
    courseResolver: (String) -> EducationCourse? = { null }
) {
    val text = colorResource(id = R.color.white)
    val ribbon = colorResource(id = R.color.indigo_500)
    val shelfCard = colorResource(id = R.color.slate_900)
    val diplomaCard = colorResource(id = R.color.slate_800)

    var activeFilter by remember { mutableStateOf(AchvFilter.ALL) }
    var selected: DiplomaUi? by remember { mutableStateOf(null) }

    val uis = remember(completed) {
        completed.sortedByDescending { it.completionDate ?: 0L }.map { edu ->
            val course = courseResolver(edu.id)
            DiplomaUi(
                id = edu.id,
                name = edu.name,
                level = edu.level,
                gpa = edu.currentGPA,
                completedAt = edu.completionDate,
                durationMonths = edu.duration,
                flavor = course?.flavorText ?: edu.description,
                boosts = course?.careerBoosts ?: emptyList()
            )
        }
    }

    val filtered = remember(uis, activeFilter) {
        when (activeFilter) {
            AchvFilter.ALL -> uis
            AchvFilter.CERTS -> uis.filter { it.level == EducationLevel.CERTIFICATION }
            AchvFilter.DEGREES -> uis.filter {
                it.level in setOf(
                    EducationLevel.ASSOCIATE,
                    EducationLevel.BACHELOR,
                    EducationLevel.MASTER,
                    EducationLevel.DOCTORATE
                )
            }
            AchvFilter.HONORS -> uis.filter { it.gpa >= HonorsThreshold }
            AchvFilter.RECENT -> {
                val cutoff = System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000 // 12 months
                uis.filter { (it.completedAt ?: 0L) >= cutoff }
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(containerColor = shelfCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        // Ribbon divider top
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(ribbon)
        )

        Column(Modifier.padding(14.dp)) {
            Text("Diplomas & Certificates", color = text, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AchvFilter.values().forEach { f ->
                    FilterChip(
                        selected = activeFilter == f,
                        onClick = { activeFilter = f },
                        label = { Text(f.label) }
                    )
                }
                Spacer(Modifier.weight(1f))
                AssistChip(
                    onClick = {},
                    label = { Text("${filtered.size}/${uis.size}") },
                    enabled = false,
                    colors = AssistChipDefaults.assistChipColors(
                        disabledContainerColor = colorResource(id = R.color.slate_700),
                        disabledLabelColor = text
                    )
                )
            }

            Spacer(Modifier.height(10.dp))

            if (uis.isEmpty()) {
                Text("No certificates earned yet.", color = text.copy(alpha = 0.85f))
                Spacer(Modifier.height(6.dp))
                Divider(color = colorResource(id = R.color.slate_700))
                Spacer(Modifier.height(6.dp))
                Text(
                    "Complete an education path to earn a diploma. Honors are awarded for GPA ≥ ${"%.1f".format(HonorsThreshold)}.",
                    color = text.copy(alpha = 0.7f)
                )
                return@Card
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered, key = { it.id }) { item ->
                    DiplomaCard(
                        item = item,
                        containerColor = diplomaCard,
                        onClick = { selected = item }
                    )
                }
            }
        }
    }

    AnimatedVisibility(
        visible = selected != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        selected?.let { item ->
            DiplomaDetailDialog(
                item = item,
                onDismiss = { selected = null }
            )
        }
    }
}

@Composable
private fun DiplomaCard(
    item: DiplomaUi,
    containerColor: Color,
    onClick: () -> Unit,
    width: Dp = 240.dp
) {
    val crest = levelToCrest(item.level)
    val honors = item.gpa >= HonorsThreshold
    val rotation by animateFloatAsState(if (honors) -12f else 0f, label = "stampRotation")

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(width)
            .heightIn(min = 120.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(Modifier.fillMaxWidth().padding(12.dp)) {
            Column(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = crest),
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        item.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.weight(1f))
                    val sdf = remember { SimpleDateFormat("yyyy", Locale.getDefault()) }
                    Text(
                        text = item.completedAt?.let { sdf.format(it) } ?: "—",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Level: ${item.level.displayName}",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "GPA: ${item.gpa.asGpa()}",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // HONORS stamp
            if (honors) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .rotate(rotation)
                        .background(Color(0x33FFD700), shape = CircleShape)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "HONORS",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun DiplomaDetailDialog(
    item: DiplomaUi,
    onDismiss: () -> Unit
) {
    val bg = colorResource(id = R.color.slate_900)
    val fg = colorResource(id = R.color.white)
    val crest = levelToCrest(item.level)
    val sdf = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = crest), contentDescription = null, tint = fg)
                Spacer(Modifier.width(8.dp))
                Text(item.name, color = fg, fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column {
                Text("Level: ${item.level.displayName}", color = fg)
                Text("GPA: ${item.gpa.asGpa()}", color = fg)
                Text(
                    "Completed: ${item.completedAt?.let { sdf.format(it) } ?: "—"}",
                    color = fg
                )
                Text("Duration: ${monthsToNiceString(item.durationMonths)}", color = fg)

                item.flavor?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = fg.copy(alpha = 0.9f))
                }

                if (item.boosts.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Career Boosts", color = fg, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    item.boosts.forEach { Text("• $it", color = fg) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        containerColor = bg
    )
}
