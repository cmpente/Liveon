package com.liveongames.liveon.ui.screens.education

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DialogProperties
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.data.model.education.EducationActionDef
import com.liveongames.domain.model.EducationProgram
import com.liveongames.domain.model.Enrollment
import com.liveongames.liveon.viewmodel.EducationEvent
import com.liveongames.liveon.viewmodel.EducationUiState
import com.liveongames.liveon.viewmodel.EducationViewModel

/**
 * Full-screen academic styled sheet.
 * Self-contained: no external component deps, only Material3 + your VM.
 */
@Composable
fun EducationSheet(
    onDismiss: () -> Unit,
    viewModel: EducationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Dimmed backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.04f))
        ) {
            // Main container
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shape = RoundedCornerShape(28.dp)
            ) {
                Box {
                    // Close
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }

                    // Content
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Title
                        item {
                            Text(
                                "Education",
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        // Student Record / Not enrolled
                        item {
                            StudentRecordCard(
                                state = state,
                                onInfo = { viewModel.handleEvent(EducationEvent.ShowGpaInfo) }
                            )
                        }

                        // Expandable activities (only when enrolled)
                        if (state.enrollment != null) {
                            item {
                                ActivitiesSection(
                                    actions = state.actions,
                                    enrollment = state.enrollment,
                                    onAction = { actionId, choiceId, mult ->
                                        viewModel.handleEvent(
                                            EducationEvent.DoAction(
                                                actionId = actionId,
                                                choiceId = choiceId,
                                                multiplier = mult
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        // Catalog
                        item {
                            SectionHeader("Course Catalog")
                        }
                        items(state.programs, key = { it.id }) { program ->
                            ProgramRow(
                                program = program,
                                enrolledProgramId = state.enrollment?.programId,
                                onEnroll = { viewModel.handleEvent(EducationEvent.Enroll(program.id)) }
                            )
                        }

                        // Transcript & honors
                        item {
                            SectionHeader("Transcript & Honors")
                        }
                        item {
                            TranscriptCard(state.enrollment, state.grade)
                        }

                        // Bottom spacing
                        item { Spacer(Modifier.height(12.dp)) }
                    }
                }
            }
        }
    }
}

/* ----------------------- Pieces ----------------------- */

@Composable
private fun StudentRecordCard(
    state: EducationUiState,
    onInfo: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    val e = state.enrollment

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = c.surfaceVariant),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Monogram
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(c.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ED",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = c.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = e?.programTitle ?: "Not Enrolled",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (e == null) "You’re not enrolled in a program." else "Student Record",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.onSurfaceVariant
                    )
                }

                IconButton(onClick = onInfo) {
                    Icon(Icons.Filled.Info, contentDescription = "Info")
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricPill(
                    label = "Grade",
                    value = "${state.grade}%",
                    emphasis = true
                )
                MetricPill(
                    label = "Progress",
                    value = "${e?.progressPct ?: 0}%"
                )
                MetricPill(
                    label = "Standing",
                    value = e?.let { gradeToStanding(state.grade) } ?: "—",
                    minWidth = 88.dp
                )
            }
        }
    }
}

private fun gradeToStanding(grade: Int): String = when (grade) {
    in 93..100 -> "A"
    in 85..92 -> "B"
    in 75..84 -> "C"
    in 65..74 -> "D"
    else -> "F"
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    emphasis: Boolean = false,
    minWidth: Dp = 0.dp
) {
    val c = MaterialTheme.colorScheme
    val border = if (emphasis) c.primary else c.outline.copy(alpha = 0.6f)

    Surface(
        shape = RoundedCornerShape(14.dp),
        tonalElevation = if (emphasis) 2.dp else 0.dp,
        border = BorderStroke(1.dp, border),
        color = if (emphasis) c.primary.copy(alpha = 0.12f) else c.surface
    ) {
        Column(
            Modifier
                .defaultMinSize(minWidth = minWidth)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = c.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        HorizontalDivider(Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun ActivitiesSection(
    actions: List<EducationActionDef>,
    enrollment: Enrollment?,
    onAction: (actionId: String, choiceId: String, multiplier: Double) -> Unit
) {
    val c = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(true) }
    val arrowRot by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "arrow")

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = c.surfaceVariant),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Activities & Interests",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(arrowRot)
                )
            }

            AnimatedVisibility(expanded) {
                Column {
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(actions, key = { it.id }) { action ->
                            ActivityCard(
                                action = action,
                                enabled = enrollment != null,
                                onClick = {
                                    // If your action has dialog branches, open sheet; default branch otherwise:
                                    onAction(action.id, choiceId = "default", multiplier = 1.0)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(
    action: EducationActionDef,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    val cardColor = if (enabled) c.surface else c.surface.copy(alpha = 0.6f)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, c.outline.copy(alpha = 0.4f)),
        modifier = Modifier
            .width(240.dp)
            .heightIn(min = 120.dp)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Info, contentDescription = null,
                    tint = if (enabled) c.primary else c.onSurface.copy(alpha = 0.4f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    action.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val gpaMin = action.gpaDeltaMin ?: 0.0
            val gpaMax = action.gpaDeltaMax ?: 0.0
            val mins = action.cooldownMinutes

            Text(
                text = "+${formatGpa(gpaMin)}–${formatGpa(gpaMax)} GPA • ${mins}m",
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant
            )

            action.description?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatGpa(v: Double): String = String.format("%.2f", v)

@Composable
private fun ProgramRow(
    program: EducationProgram,
    enrolledProgramId: String?,
    onEnroll: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    val isEnrolled = enrolledProgramId == program.id

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        border = BorderStroke(1.dp, c.outline.copy(alpha = 0.35f)),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accent
            Box(
                Modifier
                    .width(6.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(c.primary)
            )

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    program.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row {
                    Text(
                        "Req. GPA ${String.format("%.2f", program.minGpa)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.onSurfaceVariant
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Tuition ${money(program.tuition)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.onSurfaceVariant
                    )
                }
                program.description.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = c.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Button(
                onClick = onEnroll,
                enabled = !isEnrolled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = c.primary,
                    contentColor = c.onPrimary,
                    disabledContainerColor = c.surfaceVariant,
                    disabledContentColor = c.onSurfaceVariant
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(if (isEnrolled) "Enrolled" else "Enroll")
            }
        }
    }
}

private fun money(v: Int): String =
    if (v <= 0) "$0" else "$${"%,d".format(v)}"

@Composable
private fun TranscriptCard(enrollment: Enrollment?, visibleGrade: Int) {
    val c = MaterialTheme.colorScheme
    val muted = c.onSurface.copy(alpha = 0.9f)

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = c.surfaceVariant),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (enrollment == null) {
                Text(
                    "No transcript yet. Enroll in a program to begin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = muted
                )
            } else {
                bullet("Enrolled in ${enrollment.programId}")
                bullet("Progress ${enrollment.progressPct}%")
                bullet("Visible grade $visibleGrade%")
                bullet("GPA ${"%.2f".format(enrollment.gpa)}")
            }
        }
    }
}

@Composable
private fun bullet(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
