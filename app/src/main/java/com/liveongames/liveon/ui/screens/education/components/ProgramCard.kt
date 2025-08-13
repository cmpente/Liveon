// app/src/main/java/com/liveongames/liveon/ui/screens/education/components/ProgramCard.kt
package com.liveongames.liveon.ui.screens.education.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.domain.model.EducationProgram
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LocalLiveonTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramCard(
    course: EducationProgram,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    borderColor: Color? = null
) {
    val theme = LocalLiveonTheme.current

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor ?: theme.surfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_school),
                    contentDescription = null,
                    tint = theme.primary
                )
                Spacer(Modifier.width(10.dp))
                Text(course.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = theme.text)
            }
            Spacer(Modifier.height(8.dp))
            Text("Minimum GPA: ${"%.2f".format(course.minGpa)}",
                style = MaterialTheme.typography.bodySmall,
                color = theme.text.copy(alpha = 0.85f))
            Text("Tuition: $${course.tuition}",
                style = MaterialTheme.typography.bodySmall,
                color = theme.text.copy(alpha = 0.8f))
        }
    }
}
