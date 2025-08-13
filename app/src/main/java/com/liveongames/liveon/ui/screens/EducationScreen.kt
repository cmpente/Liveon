// app/src/main/java/com/liveongames/liveon/ui/screens/education/EducationScreen.kt
package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.domain.model.EducationProgram
import com.liveongames.data.model.education.EducationActionDef
import com.liveongames.domain.model.Enrollment
import com.liveongames.domain.model.groupFromPeriod
import com.liveongames.domain.model.periodFromProgress
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.components.FullScreenLoading
import com.liveongames.liveon.ui.screens.education.components.*
import com.liveongames.liveon.ui.theme.LocalLiveonTheme
import com.liveongames.liveon.viewmodel.EducationEvent
import com.liveongames.liveon.viewmodel.EducationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationScreen(
    modifier: Modifier = Modifier,
    viewModel: EducationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val theme = LocalLiveonTheme.current

    if (state.loading) {
        FullScreenLoading()
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(theme.background, theme.surface)))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderCard(
                state = state,
                onInfoClick = { viewModel.handleEvent(EducationEvent.ShowGpaInfo) }
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Study Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = theme.text
                )
            }

            Spacer(Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.actions) { action ->
                    ActionChip(
                        action = action,
                        enrollment = state.enrollment,
                        onClick = {
                            viewModel.handleEvent(EducationEvent.DoAction(action.id, "default_choice"))
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Programs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = theme.text
            )

            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.programs) { program ->
                    ProgramCard(
                        course = program, // Changed from `course = course` to `course = program`
                        onClick = { viewModel.handleEvent(EducationEvent.Enroll(program.id)) }
                    )
                }
            }
        }

        if (state.showGpaInfo) {
            GpaInfoSheet(onDismiss = { viewModel.handleEvent(EducationEvent.HideGpaInfo) })
        }

        if (state.showFailOrRetake) {
            FailOrRetakeSheet(
                onRetake = { viewModel.handleEvent(EducationEvent.ChooseFailOrRetake(true)) },
                onFail = { viewModel.handleEvent(EducationEvent.ChooseFailOrRetake(false)) }
            )
        }

        val snackbarHostState = remember { SnackbarHostState() }
        state.message?.let { msg ->
            LaunchedEffect(msg) {
                snackbarHostState.showSnackbar(msg)
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


@Composable
fun HeaderCard(
    state: com.liveongames.liveon.viewmodel.EducationUiState,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enrollment = state.enrollment
    val theme = LocalLiveonTheme.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_education),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = theme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    val title = state.programs.firstOrNull { it.id == enrollment?.programId }?.title
                        ?: enrollment?.programId ?: "Prestige Academia"

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = theme.text
                    )
                }

                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "GPA Info",
                        tint = theme.text
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (enrollment == null) {
                Text(
                    "Enrolled: Not enrolled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.text.copy(alpha = 0.8f)
                )
            } else {
                val p = periodFromProgress(enrollment.progressPct, enrollment.schema.totalPeriods)
                val g = groupFromPeriod(p, enrollment.schema.periodsPerYear)
                val groupLabel = enrollment.schema.groupingLabel ?: enrollment.schema.displayPeriodName
                val programTitle =
                    state.programs.firstOrNull { it.id == enrollment.programId }?.title ?: "Program"

                Text(
                    "Enrolled: $programTitle",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.text.copy(alpha = 0.8f)
                )
                Text(
                    "Term: $groupLabel $g • ${enrollment.schema.displayPeriodName} $p • Progress ${enrollment.progressPct}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.text.copy(alpha = 0.8f)
                )
            }

            Spacer(Modifier.height(8.dp))
            TimelineRail(
                schema = enrollment?.schema,
                progressPct = enrollment?.progressPct ?: 0
            )
        }
    }
}