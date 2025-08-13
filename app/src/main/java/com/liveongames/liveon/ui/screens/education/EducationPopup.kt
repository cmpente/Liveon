package com.liveongames.liveon.ui.screens.education

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.screens.education.components.ActionChip
import com.liveongames.liveon.ui.screens.education.components.HeaderCard
import com.liveongames.liveon.ui.screens.education.components.ProgramCard
import com.liveongames.liveon.ui.theme.LocalLiveonTheme
import com.liveongames.liveon.viewmodel.EducationEvent
import com.liveongames.liveon.viewmodel.EducationViewModel

@Composable
fun EducationPopup(
    onDismiss: () -> Unit,
    viewModel: EducationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val theme = LocalLiveonTheme.current

    // Popup palette
    val backdrop = Color(0xCC061325)
    val panel   = Color(0xFF0F1E35)
    val card    = Color(0xFF162848)
    val accent  = Color(0xFF21A0FF)

    var actionsExpanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backdrop)
            .clickable { onDismiss() } // tap outside to close
    ) {
        // Panel
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .background(panel, RoundedCornerShape(24.dp))
                .clickable(enabled = false) {} // block click-through
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        TextButton(onClick = onDismiss) {
                            Text("Close", color = Color.White)
                        }
                    }
                }

                // Student profile in a navy card wrapper
                item {
                    Surface(
                        color = card,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                            HeaderCard(
                                state = state,
                                onInfoClick = { viewModel.handleEvent(EducationEvent.ShowGpaInfo) }
                            )
                        }
                    }
                }

                // Toggle strip (separate from HeaderCard; no footer param needed)
                if (state.enrollment != null) {
                    item {
                        Surface(
                            color = card,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { actionsExpanded = !actionsExpanded }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .animateContentSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (actionsExpanded) "Hide activities" else "Show study & activities",
                                    color = Color.White
                                )
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_expand),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.rotate(if (actionsExpanded) 180f else 0f)
                                )
                            }
                        }
                    }

                    // Activities
                    item {
                        AnimatedVisibility(visible = actionsExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(card, RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                SectionHeader("Activities & Interests", accent)
                                Spacer(Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(state.actions) { action ->
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

                // Programs
                item { SectionHeader("Course Catalog", accent) }
                items(state.programs) { program ->
                    // Wrap the existing ProgramCard so we can tint the background
                    Surface(
                        color = card,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(8.dp)) {
                            ProgramCard(
                                course = program,
                                onClick = { viewModel.handleEvent(EducationEvent.Enroll(program.id)) }
                            )
                        }
                    }
                }

                // Achievements
                item { SectionHeader("Transcript & Honors", accent) }
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = card),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("No achievements yet.", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String, accent: Color) {
    Column(Modifier.fillMaxWidth()) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Divider(thickness = 2.dp, color = accent.copy(alpha = 0.65f))
    }
}
