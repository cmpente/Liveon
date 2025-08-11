package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.domain.model.Education
import com.liveongames.liveon.R
import com.liveongames.liveon.model.TermState
import com.liveongames.liveon.ui.theme.LiveonTheme

@Composable
fun EducationProfileCard(
    overallGpa: Double,
    activeEducation: Education?,
    termState: TermState?,
    theme: LiveonTheme,
    modifier: Modifier = Modifier,
    onInfoTap: () -> Unit = {},
    onCompleteTap: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .background(theme.surface, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_crest),
                contentDescription = null,
                tint = theme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Prestige Academia",
                color = theme.text,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "GPA: ${"%.2f".format(overallGpa)}",
                style = MaterialTheme.typography.titleMedium,
                color = theme.text
            )
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onInfoTap) {
                Text("ℹ️", color = theme.accent)
            }
        }
        activeEducation?.let { edu ->
            Spacer(Modifier.height(6.dp))
            Text("Enrolled: ${edu.name}", color = theme.text)
        }
        termState?.let { ts ->
            Spacer(Modifier.height(4.dp))
            Text(
                "Term: Week ${ts.weekOfTerm} • Progress ${"%.0f".format(ts.progress * 100)}%",
                style = MaterialTheme.typography.bodySmall,
                color = theme.text.copy(alpha = 0.8f)
            )
        }

        if (activeEducation?.completionDate != null) {
            Spacer(Modifier.height(8.dp))
            Button(onClick = onCompleteTap, colors = ButtonDefaults.buttonColors(containerColor = theme.primary)) {
                Text("Complete Course")
            }
        }
    }
}