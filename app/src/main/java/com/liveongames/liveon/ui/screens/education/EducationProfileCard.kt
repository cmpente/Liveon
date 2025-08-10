package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LiveonTheme

@Composable
fun EducationProfileCard(
    playerName: String,
    highestTier: String,
    currentGPA: Double,
    milestone: String,
    theme: LiveonTheme,
    onCrestTap: () -> Unit,
    onGPATap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = playerName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = theme.text,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onCrestTap,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(
                                    theme.primary.copy(alpha = 0.3f),
                                    theme.primary.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_crest),
                        contentDescription = "View next tier requirements",
                        tint = theme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clickable { onGPATap() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "GPA:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = theme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = String.format("%.2f", currentGPA),
                        style = MaterialTheme.typography.bodyLarge,
                        color = theme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                val progress = (currentGPA / 4.0).toFloat().coerceIn(0f, 1f)

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(12.dp),
                    color = theme.primary,
                    trackColor = theme.surfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0.0", style = MaterialTheme.typography.bodySmall, color = theme.accent)
                    Text("4.0", style = MaterialTheme.typography.bodySmall, color = theme.accent)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.surfaceVariant)
                    .padding(12.dp)
            ) {
                Text(
                    text = milestone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.accent,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStatItem("Highest Tier", highestTier, theme)
                QuickStatItem("Programs", "—", theme)
                QuickStatItem("Scholarships", "—", theme)
            }
        }
    }
}

@Composable
private fun QuickStatItem(
    label: String,
    value: String,
    theme: LiveonTheme
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = theme.accent)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = theme.text, fontWeight = FontWeight.Medium)
    }
}
