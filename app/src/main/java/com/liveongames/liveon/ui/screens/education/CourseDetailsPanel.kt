package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.liveon.R
import com.liveongames.liveon.model.EducationCourse
import com.liveongames.liveon.viewmodel.EducationViewModel

@Composable
fun CourseDetailsPanel(
    course: EducationCourse,
    lockInfo: EducationViewModel.ActionLock,
    onEnroll: () -> Unit,
    onDismiss: () -> Unit,
) {
    val bg = colorResource(id = R.color.slate_900)
    val fg = colorResource(id = R.color.white)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(levelToCrest(course.level)), contentDescription = null, tint = fg)
                Spacer(Modifier.width(8.dp))
                Text(course.name, color = fg, fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column {
                Text(course.flavorText, color = fg)
                Spacer(Modifier.height(8.dp))
                Text("Duration: ${monthsToNiceString(course.durationMonths)}", color = fg)
                Text("Cost/Year: $${course.cost}", color = fg)
                if (course.prerequisites.isNotEmpty()) Text("Prerequisites: ${course.prerequisites.joinToString()}", color = fg)
                if (course.milestones.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp)); Text("Milestones:", color = fg, fontWeight = FontWeight.SemiBold)
                    course.milestones.forEach { Text("• $it", color = fg) }
                }
            }
        },
        confirmButton = { if (!lockInfo.locked) Button(onClick = onEnroll) { Text("Enroll") } else Text(lockInfo.reason ?: "Locked", color = colorResource(id = R.color.indigo_300)) },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        containerColor = bg
    )
}
