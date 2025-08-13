// app/src/main/java/com/liveongames/liveon/ui/screens/education/ActiveEducationActionBar.kt
package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// Import for theme
import com.liveongames.liveon.ui.theme.LocalLiveonTheme
// Import for drawable
import com.liveongames.liveon.R
// Import data models
import com.liveongames.data.model.education.EducationActionDef
import com.liveongames.domain.model.EducationProgram // Updated import

@Composable
fun ActiveEducationActionBar(
    actions: List<EducationActionDef>,
    course: EducationProgram, // Updated type
    isActionLocked: (EducationActionDef) -> Boolean,
    isOnCooldown: (EducationActionDef) -> Boolean,
    cooldownProgress: (EducationActionDef) -> Float,
    capRemaining: (EducationActionDef) -> Int,
    onActionClick: (EducationActionDef) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalLiveonTheme.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.surface, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text("Study Actions", fontWeight = FontWeight.Bold, color = theme.text)
        Spacer(Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(actions) { def ->
                // Stabilize lambdas to prevent recomposition issues
                // Ref: [dev.to](https://dev.to/theplebdev/the-refactors-i-did-to-stop-my-jetpack-compose-lazycolumn-from-constantly-recomposing-57l0#stable)
                val locked = remember(def) { isActionLocked(def) }
                val cooldown = remember(def) { isOnCooldown(def) }
                val progress = remember(def) { cooldownProgress(def) }
                val handleClick = remember(def) { { onActionClick(def) } }

                ActionChip(
                    def = def,
                    enabled = !(locked || cooldown),
                    progress = if (cooldown) progress else 0f,
                    locked = locked,
                    onClick = handleClick
                )
            }
        }
    }
}

@Composable
private fun ActionChip(
    def: EducationActionDef,
    enabled: Boolean,
    progress: Float,
    locked: Boolean,
    onClick: () -> Unit
) {
    val theme = LocalLiveonTheme.current
    val color = if (enabled) theme.primary else theme.secondary

    Column(
        modifier = Modifier
            .width(100.dp)
            .background(theme.surfaceElevated, RoundedCornerShape(14.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Use stable drawableResId param to reduce recomposition
            // Ref: [engineering.teknasyon.com](https://engineering.teknasyon.com/reduce-recomposition-for-images-icons-in-jetpack-compose-8d2dd3bfa933?gi=8573bed2b0b3)
            Icon(
                painter = painterResource(id = R.drawable.ic_default_study),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            // ✅ Use .title instead of .name
            Text(
                text = def.title,
                modifier = Modifier.padding(start = 8.dp),
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        if (locked) {
            Text(
                text = "Locked",
                color = theme.accent,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            // Show real preview effect from minGpa..maxGpa if available
            val gpaChange = if (def.gpaDeltaMin != 0.0 || def.gpaDeltaMax != 0.0) {
                "+${"%.2f".format(def.gpaDeltaMin)}–${"%.2f".format(def.gpaDeltaMax)} GPA"
            } else {
                "No GPA change"
            }
            Text(
                text = "$gpaChange • ${def.cooldownMinutes}m",
                color = theme.text.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .alpha(0.9f)
                    .padding(top = 4.dp)
            )
        }

        // Visible cooldown progress bar
        if (progress in 0.01f..0.99f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(top = 6.dp)
                    .background(theme.surfaceVariant, RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(theme.accent, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}