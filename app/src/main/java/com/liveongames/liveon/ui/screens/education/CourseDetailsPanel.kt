// app/src/main/java/com/liveongames/liveon/ui/screens/education/CourseDetailsPanel.kt
package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.liveongames.domain.model.EducationProgram
import com.liveongames.data.model.education.EducationLockInfo // Import the correct LockInfo
import com.liveongames.liveon.ui.theme.LiveonTheme

@Composable
fun CourseDetailsPanel(
    course: EducationProgram,
    lockInfo: EducationLockInfo,
    theme: LiveonTheme,
    onEnroll: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(theme.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(course.title, fontWeight = FontWeight.Bold, color = theme.text)
            Spacer(Modifier.height(8.dp))
            Text("Cost: \$${course.tuition}", color = theme.text)
            Text("Level: ${course.tier.name}", color = theme.text)
            Spacer(Modifier.height(8.dp))
            if (lockInfo.locked) {
                Text("Cannot enroll: ${lockInfo.reason}", color = theme.secondary)
            } else {
                Button(onClick = onEnroll, colors = ButtonDefaults.buttonColors(containerColor = theme.primary)) {
                    Text("Enroll Now")
                }
            }
        }
    }
}