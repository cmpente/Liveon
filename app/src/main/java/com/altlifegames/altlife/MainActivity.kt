// app/src/main/java/com/altlifegames/altlife/MainActivity.kt
package com.altlifegames.altlife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.altlifegames.altlife.ui.viewmodel.GameViewModel
import com.altlifegames.altlife.ui.viewmodel.GameUiState
import com.altlifegames.domain.model.GameEvent
import com.altlifegames.domain.model.CharacterStats
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AltLifeGameScreen()
            }
        }
    }
}

@Composable
fun AltLifeGameScreen(viewModel: GameViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AltLife Alpha - Life Simulator",
            style = MaterialTheme.typography.headlineMedium
        )

        // Display error if any
        uiState.error?.let { error ->
            Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Display player stats
        StatsCard(stats = uiState.playerStats)

        // Age up button
        Button(
            onClick = { viewModel.ageUp() },
            enabled = !uiState.isLoading && !uiState.showEventDialog,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                Text("Loading...")
            } else {
                Text("Age Up One Year")
            }
        }

        // Show events if available
        if (uiState.showEventDialog && uiState.activeEvents.isNotEmpty()) {
            EventDialog(
                event = uiState.activeEvents.first(),
                onChoiceSelected = { choiceId ->
                    viewModel.makeChoice(uiState.activeEvents.first().id, choiceId)
                },
                onDismiss = {
                    viewModel.dismissEvent(uiState.activeEvents.first().id)
                }
            )
        }

        // Clear error button
        uiState.error?.let {
            Button(
                onClick = { viewModel.clearError() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text("Clear Error")
            }
        }
    }
}

@Composable
fun StatsCard(stats: CharacterStats?) {
    if (stats == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Character Stats",
                style = MaterialTheme.typography.headlineSmall
            )

            Text("Health: ${stats.health}")
            Text("Happiness: ${stats.happiness}")
            Text("Intelligence: ${stats.intelligence}")
            Text("Money: $${stats.money}")
            Text("Social: ${stats.social}")
            Text("Age: ${stats.age}")
        }
    }
}

@Composable
fun EventDialog(
    event: GameEvent,
    onChoiceSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = event.description,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            if (event.choices.isNotEmpty()) {
                Text(
                    text = "Choices:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                event.choices.forEach { choice ->
                    Button(
                        onClick = { onChoiceSelected(choice.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = choice.text,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Dismiss",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}