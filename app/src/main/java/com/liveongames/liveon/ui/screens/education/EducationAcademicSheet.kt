// app/src/main/java/com/liveongames/liveon/ui/screens/education/EducationAcademicSheet.kt
package com.liveongames.liveon.ui.screens.education

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.screens.education.components.ActionChip
import com.liveongames.liveon.ui.screens.education.components.TimelineRail
import com.liveongames.liveon.viewmodel.EducationEvent
import com.liveongames.liveon.viewmodel.EducationViewModel

/**
 * Academic-styled popup:
 * - Ribbon headers, serif titles
 * - Student Record card: crest + metrics grid + syllabus timeline
 * - Accordion for “Study & Activities” that pushes content and scrolls
 * - Full-width “index card” Program rows (unique look; not using ProgramCard)
 * - Simple transcript timeline block
 */
@Composable
fun EducationAcademicSheet(
    onDismiss: () -> Unit,
    viewModel: EducationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Academic palette
    val ink = Color(0xFF1F2937)
    val parchment = Color(0xFFF7F3E9)
    val card = Color(0xFFFFFEFA)
    val cardEdge = Color(0xFFE6DFC8)
    val oxfordBlue = Color(0xFF002147)
    val crimson = Color(0xFF8B0000)
    val brass = Color(0xFFC4A356)
    val green = Color(0xFF2E7D32)

    var showActivities by rememberSaveable { mutableStateOf(false) }

    // Backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC0D0D0D))
            .clickable { onDismiss() }
    ) {
        // Parchment panel
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clickable(enabled = false) {}, // block click-through
            color = parchment,
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            // Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Title bar
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Education",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.ExtraBold,
                                color = oxfordBlue
                            )
                        )
                        TextButton(onClick = onDismiss) {
                            Text("Close", color = oxfordBlue, fontFamily = FontFamily.Serif)
                        }
                    }
                }

                // Student Record card (crest + metrics + syllabus)
                item {
                    StudentRecordCard(
                        ink = ink,
                        brass = brass,
                        crimson = crimson,
                        card = card,
                        edge = cardEdge,
                        state = state,
                        onInfo = { viewModel.handleEvent(EducationEvent.ShowGpaInfo) },
                        onToggleActivities = { showActivities = !showActivities },
                        showActivities = showActivities
                    )
                }

                // Activities accordion
                if (state.enrollment != null) {
                    item {
                        AnimatedVisibility(visible = showActivities) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(card, RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                                    .animateContentSize()
                            ) {
                                RibbonHeader("Study & Activities", brass, oxfordBlue)
                                Spacer(Modifier.height(10.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(items = state.actions, key = { it.id }) { action ->
                                        val eligible = viewModel.isActionEligible(action, state.enrollment)
                                        ActionChip(
                                            action = action,
                                            enrollment = state.enrollment,
                                            isEligible = eligible,
                                            onClick = {
                                                viewModel.handleEvent(
                                                    EducationEvent.DoAction(action.id, "default_choice")
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Program list (full-width, index-card style)
                item { RibbonHeader("Course Catalog", brass, oxfordBlue) }
                items(items = state.programs, key = { it.id }) { program ->
                    AcademicProgramRow(
                        title = program.title,
                        minGpa = program.minGpa,
                        tuition = program.tuition,
                        schemaLabel = program.schema.groupingLabel ?: program.schema.displayPeriodName,
                        periodsPerYear = program.schema.periodsPerYear,
                        totalPeriods = program.schema.totalPeriods,
                        stripeColor = oxfordBlue,
                        card = card,
                        edge = cardEdge,
                        ink = ink,
                        onEnroll = {
                            viewModel.handleEvent(EducationEvent.Enroll(program.id))
                        }
                    )
                }

                // Transcript / Honors
                item { RibbonHeader("Transcript & Honors", brass, oxfordBlue) }
                item {
                    TranscriptBlock(
                        card = card,
                        edge = cardEdge,
                        ink = ink,
                        accent = green,
                        lines = buildList {
                            val e = state.enrollment
                            if (e != null) {
                                add("Enrolled in ${e.programId}")
                                add("Progress ${e.progressPct}%")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RibbonHeader(title: String, ribbon: Color, text: Color) {
    Surface(
        color = ribbon,
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                color = text
            )
        )
    }
}

@Composable
private fun StudentRecordCard(
    ink: Color,
    brass: Color,
    crimson: Color,
    card: Color,
    edge: Color,
    state: com.liveongames.liveon.viewmodel.EducationUiState,
    onInfo: () -> Unit,
    onToggleActivities: () -> Unit,
    showActivities: Boolean
) {
    Surface(
        color = card,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 6.dp,
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, edge)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Crest + Title row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(crimson, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "ED",
                        color = Color.White,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Student Record",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = ink
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Registrar",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            color = ink.copy(alpha = 0.6f),
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }
                IconButton(onClick = onInfo) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = "GPA info",
                        tint = ink
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Metrics row (academic feel)
            val e = state.enrollment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Metric(label = "School", value = (e?.programId ?: "—"), ink = ink, brass = brass)
                Metric(label = "Course", value = state.programs.firstOrNull { it.id == e?.programId }?.title ?: "—", ink = ink, brass = brass)
                Metric(
                    label = "Progress",
                    value = state.enrollment?.let { "${it.progressPct}%" } ?: "—",
                    ink = ink,
                    brass = brass
                )
            }

            Spacer(Modifier.height(12.dp))

            // Syllabus timeline (if enrolled)
            if (e != null) {
                TimelineRail(schema = e.schema, progressPct = e.progressPct)
            } else {
                Text("Not enrolled",
                    style = MaterialTheme.typography.bodyMedium, color = ink.copy(alpha = 0.8f))
            }

            Spacer(Modifier.height(12.dp))

            // Accordion handle
            val canToggle = e != null
            Surface(color = Color.Transparent, shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = canToggle) { onToggleActivities() }
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .animateContentSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (showActivities) "Hide study & activities" else "Show study & activities",
                        color = if (canToggle) ink else ink.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Serif)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_expand),
                        contentDescription = null,
                        tint = if (canToggle) ink else ink.copy(alpha = 0.4f),
                        modifier = Modifier.rotate(if (showActivities) 180f else 0f)
                    )
                }
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String, ink: Color, brass: Color) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, brass.copy(alpha = 0.4f))
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Serif,
                    color = ink.copy(alpha = 0.6f)
                )
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    color = ink
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AcademicProgramRow(
    title: String,
    minGpa: Double,
    tuition: Int,
    schemaLabel: String?,
    periodsPerYear: Int,
    totalPeriods: Int,
    stripeColor: Color,
    card: Color,
    edge: Color,
    ink: Color,
    onEnroll: () -> Unit
) {
    Surface(
        color = card,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, edge)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
        ) {
            // Left stripe
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .heightIn(min = 84.dp)
                    .background(stripeColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = ink
                    )
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Min GPA ${"%.2f".format(minGpa)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = ink.copy(alpha = 0.8f))
                    )
                    Text(
                        "Tuition $$tuition",
                        style = MaterialTheme.typography.bodySmall.copy(color = ink.copy(alpha = 0.8f))
                    )
                }
                val label = schemaLabel ?: "Period"
                Text(
                    "$label · $periodsPerYear/yr · $totalPeriods total",
                    style = MaterialTheme.typography.labelSmall.copy(color = ink.copy(alpha = 0.7f))
                )
            }

            Column(
                modifier = Modifier
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = onEnroll, shape = RoundedCornerShape(10.dp)) {
                    Text("Enroll")
                }
            }
        }
    }
}

@Composable
private fun TranscriptBlock(
    card: Color,
    edge: Color,
    ink: Color,
    accent: Color,
    lines: List<String>
) {
    Surface(
        color = card,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, edge)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            if (lines.isEmpty()) {
                Text("No achievements yet.", color = ink)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 6.dp)
                ) {
                    lines.forEach { line ->
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(accent, CircleShape)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(line, color = ink)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}