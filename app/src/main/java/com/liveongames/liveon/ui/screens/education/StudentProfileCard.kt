package com.liveongames.liveon.ui.screens.education

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StudentProfileCard(
    playerAvatar: ImageVector, // Or use Painter/Drawable for more complex avatars
    gpa: Float,
    academicStanding: String,
    institutionName: String,
    termDetails: String,
    progressPercentage: Int,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    shadowElevation: Int = 4
) {
    var expanded by remember { mutableStateOf(false) }
    val progress = animateFloatAsState(targetValue = progressPercentage / 100f).value

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(elevation = shadowElevation.dp, shape = shape)
            .clip(shape)
            .clickable { expanded = !expanded }
            .animateContentSize(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player Avatar
                Image(
                    imageVector = playerAvatar, // Use your actual avatar source here
                    contentDescription = "Player Avatar",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)) // Example rounded avatar corners
                        .background(MaterialTheme.colorScheme.primary) // Placeholder background
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Institution Name
                    Text(
                        text = institutionName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Term Details
                    Text(
                        text = termDetails,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // GPA
                    Text(
                        text = "GPA: %.2f".format(gpa),
                        style = MaterialTheme.typography.titleSmall
                    )
                    // Academic Standing
                    Text(
                        text = "Standing: $academicStanding",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Progress Percentage Text
            Text(
                text = "$progressPercentage% Complete",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )

            // Expanded Content
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Optional school info here",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    // TODO: Add actual school-specific info here (e.g., enrolled classes, major)
                }
            }
        }
    }
}

// Example Usage (for preview)
// @Preview
// @Composable
// fun PreviewStudentProfileCard() {
//     LiveonTheme { // Assuming you have a theme
//         StudentProfileCard(
//             playerAvatar = Icons.Default.Person, // Replace with your avatar
//             gpa = 3.85f,
//             academicStanding = "A",
//             institutionName = "University of Life",
//             termDetails = "Semester 2, Year 1",
//             progressPercentage = 75
//         )
//     }
// }