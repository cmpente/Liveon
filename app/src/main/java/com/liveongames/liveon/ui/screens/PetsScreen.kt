// app/src/main/java/com/liveongames/liveon/ui/screens/PetsScreen.kt
package com.liveongames.liveon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.liveon.viewmodel.PetsViewModel

@Composable
fun PetsScreen(viewModel: PetsViewModel = hiltViewModel()) {
    val pets by viewModel.pets.collectAsState()
    var petName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Your Pets",
            style = MaterialTheme.typography.headlineMedium
        )

        // Display pets
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(pets) { pet ->
                Card(
                    modifier = Modifier.fillMaxWidth()
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
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Happiness: ${pet.happiness} | Cost: $${pet.cost}",
                                style = MaterialTheme.typography.bodyMedium
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
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = petName,
            onValueChange = { petName = it },
            label = { Text("Pet Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (petName.isNotBlank()) {
                    viewModel.adoptPet(petName)
                    petName = ""
                }
            },
            enabled = petName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Adopt Pet")
        }
    }
}