// app/src/main/java/com/liveongames/liveon/ui/liveongamescreen.kt
package com.liveongames.liveon.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.viewmodel.GameViewModel
import com.liveongames.liveon.ui.viewmodel.GameUiState
import com.liveongames.domain.model.GameEvent

@Composable
fun liveongamescreen(viewModel: GameViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Liveon",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Life Simulator",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Empty Life Log Section (placeholder for now)
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Life Log",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your life story will appear here...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Action Buttons
            MainActionButtons(
                viewModel = viewModel,
                uiState = uiState
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Game Systems Menu
            GameSystemsMenu(viewModel = viewModel)

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Character Stats at Bottom
        CharacterStatsBottom(
            stats = uiState.playerStats,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )

        // Events Dialog
        if (uiState.showEventDialog && uiState.activeEvents.isNotEmpty()) {
            EventDialogComposable(
                event = uiState.activeEvents.first(),
                onChoiceSelected = { choiceId ->
                    viewModel.makeChoice(uiState.activeEvents.first().id, choiceId)
                },
                onDismiss = {
                    viewModel.dismissEvent(uiState.activeEvents.first().id)
                }
            )
        }
    }
}

@Composable
fun MainActionButtons(
    viewModel: GameViewModel,
    uiState: GameUiState
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { viewModel.ageUp() },
            enabled = !uiState.isLoading && !uiState.showEventDialog,
            modifier = Modifier.fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_continue),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Age Up One Year",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { /* TODO: Open career menu */ },
                modifier = Modifier.weight(1f)
                    .height(50.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_business),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Career", fontSize = 14.sp)
            }

            OutlinedButton(
                onClick = { /* TODO: Open relationships menu */ },
                modifier = Modifier.weight(1f)
                    .height(50.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_relationship),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Social", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun GameSystemsMenu(viewModel: GameViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Game Systems",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val menuItems = listOf(
                MenuItem(
                    iconResId = R.drawable.ic_law,
                    title = "Crime Records",
                    description = "View criminal history"
                ) { /* TODO: Open crime system */ },

                MenuItem(
                    iconResId = R.drawable.ic_band,
                    title = "Pet Management",
                    description = "Adopt and care for pets"
                ) { /* TODO: Open pet system */ },

                MenuItem(
                    iconResId = R.drawable.ic_save,
                    title = "Save Game",
                    description = "Manage game saves"
                ) { /* TODO: Implement save functionality */ },

                MenuItem(
                    iconResId = R.drawable.ic_settings,
                    title = "Settings",
                    description = "Game preferences"
                ) { /* TODO: Open settings */ }
            )

            menuItems.forEachIndexed { index, item ->
                MenuItemRow(item = item)
                if (index < menuItems.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

data class MenuItem(
    val iconResId: Int,
    val title: String,
    val description: String,
    val onClick: () -> Unit
)

@Composable
fun MenuItemRow(item: MenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = item.iconResId),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = ">",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CharacterStatsBottom(
    stats: com.liveongames.domain.model.CharacterStats?,
    modifier: Modifier = Modifier
) {
    if (stats == null) return

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Character Stats",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            StatBarRow(
                label = "Age",
                value = stats.age.toFloat(),
                maxValue = 100f,
                iconResId = R.drawable.ic_person,
                isPercentage = false
            )

            StatBarRow(
                label = "Health",
                value = stats.health.toFloat(),
                maxValue = 100f,
                iconResId = R.drawable.ic_health,
                color = Color(0xFF4CAF50)
            )

            StatBarRow(
                label = "Happiness",
                value = stats.happiness.toFloat(),
                maxValue = 100f,
                iconResId = R.drawable.ic_happiness,
                color = Color(0xFFFF9800)
            )

            StatBarRow(
                label = "Intelligence",
                value = stats.intelligence.toFloat(),
                maxValue = 100f,
                iconResId = R.drawable.ic_smarts,
                color = Color(0xFF2196F3)
            )

            StatBarRow(
                label = "Money",
                value = stats.money.toFloat(),
                maxValue = 10000f,
                iconResId = R.drawable.ic_money,
                color = Color(0xFF9C27B0),
                isPercentage = false
            )

            StatBarRow(
                label = "Social",
                value = stats.social.toFloat(),
                maxValue = 100f,
                iconResId = R.drawable.ic_relationship,
                color = Color(0xFFF44336)
            )
        }
    }
}

@Composable
fun StatBarRow(
    label: String,
    value: Float,
    maxValue: Float,
    iconResId: Int,
    color: Color = MaterialTheme.colorScheme.primary,
    isPercentage: Boolean = true
) {
    val percentage = (value / maxValue).coerceIn(0f, 1f)
    val displayValue = if (isPercentage) "${value.toInt()}/100" else "$${value.toInt()}"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier.fillMaxWidth()
                    .height(8.dp),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun EventDialogComposable(
    event: GameEvent,
    onChoiceSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (event.choices.isNotEmpty()) {
                    Text(
                        text = "What do you choose?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    event.choices.forEach { choice ->
                        Button(
                            onClick = { onChoiceSelected(choice.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(
                                text = choice.text,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            if (event.choices.isEmpty()) {
                TextButton(onClick = onDismiss) {
                    Text("Continue")
                }
            }
        }
    )
}