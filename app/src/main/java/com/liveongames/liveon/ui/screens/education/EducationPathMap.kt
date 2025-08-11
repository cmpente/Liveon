package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.liveon.model.EducationCourse
import com.liveongames.liveon.model.EducationLockInfo
import com.liveongames.liveon.ui.theme.LiveonTheme

@Composable
fun EducationPathMap(
    courses: List<EducationCourse>,
    lockInfo: (EducationCourse) -> EducationLockInfo,
    onEnroll: (EducationCourse) -> Unit,
    theme: LiveonTheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.surface, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text("Available Courses", fontWeight = FontWeight.Bold, color = theme.text)
        Spacer(Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(courses) { course ->
                val info = lockInfo(course)
                val color = if (info.locked) theme.secondary else theme.primary

                Card(
                    modifier = Modifier.width(140.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(course.name, fontWeight = FontWeight.SemiBold, color = theme.text)
                        Spacer(Modifier.height(6.dp))
                        if (info.locked) {
                            Text("🔒 Locked", color = theme.accent)
                        } else {
                            Button(onClick = { onEnroll(course) }, colors = ButtonDefaults.buttonColors(containerColor = color)) {
                                Text("Enroll")
                            }
                        }
                    }
                }
            }
        }
    }
}