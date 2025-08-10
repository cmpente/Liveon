// app/src/main/java/com/liveongames/liveon/ui/screens/education/EducationAchievementsShelf.kt
package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.domain.model.Education
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LiveonTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EducationAchievementsShelf(
    educations: List<Education>,
    theme: LiveonTheme,
    modifier: Modifier = Modifier
) {
    val completed = educations.filter { it.completionDate != null }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.surface, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Diplomas & Certificates",
            color = theme.text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        if (completed.isEmpty()) {
            Text(
                text = "No diplomas earned yet. Keep studying!",
                color = colorResource(R.color.indigo_100),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.alpha(0.9f)
            )
            return@Column
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(completed, key = { it.id }) { edu ->
                DiplomaItem(edu, theme)
            }
        }
    }
}

@Composable
private fun DiplomaItem(edu: Education, theme: LiveonTheme) {
    Column(
        modifier = Modifier
            .background(theme.surfaceElevated, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_diploma),
                contentDescription = null
            )
            Text(
                text = edu.name,
                color = theme.text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = completionString(edu),
            color = colorResource(R.color.indigo_400),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private fun completionString(edu: Education): String {
    val ts = edu.completionDate ?: return "In progress"
    val fmt = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    return "Earned ${fmt.format(Date(ts))}"
}
