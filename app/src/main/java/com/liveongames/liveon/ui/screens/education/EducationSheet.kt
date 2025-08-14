package com.liveongames.liveon.ui.screens.education

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.liveon.viewmodel.EducationEvent
import com.liveongames.liveon.viewmodel.EducationViewModel
import com.liveongames.liveon.viewmodel.EducationUiState
import com.liveongames.data.model.education.EducationActionDef
import com.liveongames.domain.model.EducationProgram
import com.liveongames.domain.model.Enrollment

/**
 * Education modal sheet (like Crime screen), pure Material 3 + system theme.
 * No semesters timeline; clean sections: Student Record, Activities, Catalog, Transcript.
 */
@Composable
fun EducationSheet(
    onDismiss: () -> Unit,
    viewModel: EducationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val cs = MaterialTheme.colorScheme

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = cs.surface,
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            var actionsOpen by rememberSaveable { mutableStateOf(true) }
            var pickingAction by remember { mutableStateOf<EducationActionDef?>(null) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Education",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )
                        Text(
                            "Close",
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onDismiss() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            color = cs.primary
                        )
                    }
                }

                // Student Record
                item {
                    val enrolledProgramTitle = state.programs
                        .firstOrNull { it.id == state.enrollment?.programId }
                        ?.title ?: "Not enrolled"
                    StudentRecordCard(
                        title = enrolledProgramTitle,
                        enrollment = state.enrollment
                    )
                }

                // Activities & Interests
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { actionsOpen = !actionsOpen }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .animateContentSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Activities & Interests",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = cs.onSurface
                                )
                                Icon(
                                    imageVector = if (actionsOpen) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = cs.onSurfaceVariant
                                )
                            }

                            AnimatedVisibility(visible = actionsOpen) {
                                Column(Modifier.padding(16.dp)) {
                                    if (state.actions.isEmpty()) {
                                        Text(
                                            "No activities available.",
                                            color = cs.onSurfaceVariant
                                        )
                                    } else {
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            items(state.actions) { action ->
                                                ActionPill(
                                                    state = state, // Pass the state to ActionPill
                                                    action = action,
                                                    title = action.title,
                                                    costDescription = action.dialog.firstOrNull()?.text.orEmpty(), // Assuming costDescription is available or create one
                                                    isOnCooldown = !viewModel.isActionEligible(action, state.enrollment), // Check eligibility for cooldown
                                                    hasMiniGame = action.minigameId != null, // Assuming a minigameId indicates a mini-game
                                                    onClick = { pickingAction = action },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Catalog
                if (state.programs.isNotEmpty()) {
                    item {
                        Text(
                            "Course Catalog",
                            style = MaterialTheme.typography.titleLarge,
                            color = cs.onSurface
                        )
                        HorizontalDivider(Modifier.padding(top = 6.dp))
                    }
                    items(state.programs) { program ->
                        ProgramRow(
                            program = program,
                            enrolled = state.enrollment?.programId == program.id,
                            onEnroll = { viewModel.handleEvent(EducationEvent.Enroll(program.id)) }
                        )
                    }
                }

                // Transcript
                item {
                    TranscriptCard(enrollment = state.enrollment)
                }
            }

            // Action choice dialog
            pickingAction?.let { act ->
                ActionChoiceSheet(
                    action = act,
                    onChoose = { choiceId: String ->
                        viewModel.handleEvent(EducationEvent.DoAction(act.id, choiceId))
                        pickingAction = null
                    },
                    onDismiss = { pickingAction = null }
                )
            }
        }
    }
}

/* ---------- sub-components (same file for easy paste) ---------- */

@Composable
private fun StudentRecordCard(
    title: String,
    enrollment: Enrollment?
/*) {
    val cs = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = cs.secondaryContainer),

        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = cs.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                if (enrollment != null) "Student Record" else "Browse programs below",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSecondaryContainer.copy(alpha = 0.75f)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatChip("Progress", "${enrollment?.progressPct ?: 0}%")
                val standing = gradeFromProgress(enrollment?.progressPct ?: 0)
                StatChip("Standing", standing)
            }
        }
    }
}*/
) {
    val cs = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = cs.secondaryContainer),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = cs.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                if (enrollment != null) "Student Record" else "Browse programs below",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSecondaryContainer.copy(alpha = 0.75f)
            )

            Spacer(Modifier.height(12.dp))

            if (enrollment != null) {
                // Progress Bar
                Text(
                    "Progress: ${enrollment.progressPct}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = cs.onSecondaryContainer
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = enrollment.progressPct / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    color = cs.primary,
                    trackColor = cs.onSecondaryContainer.copy(alpha = 0.2f)
                )

                Spacer(Modifier.height(12.dp))

                // GPA and Grade
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatChip("GPA", "%.2f".format(enrollment.gpa))
                    StatChip("Standing", gradeFromProgress(enrollment.progressPct))
                }
            }
        }
    }
}

