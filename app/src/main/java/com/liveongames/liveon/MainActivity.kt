// app/src/main/java/com/liveongames/liveon/MainActivity.kt
package com.liveongames.liveon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.liveongames.liveon.ui.screens.CrimeScreen
import com.liveongames.liveon.viewmodel.CrimeViewModel
import com.liveongames.liveon.ui.screens.PetsScreen
import com.liveongames.liveon.viewmodel.PetsViewModel
import com.liveongames.liveon.ui.screens.SettingsScreen
import com.liveongames.liveon.viewmodel.SettingsViewModel
import com.liveongames.liveon.ui.viewmodel.GameViewModel
import com.liveongames.liveon.ui.viewmodel.GameUiState
import com.liveongames.domain.model.GameEvent
import com.liveongames.domain.model.CharacterStats
import dagger.hilt.android.AndroidEntryPoint
import com.liveongames.liveon.R

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                liveonApp()
            }
        }
    }
}

@Composable
fun liveonApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            liveongamescreen(
                onNavigateToCrime = { navController.navigate("crime") },
                onNavigateToPets = { navController.navigate("pets") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("crime") {
            val crimeViewModel: CrimeViewModel = hiltViewModel()
            CrimeScreen(viewModel = crimeViewModel)
        }

        composable("pets") {
            val petsViewModel: PetsViewModel = hiltViewModel()
            PetsScreen(viewModel = petsViewModel)
        }

        composable("settings") {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(viewModel = settingsViewModel)
        }
    }
}

@Composable
fun liveongamescreen(
    viewModel: GameViewModel = hiltViewModel(),
    onNavigateToCrime: () -> Unit = {},
    onNavigateToPets: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
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
                .padding(16.dp)
                .padding(bottom = 120.dp), // Space for stats at bottom
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "liveon Alpha",
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

            // Life Log Section
            LifeLogSection()

            Spacer(modifier = Modifier.height(24.dp))

            // Main Action Buttons
            MainActionButtons(viewModel = viewModel, uiState = uiState)

            Spacer(modifier = Modifier.height(24.dp))

            // Game Systems Menu
            GameSystemsMenu(
                onNavigateToCrime = onNavigateToCrime,
                onNavigateToPets = onNavigateToPets,
                onNavigateToSettings = onNavigateToSettings
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Character Stats at Bottom (minimized)
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
fun LifeLogSection() {
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
                Icon(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = "Info",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
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
}

@Composable
fun MainActionButtons(viewModel: GameViewModel, uiState: GameUiState) {
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
                Icon(
                    painter = painterResource(id = R.drawable.ic_continue),
                    contentDescription = "Continue",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Age Up One Year",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary
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
                Icon(
                    painter = painterResource(id = R.drawable.ic_business),
                    contentDescription = "Business",
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
                Icon(
                    painter = painterResource(id = R.drawable.ic_relationship),
                    contentDescription = "Relationships",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Social", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun GameSystemsMenu(
    onNavigateToCrime: () -> Unit = {},
    onNavigateToPets: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
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
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary
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
                MenuItemData(
                    iconResId = R.drawable.ic_law,
                    title = "Crime Records",
                    description = "View criminal history"
                ) { onNavigateToCrime() },

                MenuItemData(
                    iconResId = R.drawable.ic_band,
                    title = "Pet Management",
                    description = "Adopt and care for pets"
                ) { onNavigateToPets() },

                MenuItemData(
                    iconResId = R.drawable.ic_save,
                    title = "Save Game",
                    description = "Manage game saves"
                ) { /* TODO: Implement save functionality */ },

                MenuItemData(
                    iconResId = R.drawable.ic_settings,
                    title = "Settings",
                    description = "Game preferences"
                ) { onNavigateToSettings() }
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

data class MenuItemData(
    val iconResId: Int,
    val title: String,
    val description: String,
    val onClick: () -> Unit
)

@Composable
fun MenuItemRow(item: MenuItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = item.iconResId),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurface
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

        Icon(
            painter = painterResource(id = R.drawable.ic_continue),
            contentDescription = "Navigate",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CharacterStatsBottom(
    stats: CharacterStats?,
    modifier: Modifier = Modifier
) {
    if (stats == null) return

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(12.dp) // Reduced padding
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = "Person",
                    modifier = Modifier.size(20.dp), // Smaller icon
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Stats",
                    style = MaterialTheme.typography.titleSmall, // Smaller text
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing

            // Compact stat bars
            CompactStatRow(
                label = "Age",
                value = stats.age,
                maxValue = 100,
                iconResId = R.drawable.ic_person
            )

            CompactStatRow(
                label = "Health",
                value = stats.health,
                maxValue = 100,
                iconResId = R.drawable.ic_health
            )

            CompactStatRow(
                label = "Happy",
                value = stats.happiness,
                maxValue = 100,
                iconResId = R.drawable.ic_happiness
            )

            CompactStatRow(
                label = "Smart",
                value = stats.intelligence,
                maxValue = 100,
                iconResId = R.drawable.ic_smarts
            )

            CompactStatRow(
                label = "Money",
                value = stats.money,
                maxValue = 10000,
                iconResId = R.drawable.ic_money,
                isCurrency = true
            )

            CompactStatRow(
                label = "Social",
                value = stats.social,
                maxValue = 100,
                iconResId = R.drawable.ic_relationship
            )
        }
    }
}

@Composable
fun CompactStatRow(
    label: String,
    value: Int,
    maxValue: Int,
    iconResId: Int,
    isCurrency: Boolean = false
) {
    val percentage = (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
    val displayValue = if (isCurrency) "$$value" else "$value/$maxValue"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(16.dp), // Even smaller icons
            tint = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(40.dp) // Fixed width for labels
        )

        Spacer(modifier = Modifier.width(4.dp))

        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier.weight(1f)
                .height(6.dp), // Thinner progress bar
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = displayValue,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(50.dp) // Fixed width for values
        )
    }

    Spacer(modifier = Modifier.height(4.dp)) // Minimal spacing between rows
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