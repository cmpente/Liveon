package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liveongames.liveon.R
import com.liveongames.liveon.model.EducationActionDef
import com.liveongames.liveon.model.EducationCourse
import com.liveongames.liveon.viewmodel.EducationViewModel

@Composable
fun ActiveEducationActionBar(
    actions: List<EducationActionDef>,
    course: EducationCourse,
    isActionLocked: (EducationActionDef) -> EducationViewModel.ActionLock,
    isOnCooldown: (EducationActionDef) -> Boolean,
    cooldownProgress: (EducationActionDef) -> Float,
    capRemaining: (EducationActionDef) -> Int,
    onActionClick: (EducationActionDef) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = colorResource(id = R.color.slate_900)
    val text = colorResource(id = R.color.white)

    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("Study Actions", color = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            val sorted = actions.sortedBy { it.cooldownSeconds }
            val perRow = 3
            sorted.chunked(perRow).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    row.forEach { def ->
                        ActionChip(def = def, locked = isActionLocked(def).locked, onCooldown = isOnCooldown(def), progress = cooldownProgress(def), capLeft = capRemaining(def), onClick = { onActionClick(def) }, modifier = Modifier.weight(1f).padding(4.dp))
                    }
                    repeat(kotlin.math.max(0, perRow - row.size)) { Spacer(Modifier.weight(1f).padding(4.dp)) }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun ActionChip(def: EducationActionDef, locked: Boolean, onCooldown: Boolean, progress: Float, capLeft: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val chipBg = colorResource(id = R.color.slate_800)
    val text = colorResource(id = R.color.white)
    val disabled = locked || onCooldown

    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = chipBg, onClick = { if (!disabled) onClick() }, enabled = !disabled) {
        Box(modifier = Modifier.height(84.dp).padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(painter = painterResource(id = actionIconFor(def)), contentDescription = def.name, tint = text)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(def.name, color = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    val deltaText = if (def.baseDelta > 0.0) "+${"%.2f".format(def.baseDelta)} GPA" else "Utility"
                    Text(deltaText, color = text.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                }
            }
            if (onCooldown) {
                Canvas(modifier = Modifier.size(40.dp).align(Alignment.CenterEnd)) {
                    val stroke = 6f
                    drawArc(color = text.copy(alpha = 0.25f), startAngle = -90f, sweepAngle = 360f, useCenter = false, size = Size(size.width, size.height), style = Stroke(width = stroke, cap = StrokeCap.Round))
                    drawArc(color = text, startAngle = -90f, sweepAngle = progress.coerceIn(0f,1f) * 360f, useCenter = false, size = Size(size.width, size.height), style = Stroke(width = stroke, cap = StrokeCap.Round))
                }
            } else if (capLeft >= 0) {
                AssistChip(onClick = {}, label = { Text("Cap $capLeft") }, enabled = false, modifier = Modifier.align(Alignment.CenterEnd))
            }
            if (locked) Text("Locked", color = colorResource(id = R.color.indigo_300), modifier = Modifier.align(Alignment.BottomEnd), style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun actionIconFor(def: EducationActionDef): Int {
    val id = def.id.lowercase()
    return when {
        "quiz" in id || def.minigame?.type == EducationActionDef.MiniGameType.QUIZ -> R.drawable.ic_quiz
        "tim" in id || def.minigame?.type == EducationActionDef.MiniGameType.TIMING -> R.drawable.ic_timing
        "mem" in id || def.minigame?.type == EducationActionDef.MiniGameType.MEMORY -> R.drawable.ic_memory
        "lab" in id -> R.drawable.ic_lab
        "tutor" in id -> R.drawable.ic_tutor
        "research" in id -> R.drawable.ic_research
        "presentation" in id -> R.drawable.ic_presentation
        "planner" in id -> R.drawable.ic_planner
        "nap" in id -> R.drawable.ic_nap
        "walk" in id -> R.drawable.ic_walk
        else -> R.drawable.ic_education
    }
}