private fun gradeFromProgress(progress: Int): String = when {
    progress >= 90 -> "A"
    progress >= 80 -> "B"
    progress >= 70 -> "C"
    progress >= 60 -> "D"
    progress > 0 -> "E"
    else -> "F"
}

@Composable
private fun StatChip(label: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Surface(
        color = cs.surface,
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = cs.onSurface
            )
        }
    }
}

@Composable
private fun ActionPill(
 state: EducationUiState,
    action: EducationActionDef,
    title: String,
    subtitle: String, // This is currently the cost description
    hasMiniGame: Boolean, // New parameter to indicate mini-game
    isOnCooldown: Boolean, // New parameter to indicate cooldown status
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val cardColor = if (!isOnCooldown) cs.surface else cs.surfaceVariant
    val contentColor = if (!isOnCooldown) cs.onSurface else cs.onSurfaceVariant
    val rippleEnabled = !isOnCooldown

    OutlinedCard(

        onClick = if (rippleEnabled) onClick else ({}),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = cardColor, contentColor = contentColor),
        modifier = Modifier
            .widthIn(min = 220.dp)
            .clip(RoundedCornerShape(18.dp))
 ,
        enabled = true // Disable interaction if not eligible


    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            }
            if (!isOnCooldown) { // Corrected conditional check
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
 Icons.Outlined.Timer,
                        contentDescription = "On Cooldown",
                        modifier = Modifier.size(16.dp),
 tint = LocalContentColor.current.copy(alpha = 0.7f)
                    )
 Text(" On Cooldown", style = MaterialTheme.typography.labelSmall, color = LocalContentColor.current.copy(alpha = 0.7f))
                }
            }
            if (hasMiniGame) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.VideogameAsset,
                        contentDescription = "Mini-game included",
                        modifier = Modifier.size(16.dp),
                        tint = LocalContentColor.current.copy(alpha = 0.7f)
                    )
                    Text(" Mini-game", style = MaterialTheme.typography.labelSmall, color = LocalContentContent.current.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun ProgramRow(
    program: EducationProgram,
    enrolled: Boolean,
    onEnroll: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (enrolled) cs.primaryContainer else cs.surface,
            contentColor = if (enrolled) cs.onPrimaryContainer else cs.onSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.School, // Placeholder icon, you can change this based on program type
                contentDescription = null,
                tint = if (enrolled) cs.onPrimaryContainer else cs.primary,
                modifier = Modifier.size(40.dp).padding(end = 16.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    program.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (enrolled) cs.onPrimaryContainer else cs.onSurface
                )
                Spacer(Modifier.height(2.dp))
                val tuition = if (program.tuition == 0) "$0" else "$${program.tuition}"
                Text(
                    "Min GPA ${"%.2f".format(program.minGpa)}   Tuition $tuition",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (enrolled) cs.onPrimaryContainer.copy(alpha = 0.8f) else cs.onSurfaceVariant
                )
                val s = program.schema
                Text(
                    "${s.displayPeriodName} · ${s.periodsPerYear}/yr · ${s.totalPeriods} total",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enrolled) cs.onPrimaryContainer.copy(alpha = 0.7f) else cs.onSurfaceVariant
                )
            }
            if (enrolled) {
                AssistChip(onClick = {}, label = { Text("Enrolled") }, enabled = false) // Make enrolled chip non-interactive
            } else {
                Button(onClick = onEnroll, shape = RoundedCornerShape(12.dp)) { Text("Enroll") } // Consider making button color distinct
            }
        }
    }
}

@Composable
private fun TranscriptCard(enrollment: Enrollment?) {
    val cs = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Transcript & Honors", style = MaterialTheme.typography.titleLarge, color = cs.onSurface)
            if (enrollment == null) {
                Text("Not enrolled.", color = cs.onSurfaceVariant)
            } else {
                Text("• Enrolled in ${enrollment.programId}", color = cs.onSurface)
                Text("• Progress ${enrollment.progressPct}%", color = cs.onSurface)
            }
        }
    }
}