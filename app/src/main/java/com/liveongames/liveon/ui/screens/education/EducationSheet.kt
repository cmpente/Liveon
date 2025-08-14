// app/src/main/java/com/liveongames/liveon/ui/screens/education/EducationSheet.kt
package com.liveongames.liveon.ui.screens.education

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.liveon.R
import com.liveongames.liveon.viewmodel.EducationEvent
import com.liveongames.liveon.viewmodel.EducationViewModel
import com.liveongames.domain.model.EducationProgram
import com.liveongames.domain.model.Enrollment
import com.liveongames.domain.model.EduTier
import com.liveongames.data.model.education.EducationActionDef
import com.liveongames.liveon.ui.theme.LocalLiveonTheme

/**
 * Education modal sheet using Liveon theme tokens.
 */
@Composable
fun EducationSheet(
    onDismiss: () -> Unit,
    viewModel: EducationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val theme = LocalLiveonTheme.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = theme.surface,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            var actionsOpen by rememberSaveable { mutableStateOf(true) }
            var selectedProgram: EducationProgram? by remember { mutableStateOf(null) }
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
                            color = theme.text
                        )
                        Text(
                            "Close",
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onDismiss() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            color = theme.primary
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
                        enrollment = state.enrollment,
                        playerAvatar = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = "Player Avatar",
                                modifier = Modifier.size(40.dp),
                                tint = theme.text
                            )
                        }
                    )
                }

                // Activities & Interests
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
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
                                    color = theme.text
                                )
                                Icon(
                                    painter = painterResource(id = if (actionsOpen) R.drawable.ic_collapse else R.drawable.ic_expand),
                                    contentDescription = null,
                                    tint = theme.text.copy(alpha = 0.7f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            AnimatedVisibility(visible = actionsOpen) {
                                Column(Modifier.padding(16.dp)) {
                                    if (state.actions.isEmpty()) {
                                        Text(
                                            "No activities available.",
                                            color = theme.text.copy(alpha = 0.7f)
                                        )
                                    } else {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(state.actions) { action ->
                                                ActionPill(
                                                    action = action,
                                                    title = action.title,
                                                    subtitle = action.dialog.firstOrNull()?.text.orEmpty(),
                                                    isOnCooldown = !viewModel.isActionEligible(action, state.enrollment),
                                                    hasMiniGame = false,
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
                item {
                    AnimatedVisibility(visible = state.enrollment == null) {
                        if (state.programs.isNotEmpty()) {
                            Column {
                                Text(
                                    "Course Catalog",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = theme.text
                                )
                                Divider(
                                    modifier = Modifier.padding(top = 6.dp),
                                    color = theme.text.copy(alpha = 0.12f)
                                )
                            }
                        }
                    }
                }

                // Program List
                items(state.programs) { program ->
                    AnimatedVisibility(visible = state.enrollment == null) {
                        ProgramRow(
                            program = program,
                            enrolled = state.enrollment?.programId == program.id,
                            onEnroll = { viewModel.handleEvent(EducationEvent.Enroll(program.id)) }
                        )
                    }
                }

                // Transcript
                item {
                    TranscriptCard(
                        enrollment = state.enrollment,
                        completedInstitutions = emptyList(),
                        academicHonors = emptyList(),
                        certifications = emptyList()
                    )
                }
            }

            // Action choice dialog (placeholder)
            pickingAction?.let { act ->
                // TODO: Implement ActionChoiceSheet
            }

            // Program detail panel
            selectedProgram?.let { program ->
                Dialog(
                    onDismissRequest = { selectedProgram = null },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = theme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                program.title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = theme.text
                            )
                            Text(
                                program.description,
                                color = theme.text.copy(alpha = 0.7f)
                            )
                            Text(
                                "Min GPA: ${"%.2f".format(program.minGpa)}",
                                color = theme.text
                            )
                            Text(
                                "Tuition: $${program.tuition}",
                                color = theme.text
                            )
                            val s = program.schema
                            Text(
                                "${s.displayPeriodName} · ${s.periodsPerYear}/yr · ${s.totalPeriods} total",
                                color = theme.text.copy(alpha = 0.7f)
                            )
                            Button(
                                onClick = { selectedProgram = null },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Sub-components ---------- */

@Composable
private fun StudentRecordCard(
    title: String,
    enrollment: Enrollment?,
    playerAvatar: @Composable () -> Unit
) {
    val theme = LocalLiveonTheme.current
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                playerAvatar()
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = theme.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (enrollment != null) "Student Record" else "Browse programs below",
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.text.copy(alpha = 0.75f)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            if (enrollment != null) {
                // Progress Bar
                Text(
                    "Progress: ${enrollment.progressPct}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.text
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { enrollment.progressPct / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = theme.primary,
                    trackColor = theme.text.copy(alpha = 0.2f)
                )

                Spacer(Modifier.height(12.dp))

                // GPA and Grade
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatChip(
                        label = "GPA",
                        value = "%.2f".format(enrollment.gpa),
                        icon = R.drawable.ic_gpa
                    )
                    StatChip(
                        label = "Standing",
                        value = gradeFromProgress(enrollment.progressPct),
                        icon = R.drawable.ic_certificate
                    )
                }

                // School Details
                Spacer(Modifier.height(12.dp))
                Column {
                    Text(
                        "School Details:",
                        style = MaterialTheme.typography.titleMedium,
                        color = theme.text
                    )
                    Spacer(Modifier.height(8.dp))
                    // TODO: Dynamically display enrolled courses/grade level based on enrollment.courses and enrollment.tier
                    when (enrollment.tier) {
                        EduTier.ELEMENTARY -> Text(
                            "Current Grade: 5th Grade", // Placeholder
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.text.copy(alpha = 0.75f)
                        )
                        EduTier.MIDDLE -> Text(
                            "Current Grade: 7th Grade", // Placeholder
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.text.copy(alpha = 0.75f)
                        )
                        EduTier.HIGH -> Text(
                            "Enrolled Classes: English, Math, Science", // Placeholder
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.text.copy(alpha = 0.75f)
                        )
                        EduTier.CERT -> Text(
                            "Certificate Program Details: Completing the requirements for a Software Development certification.", // Placeholder
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.text.copy(alpha = 0.75f)
                        )


                        EduTier.ASSOC -> Text(
                            "Associate Degree Details: [Details]",
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.text.copy(alpha = 0.75f)
                        )
                        EduTier.BACH -> Text(
                            "Bachelor's Degree Details: [Details]",
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.text.copy(alpha = 0.75f)
                        )
                        EduTier.MAST -> Text(
                            "Master's Degree Details: [Details]",
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.text.copy(alpha = 0.75f)
                        )
                        EduTier.PHD -> Text(
                            "Doctoral Program Details: [Details]",
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.text.copy(alpha = 0.75f)
                        )
                        else -> Text(
                            "Not enrolled in a recognized institution tier.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.text.copy(alpha = 0.75f)
                        )
                    }
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
private fun StatChip(label: String, value: String, icon: Int) {
    val theme = LocalLiveonTheme.current
    Surface(
        color = theme.surface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = theme.primary,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.text.copy(alpha = 0.7f)
                )
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = theme.text
                )
            }
        }
    }
}

@Composable
private fun ActionPill(
    action: EducationActionDef,
    title: String,
    subtitle: String?,
    hasMiniGame: Boolean = false, // Default to false if not explicitly passed
    isOnCooldown: Boolean,
    onClick: () -> Unit
) {
    val theme = LocalLiveonTheme.current
    val cardColor = if (!isOnCooldown) theme.surface else theme.surfaceVariant
    val contentColor = if (!isOnCooldown) theme.text else theme.text.copy(alpha = 0.7f)
    val rippleEnabled = !isOnCooldown

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor,
            contentColor = contentColor
        ),
        modifier = Modifier
            .widthIn(min = 220.dp)
            .clip(RoundedCornerShape(18.dp))
            .then(
                if (rippleEnabled) Modifier.clickable(onClick = onClick) else Modifier
            )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_default_study),
                    contentDescription = null,
                    tint = if (isOnCooldown) theme.text.copy(alpha = 0.7f) else theme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = theme.text,
                    modifier = Modifier.weight(1f)
                )
            }

            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.text.copy(alpha = 0.7f)
                , maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            if (isOnCooldown) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_hourglass),
                        contentDescription = "On Cooldown",
                        modifier = Modifier.size(16.dp),
                        tint = theme.text.copy(alpha = 0.7f)
                    )
                    Text(
                        "On Cooldown",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.text.copy(alpha = 0.7f)
                    )
                }
            }

            if (hasMiniGame) {
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_default_study),
                        contentDescription = "Mini-game included",
                        modifier = Modifier.size(16.dp),
                        tint = theme.text.copy(alpha = 0.7f)
                    )
                    Text(
                        "Mini-game",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.text.copy(alpha = 0.7f)
                    )
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
    val theme = LocalLiveonTheme.current
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (enrolled) theme.primary.copy(alpha = 0.2f) else theme.surface,
            contentColor = if (enrolled) theme.primary else theme.text
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_school),
                contentDescription = null,
                tint = if (enrolled) theme.primary else theme.primary,
                modifier = Modifier.size(40.dp).padding(end = 16.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    program.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (enrolled) theme.primary else theme.text
                )
                Spacer(Modifier.height(2.dp))
                val tuition = if (program.tuition == 0) "$0" else "$${program.tuition}"
                Text(
                    "Min GPA ${"%.2f".format(program.minGpa)}   Tuition $tuition",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (enrolled) theme.primary.copy(alpha = 0.8f) else theme.text.copy(alpha = 0.7f)
                )
                val s = program.schema
                Text(
                    "${s.displayPeriodName} · ${s.periodsPerYear}/yr · ${s.totalPeriods} total",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enrolled) theme.primary.copy(alpha = 0.7f) else theme.text.copy(alpha = 0.7f)
                )
            }
            if (enrolled) {
                AssistChip(
                    onClick = { },
                    label = { Text("Enrolled", color = theme.primary) },
                    enabled = false
                )
            } else {
                Button(
                    onClick = onEnroll,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primary,
                        contentColor = theme.surface
                    )
                ) {
                    Text("Enroll")
                }
            }
        }
    }
}

@Composable
private fun TranscriptCard(
    enrollment: Enrollment?,
    completedInstitutions: List<Any>,
    academicHonors: List<Any>,
    certifications: List<Any>
) {
    val theme = LocalLiveonTheme.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_diploma),
                    contentDescription = null,
                    tint = theme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Transcript & Honors",
                    style = MaterialTheme.typography.titleLarge,
                    color = theme.text
                )
            }

            Text(
                "Transcript information will be available here",
                color = theme.text.copy(alpha = 0.7f)
            )
        }
    }
}