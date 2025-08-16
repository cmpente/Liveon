@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.liveongames.liveon.ui.screens.education

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LocalLiveonTheme
import com.liveongames.liveon.viewmodel.EducationEvent
import com.liveongames.liveon.viewmodel.EducationViewModel
import com.liveongames.data.model.education.EducationActionDef
import java.util.Locale
import com.liveongames.liveon.ui.LocalChromeInsets

@Composable
fun EducationSheet(
    onDismiss: () -> Unit,
    viewModel: EducationViewModel = hiltViewModel()
) {
    val t = LocalLiveonTheme.current
    val state by viewModel.uiState.collectAsState()
    val insets = LocalChromeInsets.current

    // derived state (unchanged)
    val enrollment = state.enrollment
    val programTitle = remember(enrollment, state.programs) {
        val id = enrollment?.programId
        state.programs.firstOrNull { it.id == id }?.title ?: "Student Record"
    }
    val progressPct = enrollment?.progressPct ?: 0
    val gpaText = enrollment?.gpa?.let { g -> String.format(Locale.US, "%.2f", g) } ?: "—"
    val standingText = remember(enrollment?.gpa) {
        val g = enrollment?.gpa ?: return@remember "—"
        when {
            g >= 3.7 -> "A"
            g >= 3.0 -> "B"
            g >= 2.0 -> "C"
            g >= 1.0 -> "D"
            else -> "F"
        }
    }

    // hoist snackbar to Scaffold
    val snackbarHostState = remember { SnackbarHostState() }
    state.message?.let { msg ->
        LaunchedEffect(msg) {
            snackbarHostState.showSnackbar(msg)
            viewModel.handleEvent(EducationEvent.DismissMessage)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Education", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = onDismiss) {
                        Icon(painter = painterResource(R.drawable.ic_close), contentDescription = "Close")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->
        val insets = LocalChromeInsets.current

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = insets.bottom)   // ← only bottom from chrome
        ) {
            // Student record card
            item {
                Surface(
                    color = t.surfaceElevated,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(t.primary.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_person),
                                    contentDescription = null,
                                    tint = t.primary
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    programTitle,
                                    color = t.text,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "Student Record",
                                    color = t.text.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            IconButton(
                                onClick = { viewModel.handleEvent(EducationEvent.ShowGpaInfo) }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_info),
                                    contentDescription = "GPA Info",
                                    tint = t.accent
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            "Progress: $progressPct%",
                            color = t.text.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelMedium
                        )
                        LinearProgressIndicator(
                            progress = { progressPct / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = t.primary,
                            trackColor = t.surfaceVariant
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatChip(
                                label = "GPA",
                                value = gpaText,
                                tint = t.primary,
                                container = t.surfaceVariant
                            )
                            StatChip(
                                label = "Standing",
                                value = standingText,
                                tint = t.accent,
                                container = t.surfaceVariant
                            )
                        }
                    }
                }
            }

            // Activities — ACCORDION of categories
            item {
                ActivitiesAccordion(
                    actions = state.actions,
                    canPerform = enrollment != null,
                    onPerform = { actionId ->
                        viewModel.handleEvent(
                            EducationEvent.DoAction(actionId, "default_choice")
                        )
                    }
                )
            }

            // Catalog
            item { SectionHeader("Course Catalog", t.text, t.accent) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.programs.forEach { program ->
                        val enrolledHere = enrollment?.programId == program.id
                        Surface(
                            color = t.surfaceElevated,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        program.title,
                                        color = t.text,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Text(
                                            "Req. GPA ${String.format(Locale.US, "%.2f", program.minGpa)}",
                                            color = t.text.copy(alpha = 0.75f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "Tuition $${program.tuition}",
                                            color = t.text.copy(alpha = 0.75f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (!enrolledHere) {
                                            viewModel.handleEvent(
                                                EducationEvent.Enroll(program.id)
                                            )
                                        }
                                    },
                                    enabled = !enrolledHere,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (enrolledHere) t.surfaceVariant else t.primary,
                                        contentColor = if (enrolledHere) t.text.copy(alpha = 0.75f) else Color.White,
                                        disabledContainerColor = t.surfaceVariant,
                                        disabledContentColor = t.text.copy(alpha = 0.6f)
                                    )
                                ) { Text(if (enrolledHere) "Enrolled" else "Enroll") }
                            }
                        }
                    }
                }
            }

            // Transcript
            item { SectionHeader("Transcript & Honors", t.text, t.accent) }
            item {
                Surface(
                    color = t.surfaceElevated,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        if (enrollment == null) {
                            Text(
                                "Not enrolled.",
                                color = t.text.copy(alpha = 0.75f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Bullet("Enrolled in $programTitle", t)
                            Bullet("Progress $progressPct%", t)
                            Bullet("Standing $standingText", t)
                        }
                    }
                }
            }
        }

        // Dialogs (unchanged visuals; still dim the background while open)
        if (state.showGpaInfo) {
            GpaInfoDialog(
                show = true,
                onDismiss = { viewModel.handleEvent(EducationEvent.HideGpaInfo) }
            )
        }
        if (state.showFailOrRetake) {
            FailOrRetakeDialog(
                onRetake = { viewModel.handleEvent(EducationEvent.ChooseFailOrRetake(true)) },
                onFail = { viewModel.handleEvent(EducationEvent.ChooseFailOrRetake(false)) },
                onDismiss = { viewModel.handleEvent(EducationEvent.ChooseFailOrRetake(false)) }
            )
        }
    }
}

