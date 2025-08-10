package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liveongames.liveon.R
import com.liveongames.liveon.model.Education
import com.liveongames.liveon.model.TermState

@Composable
fun EducationProfileCard(
    overallGpa: Double,
    activeEducation: Education?,
    termState: TermState?,
    modifier: Modifier = Modifier,
    onInfoTap: () -> Unit = {},
    onCompleteTap: () -> Unit = {}
) {
    Column(
        modifier
            .background(colorResource(R.color.slate_900), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(R.drawable.ic_crest), contentDescription = null, tint = colorResource(R.color.indigo_500))
            Spacer(Modifier.width(8.dp))
            Text("Prestige Academia", color = colorResource(R.color.white), fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text("GPA: ${"%.2f".format(overallGpa)}", style = MaterialTheme.typography.titleMedium)
        activeEducation?.let { edu ->
            Spacer(Modifier.height(6.dp))
            Text("Enrolled: ${edu.name}", style = MaterialTheme.typography.bodyMedium)
        }
        termState?.let { ts ->
            Spacer(Modifier.height(4.dp))
            Text("Term: Week ${ts.weekOfTerm} • Progress ${"%.0f".format(ts.progress * 100)}%", style = MaterialTheme.typography.bodySmall)
        }
    }
}