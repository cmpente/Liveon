package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.domain.model.EducationLevel
import com.liveongames.liveon.R
import com.liveongames.liveon.model.EducationCourse
import com.liveongames.liveon.viewmodel.EducationViewModel

@Composable
fun EducationPathMap(
    courses: List<EducationCourse>,
    lockInfo: (EducationCourse) -> EducationViewModel.ActionLock,
    onEnroll: (EducationCourse) -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = colorResource(id = R.color.slate_900)
    Column(modifier.background(bg).verticalScroll(rememberScrollState()).padding(12.dp)) {
        courses.groupBy { it.level }.toSortedMap(compareBy { it.ordinal }).forEach { (level, list) ->
            SectionHeader(level)
            list.forEach { course ->
                val lock = lockInfo(course)
                CourseCard(course, locked = lock.locked, lockReason = lock.reason, onEnroll = { onEnroll(course) })
            }
        }
    }
}

@Composable
private fun SectionHeader(level: EducationLevel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painter = painterResource(id = levelToCrest(level)), contentDescription = null, tint = colorResource(id = R.color.white))
        Spacer(Modifier.width(8.dp))
        Text(level.displayName, color = colorResource(id = R.color.white), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun CourseCard(course: EducationCourse, locked: Boolean, lockReason: String?, onEnroll: () -> Unit) {
    val cardBg = colorResource(id = R.color.slate_800)
    val text = colorResource(id = R.color.white)
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(containerColor = cardBg), shape = CardDefaults.shape) {
        Column(Modifier.padding(14.dp)) {
            Text(course.name, color = text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(course.flavorText, color = text.copy(alpha = 0.9f))
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("GPA ≥ ${"%.2f".format(course.requiredGpa)}") }, enabled = false)
                AssistChip(onClick = {}, label = { Text("$${course.cost}/yr") }, enabled = false)
                AssistChip(onClick = {}, label = { Text(monthsToNiceString(course.durationMonths)) }, enabled = false)
            }
            Spacer(Modifier.height(8.dp))
            if (locked) Text(lockReason ?: "Locked", color = colorResource(id = R.color.indigo_300))
            else Button(onClick = onEnroll) { Text("Enroll") }
        }
    }
}
