package com.liveongames.liveon.ui.screens.education

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.liveongames.domain.model.Education
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LiveonTheme

@Composable
fun EducationPathMap(
    tiers: List<EducationTier>,
    theme: LiveonTheme,
    onCourseSelected: (Education) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Education Path Map",
            style = MaterialTheme.typography.headlineSmall,
            color = theme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        tiers.forEachIndexed { index, tier ->
            EducationTierRow(
                tier = tier,
                theme = theme,
                onCourseSelected = onCourseSelected,
                isFirst = index == 0
            )

            if (index < tiers.size - 1) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_down),
                        contentDescription = null,
                        tint = theme.accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EducationTierRow(
    tier: EducationTier,
    theme: LiveonTheme,
    onCourseSelected: (Education) -> Unit,
    isFirst: Boolean
) {
    var visible by remember { mutableStateOf(isFirst) }

    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = if (isFirst) scaleIn(initialScale = 0.8f) + expandHorizontally()
        else expandHorizontally() + scaleIn(initialScale = 0.9f)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = tier.icon),
                    contentDescription = null,
                    tint = if (tier.unlocked) theme.primary else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = tier.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (tier.unlocked) theme.text else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                if (!tier.unlocked) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock),
                        contentDescription = "Locked",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = tier.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (tier.unlocked) theme.accent else Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp, start = 32.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tier.courses) { course ->
                    CourseCard(
                        course = course,
                        theme = theme,
                        unlocked = tier.unlocked,
                        onSelected = { onCourseSelected(course) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseCard(
    course: Education,
    theme: LiveonTheme,
    unlocked: Boolean,
    onSelected: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed && unlocked) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "courseCardScale"
    )

    Box(
        modifier = Modifier
            .height(200.dp)
            .width(160.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(if (unlocked) 1f else 0.6f)
    ) {
        Card(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    enabled = unlocked,
                    onClick = { pressed = true; onSelected() }
                )
                .border(
                    width = 1.dp,
                    color = if (unlocked) theme.primary.copy(alpha = 0.3f) else Color.Gray,
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (unlocked) theme.surface else theme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (unlocked) 6.dp else 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (unlocked) theme.text else theme.accent,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (unlocked) theme.accent else theme.accent.copy(alpha = 0.5f),
                    modifier = Modifier
                        .padding(top = 6.dp, bottom = 8.dp)
                        .weight(1f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatChip(R.drawable.ic_money, "$${course.cost}", theme, unlocked)
                    StatChip(R.drawable.ic_timer, "${course.duration}m", theme, unlocked)
                    if (course.requiredGPA > 0) {
                        StatChip(R.drawable.ic_gpa, "${course.requiredGPA}", theme, unlocked)
                    }
                }
                if (unlocked) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(course.duration.coerceAtMost(12)) { index ->
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index < (course.duration * 0.5).toInt())
                                            theme.primary
                                        else
                                            theme.surfaceVariant
                                    )
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(150)
            pressed = false
        }
    }
}

@Composable
private fun StatChip(
    icon: Int,
    text: String,
    theme: LiveonTheme,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (enabled) theme.surfaceVariant.copy(alpha = 0.7f)
                else theme.surfaceVariant.copy(alpha = 0.3f)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = if (enabled) theme.primary else Color.Gray,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) theme.text else Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}
