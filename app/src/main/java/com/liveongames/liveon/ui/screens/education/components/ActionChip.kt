// app/src/main/java/com/liveongames/liveon/ui/screens/education/components/ActionChip.kt
package com.liveongames.liveon.ui.screens.education.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.domain.model.Enrollment
import com.liveongames.data.model.education.EducationActionDef
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LocalLiveonTheme

@Composable
fun ActionChip(
    action: EducationActionDef,
    enrollment: Enrollment?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalLiveonTheme.current
    // TODO: Implement actual eligibility checks (tier, GPA) and cooldown status
    val isEligible = true // Placeholder logic
    val isOnCooldown = false // Placeholder logic
    val isEnabled = isEligible && !isOnCooldown

    Card(
        modifier = modifier
            .width(140.dp) // Width from your original
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) theme.surfaceVariant else theme.secondary.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onClick,
        enabled = isEnabled
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_default_study), // Ensure this drawable exists
                contentDescription = null,
                modifier = Modifier.size(32.dp), // Slightly larger icon
                tint = if (isEnabled) theme.primary else theme.text.copy(alpha = 0.5f) // Use theme.text for disabled
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = action.title, // Use .title instead of .name
                style = MaterialTheme.typography.labelLarge, // Style from original
                fontWeight = FontWeight.Bold, // Bold from original
                color = if (isEnabled) theme.text else theme.text.copy(alpha = 0.5f), // Use theme.text for disabled
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            // Placeholder text reflecting missing detailed model fields
            // You can update this once EducationActionDef has baseDelta/cooldownSeconds
            Text(
                text = "+%.2f–%.2f GPA • %dm".format(action.gpaDeltaMin, action.gpaDeltaMax, action.cooldownMinutes),
                style = MaterialTheme.typography.bodySmall, // Style from original
                color = if (isEnabled) theme.text.copy(alpha = 0.7f) else theme.text.copy(alpha = 0.3f) // Use theme.text for disabled
            )
        }
    }
}