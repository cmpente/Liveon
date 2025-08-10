// app/src/main/java/com/liveongames/liveon/ui/screens/education/CourseDetailsPanel.kt
package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            // Header with close button
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
                        contentDescription = "Close",
                        tint = theme.text
                    )
                }
            }

            // Tier badge and crest
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_crest),
                    contentDescription = null,
                    tint = theme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = course.level.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("💰 Cost", "$${course.cost}", theme)
                StatItem("⏱ Duration", "${course.duration} months", theme)
                if (course.requiredGPA > 0) {
                    StatItem("🎓 Required GPA", "${course.requiredGPA}", theme)
                }
            }

            // Flavor text
            Text(
                text = course.description,
                style = MaterialTheme.typography.bodyMedium,
                color = theme.accent,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Validation messages
            if (!course.canEnroll) {
                Text(
                    text = course.validationMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // Action buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEnroll,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = course.canEnroll,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (course.canEnroll) theme.primary else Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (course.isActive) "Continue Education" else "Enroll",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (course.isActive) {
                    OutlinedButton(
                        onClick = onDropOut,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Text(
                            text = "Drop Out",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    theme: LiveonTheme
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = theme.accent
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = theme.text,
            fontWeight = FontWeight.Medium
        )
    }
}

// Extension properties for Education model
val Education.canEnroll: Boolean
    get() = true // Implement actual logic based on prerequisites and funds

val Education.validationMessage: String
    get() = if (!canEnroll) "Prerequisites not met" else ""

val Education.isActive: Boolean
    get() = false // Implement based on your active education logic