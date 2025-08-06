// app/src/main/java/com/liveongames/liveon/ui/screens/PetsScreen.kt
package com.liveongames.liveon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.liveon.ui.theme.AllGameThemes
import com.liveongames.liveon.viewmodel.PetsViewModel
import com.liveongames.liveon.viewmodel.SettingsViewModel

@Composable
fun PetsScreen(
    viewModel: PetsViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val pets by viewModel.pets.collectAsState()
    val selectedThemeIndex by settingsViewModel.selectedThemeIndex.collectAsState()
    val currentTheme = AllGameThemes.getOrElse(selectedThemeIndex) { AllGameThemes[0] }
    var petName by remember { mutableStateOf(TextFieldValue("")) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Your Pets",
                style = MaterialTheme.typography.headlineMedium,
                color = currentTheme.text
            )

            // Display pets
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(pets) { pet ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = currentTheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${pet.name} (${pet.type})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = currentTheme.text
                                )
                                Text(
                                    text = "Happiness: ${pet.happiness} | Cost: $${pet.cost}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = currentTheme.accent
                                )
                            }
                            Button(
                                onClick = { viewModel.removePet(pet.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }

            Text(
                text = "Adopt New Pet",
                style = MaterialTheme.typography.headlineSmall,
                color = currentTheme.text
            )

            OutlinedTextField(
                value = petName.text,
                onValueChange = { petName = petName.copy(text = it) },
                label = { Text("Pet Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (petName.text.isNotBlank()) {
                        viewModel.adoptPet(petName.text)
                        petName = TextFieldValue("")
                    }
                },
                enabled = petName.text.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primary)
            ) {
                Text("Adopt Pet", color = currentTheme.text)
            }
        }
    }
}