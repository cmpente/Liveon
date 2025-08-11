// app/src/main/java/com/liveongames/liveon/ui/screens/education/ActiveEducationActionBar.kt
package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.liveon.R
import com.liveongames.liveon.model.EducationActionDef
import com.liveongames.liveon.model.EducationCourse
import com.liveongames.liveon.ui.theme.LiveonTheme
import kotlin.math.ceil

@Composable
fun ActiveEducationActionBar(
    actions: List<EducationActionDef>,
    course: EducationCourse,
    isActionLocked: (EducationActionDef) -> Boolean,
    isOnCooldown: (EducationActionDef) -> Boolean,
    cooldownProgress: (EducationActionDef) -> Float,
    capRemaining: (EducationActionDef) -> Int,
    onActionClick: (EducationActionDef) -> Unit,
    theme: LiveonTheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.surface, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text("Study Actions", fontWeight = FontWeight.Bold, color = theme.text)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            actions.forEach { def ->
                val locked = isActionLocked(def)
                val cooldown = isOnCooldown(def)
                val disabled = locked || cooldown

                ActionChip(
                    def = def,
                    enabled = !disabled,
                    progress = if (cooldown) cooldownProgress(def) else 0f,
                    locked = locked,
                    theme = theme,
                    onClick = { onActionClick(def) }
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
    theme: LiveonTheme,
    onClick: () -> Unit
) {
    val color = if (enabled) theme.primary else theme.secondary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.surfaceElevated, RoundedCornerShape(14.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color),
            modifier = Modifier.fillMaxWidth()
        ) {
            val safeIconRes = if (def.iconRes != 0) def.iconRes else R.drawable.ic_default_study

            Icon(
                painter = painterResource(id = safeIconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(text = def.name, modifier = Modifier.padding(start = 8.dp))
        }

        if (locked) {
            Text(text = "Locked", color = theme.accent, style = MaterialTheme.typography.labelSmall)
        } else {
            Text(
                text = "+${"%.2f".format(def.baseDelta)} GPA • ${ceil(def.cooldownSeconds / 60.0).toInt()}m",
                color = theme.text.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.alpha(0.9f)
            )
        }

        // Fix deprecated CircularProgressIndicator usage
        if (progress in 0f..0.999f) {
            CircularProgressIndicator(
                progress = { progress },
                color = theme.accent
            )
        }
    }
}