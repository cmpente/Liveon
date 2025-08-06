// app/src/main/java/com/liveongames/liveon/ui/screens/CrimeScreen.kt
package com.liveongames.liveon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.domain.model.CrimeType
import com.liveongames.liveon.ui.theme.AllGameThemes
import com.liveongames.liveon.viewmodel.CrimeViewModel
import com.liveongames.liveon.viewmodel.SettingsViewModel

@Composable
fun CrimeScreen(
    viewModel: CrimeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val crimes by viewModel.crimes.collectAsState()
    val selectedThemeIndex by settingsViewModel.selectedThemeIndex.collectAsState()
    val currentTheme = AllGameThemes.getOrElse(selectedThemeIndex) { AllGameThemes[0] }
    var showClearDialog by remember { mutableStateOf(false) }

    // Main content with vertical scroll - NO LazyColumn inside!
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Criminal Record",
            style = MaterialTheme.typography.headlineMedium,
            color = currentTheme.text,
            fontWeight = FontWeight.Bold
        )

        // Current crimes display - USING Column instead of LazyColumn
        Card(
            colors = CardDefaults.cardColors(containerColor = currentTheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (crimes.isEmpty()) {
                    Text(
                        text = "You have a clean record!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = currentTheme.text
                    )
                } else {
                    // Use Column with limited items instead of LazyColumn to avoid nesting
                    crimes.take(10).forEach { crime -> // Limit to 10 items to prevent excessive height
                        Column(
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "${crime.name} (Severity: ${crime.severity})",
                                style = MaterialTheme.typography.titleMedium,
                                color = currentTheme.text
                            )
                            Text(
                                text = "Fine: $${crime.fine} | Jail Time: ${crime.jailTime} days",
                                style = MaterialTheme.typography.bodySmall,
                                color = currentTheme.accent
                            )
                            Text(
                                text = crime.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = currentTheme.accent.copy(alpha = 0.7f)
                            )
                        }
                        // Add spacing between items
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // If there are more than 10 crimes, show a message
                    if (crimes.size > 10) {
                        Text(
                            text = "+${crimes.size - 10} more crimes...",
                            style = MaterialTheme.typography.bodySmall,
                            color = currentTheme.accent
                        )
                    }
                }
            }
        }

        Text(
            text = "Commit a Crime",
            style = MaterialTheme.typography.headlineSmall,
            color = currentTheme.text
        )

        // Crime Selection - Two Columns with all crime types
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Define crime types in order
            val crimeTypes = listOf(
                CrimeType.THEFT to "Steal items for money",
                CrimeType.ASSAULT to "Physical violence",
                CrimeType.FRAUD to "Deceive for money",
                CrimeType.DRUG_POSSESSION to "Possess substances",
                CrimeType.DRUG_DEALING to "Sell substances",
                CrimeType.MURDER to "Take a life",
                CrimeType.EXTORTION to "Threaten for money",
                CrimeType.VANDALISM to "Damage property"
            )

            // Group crimes into pairs for 2-column layout
            val crimePairs = crimeTypes.chunked(2)

            crimePairs.forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // First crime in pair
                    val (crimeType1, description1) = pair[0]
                    CrimeTypeButton(
                        iconResId = getCrimeIcon(crimeType1),
                        title = crimeType1.name.replace("_", " "),
                        description = description1,
                        onClick = { viewModel.commitCrime(crimeType1) },
                        theme = currentTheme,
                        modifier = Modifier.weight(1f)
                    )

                    // Second crime in pair (if exists)
                    if (pair.size > 1) {
                        val (crimeType2, description2) = pair[1]
                        CrimeTypeButton(
                            iconResId = getCrimeIcon(crimeType2),
                            title = crimeType2.name.replace("_", " "),
                            description = description2,
                            onClick = { viewModel.commitCrime(crimeType2) },
                            theme = currentTheme,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Clear Record Button
        Button(
            onClick = { showClearDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Clear Criminal Record ($5000)")
        }

        // Add some bottom padding to ensure everything is visible
        Spacer(modifier = Modifier.height(16.dp))
    }

    // Clear Record Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "Clear Criminal Record?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = currentTheme.text
                )
            },
            text = {
                Text(
                    text = "This will cost $5000 and permanently clear your criminal record. Are you sure?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = currentTheme.accent
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearRecord()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = currentTheme.surface
        )
    }
}

@Composable
fun CrimeTypeButton(
    iconResId: Int,
    title: String,
    description: String,
    onClick: () -> Unit,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(90.dp) // Reduced height to fit better
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp), // Reduced padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp) // Smaller icon
                    .background(
                        color = theme.primary.copy(alpha = 0.1f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp), // Smaller icon
                    tint = theme.primary
                )
            }

            Spacer(modifier = Modifier.width(8.dp)) // Reduced spacing

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = theme.text,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.accent,
                    maxLines = 2 // Limit to 2 lines
                )
            }
        }
    }
}

// Helper function for crime icons
fun getCrimeIcon(crimeType: CrimeType): Int {
    return when (crimeType) {
        CrimeType.THEFT -> android.R.drawable.ic_menu_compass
        CrimeType.ASSAULT -> android.R.drawable.ic_menu_view
        CrimeType.FRAUD -> android.R.drawable.ic_menu_edit
        CrimeType.DRUG_POSSESSION -> android.R.drawable.ic_menu_preferences
        CrimeType.DRUG_DEALING -> android.R.drawable.ic_menu_upload
        CrimeType.MURDER -> android.R.drawable.ic_menu_close_clear_cancel
        CrimeType.EXTORTION -> android.R.drawable.ic_lock_power_off
        CrimeType.VANDALISM -> android.R.drawable.ic_menu_delete
    }
}