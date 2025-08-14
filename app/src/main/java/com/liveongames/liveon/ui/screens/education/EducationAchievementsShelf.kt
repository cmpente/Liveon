package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.data.model.education.EducationCourse
import com.liveongames.domain.model.education.EducationAchievement
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LiveonTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EducationAchievementsShelf(
    completed: List<EducationAchievement>,
    theme: LiveonTheme,
    modifier: Modifier = Modifier,
    courseResolver: (String) -> EducationCourse?
) {
    Column(modifier = modifier) {
        Text("Diplomas", fontWeight = FontWeight.Bold, color = theme.text)
        Spacer(Modifier.height(8.dp))

        if (completed.isEmpty()) {
            Text("No diplomas yet.", color = theme.text.copy(alpha = 0.7f))
            return
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(completed) { achievement ->
                DiplomaCard(achievement, theme)
            }
        }
    }
}

@Composable
fun DiplomaCard(achievement: EducationAchievement, theme: LiveonTheme) {
    Card {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(R.drawable.ic_diploma), contentDescription = "Diploma")
            Text(achievement.name, fontWeight = FontWeight.Bold, color = theme.text)
            achievement.completionDate?.let {
                Text(
                    SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(it)),
                    color = theme.text.copy(alpha = 0.7f)
                )
            }
        }
    }
}