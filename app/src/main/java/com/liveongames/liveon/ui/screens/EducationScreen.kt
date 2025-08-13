// app/src/main/java/com/liveongames/liveon/ui/screens/education/EducationScreen.kt
package com.liveongames.liveon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.liveon.ui.components.FullScreenLoading
import com.liveongames.liveon.ui.screens.education.EducationAcademicSheet
import com.liveongames.liveon.ui.screens.education.components.*
import com.liveongames.liveon.ui.theme.LocalLiveonTheme
import com.liveongames.liveon.viewmodel.EducationEvent
import com.liveongames.liveon.viewmodel.EducationViewModel

@Composable
fun EducationScreen(
    modifier: Modifier = Modifier,
    showEducationModal: Boolean = false,
    onEducationModalDismiss: () -> Unit = {},
    viewModel: EducationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val theme = LocalLiveonTheme.current

    if (state.loading) {
        FullScreenLoading()
        return
    }

    if (showEducationModal) {
        EducationAcademicSheet(onDismiss = onEducationModalDismiss)
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
                    val isEligible = viewModel.isActionEligible(action, state.enrollment)
                    ActionChip(
                        action = action,
                        enrollment = state.enrollment,
                        isEligible = isEligible,
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
                        course = program,
                        onClick = { viewModel.handleEvent(EducationEvent.Enroll(program.id)) }
                    )
                }
            }
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