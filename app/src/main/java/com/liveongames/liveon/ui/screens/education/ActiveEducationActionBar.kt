// app/src/main/java/com/liveongames/liveon/ui/screens/education/ActiveEducationActionBar.kt
package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.liveon.R
import com.liveongames.liveon.model.EducationActionDef
import com.liveongames.liveon.model.EducationCourse
import com.liveongames.liveon.ui.theme.LiveonTheme
import com.liveongames.liveon.viewmodel.EducationViewModel
import kotlin.math.ceil

@Composable
fun ActiveEducationActionBar(
    viewModel: EducationViewModel,
    course: EducationCourse,
    theme: LiveonTheme,
    modifier: Modifier = Modifier
) {
    val edu by viewModel.activeEducation.collectAsState()
    val actions by viewModel.actions.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.surface, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Study Actions",
            style = MaterialTheme.typography.titleMedium,
            color = theme.text,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            actions.forEach { def ->
                val lockInfo = viewModel.isActionLocked(course, def)
                val onCooldown = edu?.let { e -> viewModel.isOnCooldown(e.id, def) } ?: false
                val disabled = lockInfo.locked || onCooldown || edu == null

                val progress = if (onCooldown && edu != null) {
                    viewModel.cooldownProgress(edu.id, def)
                } else 0f

                ActionChip(
                    title = def.name,
                    summary = shortSummary(def),
                    iconRes = def.iconRes,
                    enabled = !disabled,
                    theme = theme,
                    progress = progress,
                    lockReason = when {
                        lockInfo.locked -> (lockInfo.reason ?: "Locked")
                        onCooldown      -> "Cooling down"
                        edu == null     -> "No active course"
                        else            -> null
                    },
                    onClick = { viewModel.performAction(def) }
                )
            }
        }
    }
}

private fun shortSummary(def: EducationActionDef): String {
    // Base delta is the *unmodified* GPA gain before tier/focus/term multipliers.
    // Show minutes/seconds nicely.
    val secs = def.cooldownSeconds
    val cd = if (secs < 60) "${secs}s" else "${ceil(secs / 60.0).toInt()}m"
    val cap = def.capPerAge
    return "+${"%.2f".format(def.baseDelta)} GPA • $cd • cap $cap"
}

@Composable
private fun ActionChip(
    title: String,
    summary: String,
    iconRes: Int,
    enabled: Boolean,
    progress: Float,
    theme: LiveonTheme,
    lockReason: String?,
    onClick: () -> Unit,
) {
    val base = if (enabled) theme.primary else colorResource(R.color.indigo_400)
    val textColor = if (enabled) colorResource(R.color.indigo_100) else colorResource(R.color.indigo_200)

    Column(
        modifier = Modifier
            .weight(1f)
            .background(theme.surfaceElevated, RoundedCornerShape(14.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = base),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                modifier = Modifier.padding(start = 8.dp),
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (!enabled && lockReason != null) {
            Text(
                text = lockReason,
                color = colorResource(R.color.indigo_200),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .alpha(0.9f)
            )
        } else {
            Text(
                text = summary,
                color = colorResource(R.color.indigo_100),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        if (progress in 0f..0.999f) {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = theme.accent,
                    trackColor = Color.Transparent
                )
                Text(
                    text = "Cooling down",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorResource(R.color.indigo_200),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
