// app/src/main/java/com/liveongames/liveon/ui/screens/EventScreen.kt
package com.liveongames.liveon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.liveongames.domain.model.Event
import com.liveongames.domain.model.EventChoice
import com.liveongames.domain.model.EventOutcome
import com.liveongames.liveon.viewmodel.EventViewModel

@Composable
fun EventScreen(
    viewModel: EventViewModel = hiltViewModel()
) {
    val activeEvents by viewModel.activeEvents.collectAsState()
    val currentEvent by viewModel.currentEvent.collectAsState()
    val selectedChoice by viewModel.selectedChoice.collectAsState()
    val eventOutcomes by viewModel.eventOutcomes.collectAsState()

    // Add refresh button to top of screen
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with refresh button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Life Events",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { viewModel.reloadEvents() }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Events"
                )
            }
        }

        if (currentEvent != null) {
            EventDetailDialog(
                event = currentEvent!!,
                selectedChoice = selectedChoice,
                eventOutcomes = eventOutcomes,
                onChoiceSelected = { choice -> viewModel.selectChoice(choice) },
                onDismiss = { viewModel.hideEvent() },
                onBack = { viewModel.clearSelectedChoice() }
            )
        } else {
            EventListScreen(
                events = activeEvents,
                onEventClick = { event -> viewModel.showEvent(event) }
            )
        }
    }
}

@Composable
fun EventListScreen(
    events: List<Event>,
    onEventClick: (Event) -> Unit
) {
    if (events.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No events available",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Try refreshing or check back later",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(events) { event ->
                EventCard(
                    event = event,
                    onClick = { onEventClick(event) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (event.isMature) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Mature",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Age: ${event.minAge}-${event.maxAge}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = event.category.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (event.type != "NEUTRAL") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Type: ${event.type}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailDialog(
    event: Event,
    selectedChoice: EventChoice?,
    eventOutcomes: List<EventOutcome>,
    onChoiceSelected: (EventChoice) -> Unit,
    onDismiss: () -> Unit,
    onBack: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    if (event.isMature) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = "Mature",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 400.dp)
            ) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (selectedChoice == null) {
                    // Show choices
                    Text(
                        text = "Choose an option:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(event.choices) { choice ->
                            ChoiceButton(
                                choice = choice,
                                onClick = { onChoiceSelected(choice) }
                            )
                        }
                    }
                } else {
                    // Show outcomes
                    Text(
                        text = "Outcome:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(eventOutcomes) { outcome ->
                            OutcomeItem(outcome = outcome)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedChoice != null) {
                TextButton(onClick = onDismiss) {
                    Text("Continue")
                }
            }
        },
        dismissButton = {
            if (selectedChoice != null) {
                TextButton(onClick = onBack) {
                    Text("Back")
                }
            }
        }
    )
}

@Composable
fun ChoiceButton(
    choice: EventChoice,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = choice.text.ifEmpty { choice.description },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (choice.description.isNotEmpty() && choice.text.isNotEmpty() && choice.text != choice.description) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = choice.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun OutcomeItem(outcome: EventOutcome) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = outcome.description,
                style = MaterialTheme.typography.bodyMedium
            )

            if (outcome.statChanges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Effects:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn {
                    items(outcome.statChanges.toList()) { (attribute, change) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = attribute.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = if (change > 0) "+$change" else "$change",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (change > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (outcome.ageProgression > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Age increases by ${outcome.ageProgression} year(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}