// app/src/main/java/com/liveongames/liveon/ui/screens/education/EducationPathMap.kt
package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.liveon.R
import com.liveongames.liveon.model.EducationCourse
import com.liveongames.liveon.model.EducationTier
import com.liveongames.liveon.ui.theme.LiveonTheme
import com.liveongames.liveon.viewmodel.EducationViewModel

@Composable
fun EducationPathMap(
    viewModel: EducationViewModel,
    onSelect: (EducationCourse) -> Unit,
    theme: LiveonTheme,
    modifier: Modifier = Modifier
) {
    val catalog by viewModel.catalog.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.surface, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Course Catalog",
            color = theme.text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(catalog, key = { it.id }) { course ->
                val lock = viewModel.courseLockInfo(course)
                CourseRow(
                    course = course,
                    theme = theme,
                    locked = lock.locked,
                    lockReason = lock.reason,
                    onClick = { if (!lock.locked) onSelect(course) }
                )
            }
        }
    }
}

@Composable
private fun CourseRow(
    course: EducationCourse,
    theme: LiveonTheme,
    locked: Boolean,
    lockReason: String?,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (locked) 0.6f else 1f)
            .clickable(enabled = !locked, onClick = onClick)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = course.iconRes),
                    contentDescription = null,
                    tint = theme.accent
                )
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = theme.text,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = course.flavorText,
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(R.color.indigo_100),
                modifier = Modifier.alpha(0.95f)
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val req = if (course.requiredGpa > 0.0) "• Req GPA ${"%.2f".format(course.requiredGpa)}" else ""
                Text(
                    text = "Tier ${course.tier.value} • ${course.durationMonths} mo $req",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorResource(R.color.indigo_400)
                )
                Text(
                    text = "$${course.cost}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorResource(R.color.indigo_400)
                )
            }

            if (locked && lockReason != null) {
                Text(
                    text = lockReason,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorResource(R.color.indigo_200),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
