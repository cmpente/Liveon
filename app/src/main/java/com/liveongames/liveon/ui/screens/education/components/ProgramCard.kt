// app/src/main/java/com/liveongames/liveon/ui/screens/education/components/ProgramCard.kt
package com.liveongames.liveon.ui.screens.education.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.data.model.education.EducationCourse
import com.liveongames.liveon.ui.theme.LocalLiveonTheme

@Composable
fun ProgramCard(
    course: EducationCourse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalLiveonTheme.current

    Card(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = course.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = theme.text
            )
            Text(
                text = "GPA: ${"%.2f".format(course.minGpa)}",
                style = MaterialTheme.typography.bodySmall,
                color = theme.text.copy(alpha = 0.7f)
            )
        }
    }
}