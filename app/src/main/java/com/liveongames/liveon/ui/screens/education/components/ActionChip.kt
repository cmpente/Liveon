package com.liveongames.liveon.ui.screens.education.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.domain.model.Enrollment
import com.liveongames.data.model.education.EducationActionDef
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LocalLiveonTheme
import kotlin.math.roundToInt

@Composable
fun ActionChip(
    action: EducationActionDef,
    enrollment: Enrollment?,
    isEligible: Boolean,
    onClick: () -> Unit
) {
    val theme = LocalLiveonTheme.current
    val enabled = isEligible && enrollment != null

    Surface(
        modifier = Modifier
            .widthIn(min = 180.dp)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = theme.surfaceElevated
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_info),
                contentDescription = null,
                tint = if (enabled) theme.primary else theme.text.copy(alpha = 0.3f)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) theme.text else theme.text.copy(alpha = 0.4f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = buildString {
                        val minPts = (action.gpaDeltaMin * 25.0).roundToInt()
                        val maxPts = (action.gpaDeltaMax * 25.0).roundToInt()
                        append("+$minPts–$maxPts pts • ${action.cooldownMinutes}m")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) theme.text.copy(alpha = 0.7f) else theme.text.copy(alpha = 0.3f)
                )
            }
        }
    }
}