/* --------------------- Activities Accordion --------------------- */

@Composable
private fun ActivitiesAccordion(
    actions: List<EducationActionDef>,
    canPerform: Boolean,
    onPerform: (String) -> Unit
) {
    val insets = LocalChromeInsets.current
    val t = LocalLiveonTheme.current

    Surface(
        color = t.surfaceElevated,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                "Activities & Interests",
                color = t.text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            if (!canPerform) {
                HintPill("Enroll in a course to unlock activities.")
                return@Column
            }
            if (actions.isEmpty()) {
                HintPill("No activities available right now.")
                return@Column
            }

            val grouped = remember(actions) {
                actions.groupBy { actionCategory(it.id, it.title) }
                    .toSortedMap(String.CASE_INSENSITIVE_ORDER)
            }

            val expansionKeys = remember(grouped) { grouped.keys.toList() }
            val expanded = rememberSaveable(
                inputs = arrayOf(expansionKeys),
                saver = mapSaver(
                    save = { stateMap: MutableMap<String, Boolean> -> stateMap.toMap() },
                    restore = { restored: Map<String, Any?> ->
                        @Suppress("UNCHECKED_CAST")
                        val restoredTyped = restored as Map<String, Boolean>
                        mutableStateMapOf<String, Boolean>().apply {
                            putAll(restoredTyped)
                            expansionKeys.forEach { key -> if (key !in this) put(key, false) }
                        }
                    }
                )
            ) {
                mutableStateMapOf<String, Boolean>().apply {
                    expansionKeys.forEach { put(it, false) }
                }
            }

            for ((category, itemList) in grouped) {
                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { expanded[category] = !(expanded[category] ?: true) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_expand),
                        contentDescription = null,
                        tint = t.text.copy(alpha = 0.75f),
                        modifier = Modifier
                            .size(16.dp)
                            .graphicsLayer {
                                rotationZ = if (expanded[category] == true) 180f else 0f
                            }
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        category,
                        color = t.text,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${itemList.size}",
                        color = t.text.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                AnimatedVisibility(visible = expanded[category] == true) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 6.dp, end = 4.dp, bottom = 4.dp)
                    ) {
                        itemList.forEach { action ->
                            ActivityListItem(
                                title = action.title,
                                enabled = true,
                                onClick = { onPerform(action.id) },
                                leadingIcon = R.drawable.ic_info
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ------------------------- helpers ------------------------- */

private fun actionCategory(actionId: String, title: String): String {
    val t = title.lowercase(Locale.US)
    val i = actionId.lowercase(Locale.US)
    return when {
        "study" in t || "focus" in t || "pomodoro" in i || "flow" in i -> "Study & Focus"
        "club" in t || "org" in t || "society" in t || "debate" in t -> "Clubs & Orgs"
        "exam" in t || "test" in t || "quiz" in t || "practice" in t -> "Exams & Prep"
        "tutor" in t || "mentor" in t || "office hours" in t -> "Tutoring"
        "research" in t || "lab" in t -> "Research"
        "volunteer" in t || "community" in t || "service" in t -> "Community"
        "sport" in t || "gym" in t || "fitness" in t || "run" in t -> "Fitness"
        "library" in t || "read" in t || "book" in t -> "Library & Reading"
        else -> "General"
    }
}

@Composable
private fun SectionHeader(title: String, text: Color, accent: Color) {
    val insets = LocalChromeInsets.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            color = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(40.dp)
                .background(accent, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun StatChip(label: String, value: String, tint: Color, container: Color) {
    val insets = LocalChromeInsets.current

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(container)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(label, color = tint.copy(alpha = 0.9f), style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(1.dp))
        Text(
            value,
            color = Color.White.copy(alpha = 0.95f),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActivityListItem(
    title: String,
    enabled: Boolean,
    onClick: () -> Unit,
    leadingIcon: Int
) {
    val t = LocalLiveonTheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) t.surfaceVariant else t.surfaceVariant.copy(alpha = 0.5f))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(leadingIcon),
            contentDescription = null,
            tint = if (enabled) t.primary else t.text.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            color = if (enabled) t.text else t.text.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            painter = painterResource(R.drawable.ic_expand),
            contentDescription = null,
            tint = t.text.copy(alpha = 0.45f),
            modifier = Modifier
                .size(14.dp)
                .graphicsLayer { rotationZ = 270f }
        )
    }
}

@Composable
private fun HintPill(text: String) {
    val insets = LocalChromeInsets.current
    val t = LocalLiveonTheme.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(t.surface.copy(alpha = 0.85f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = t.text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun Bullet(text: String, t: com.liveongames.liveon.ui.theme.LiveonTheme) {
    val insets = LocalChromeInsets.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(t.accent)
        )
        Spacer(Modifier.width(6.dp))
        Text(text, color = t.text.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
    }
}

/* --- Inline dialogs (unchanged visuals; still dim background) --- */

@Composable
private fun GpaInfoDialog(show: Boolean, onDismiss: () -> Unit) {
    if (!show) return
    val insets = LocalChromeInsets.current
    val t = LocalLiveonTheme.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
    ) {
        Surface(
            color = t.surfaceElevated,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("About GPA", color = t.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Your grade is tracked as GPA. Do activities to raise it; ignore school and it drifts down over time.",
                    color = t.text.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = t.primary)) { Text("OK") }
            }
        }
    }
}

@Composable
private fun FailOrRetakeDialog(
    onRetake: () -> Unit,
    onFail: () -> Unit,
    onDismiss: () -> Unit
) {
    val insets = LocalChromeInsets.current
    val t = LocalLiveonTheme.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
    ) {
        Surface(
            color = t.surfaceElevated,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Course Outcome", color = t.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Accept a failing grade or retake. Retaking costs time but may improve GPA.",
                    color = t.text.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onRetake, colors = ButtonDefaults.buttonColors(containerColor = t.primary)) { Text("Retake") }
                    Button(onClick = onFail, colors = ButtonDefaults.buttonColors(containerColor = t.accent)) { Text("Fail") }
                }
            }
        }
    }
}
