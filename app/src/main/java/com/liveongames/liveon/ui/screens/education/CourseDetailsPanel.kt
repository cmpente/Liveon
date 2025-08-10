// app/src/main/java/com/liveongames/liveon/ui/screens/education/CourseDetailsPanel.kt
package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.domain.model.Education
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LiveonTheme

@Composable
fun CourseDetailsPanel(
    course: Education,
    theme: LiveonTheme,
    onEnroll: () -> Unit,
    onContinue: () -> Unit,
    onDropOut: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = theme.text,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = null,
                        tint = colorResource(R.color.indigo_200),
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.surfaceElevated)
                            .padding(2.dp)
                    )
                }
            }

            Spacer(Modifier.size(8.dp))

            // Quick stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("💰 Cost", "$${course.cost}", theme)
                StatItem("⏱ Duration", "${course.duration} months", theme)
                if (course.requiredGPA > 0) {
                    StatItem("🎓 Required GPA", "${"%.2f".format(course.requiredGPA)}", theme)
                }
            }

            // Description
            Text(
                text = course.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.indigo_100),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.size(12.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEnroll,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    modifier = Modifier.weight(1f)
                ) { Text("Enroll") }

                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accent),
                    modifier = Modifier.weight(1f)
                ) { Text("Continue") }

                Button(
                    onClick = onDropOut,
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.indigo_400)),
                    modifier = Modifier.weight(1f)
                ) { Text("Drop out") }
            }
        }
    }
}

@Composable
private fun StatItem(title: String, value: String, theme: LiveonTheme) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, color = colorResource(R.color.indigo_200), style = MaterialTheme.typography.labelSmall)
        Text(text = value, color = colorResource(R.color.indigo_100), style = MaterialTheme.typography.bodySmall)
    }
}
