// app/src/main/java/com/liveongames/liveon/ui/screens/education/components/ProgramCard.kt
package com.liveongames.liveon.ui.screens.education.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.domain.model.EducationProgram
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LocalLiveonTheme

@Composable
fun ProgramCard(
    course: EducationProgram,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalLiveonTheme.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_school),
                    contentDescription = null,
                    tint = theme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = theme.text
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Minimum GPA: ${"%.2f".format(course.minGpa)}",
                style = MaterialTheme.typography.bodySmall,
                color = theme.text.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Tuition: $${course.tuition}",
                style = MaterialTheme.typography.bodySmall,
                color = theme.text.copy(alpha = 0.8f)
            )
        }
    }
}