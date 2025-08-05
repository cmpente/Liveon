// app/src/main/java/com/liveongames/liveon/ui/screens/CrimeScreen.kt
package com.liveongames.liveon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.domain.model.CrimeType
import com.liveongames.liveon.viewmodel.CrimeViewModel

@Composable
fun CrimeScreen(viewModel: CrimeViewModel = hiltViewModel()) {
    val crimes by viewModel.crimes.collectAsState()
    var selectedType by remember { mutableStateOf(CrimeType.THEFT) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Criminal Record",
            style = MaterialTheme.typography.headlineMedium
        )

        // Display crimes
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(crimes) { crime ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "${crime.name} (Severity: ${crime.severity})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = crime.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (crime.fine > 0) {
                            Text(
                                text = "Fine: $${crime.fine}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (crime.jailTime > 0) {
                            Text(
                                text = "Jail Time: ${crime.jailTime} days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "Commit a Crime",
            style = MaterialTheme.typography.headlineSmall
        )

        Button(
            onClick = {
                val types = CrimeType.values()
                val currentIndex = types.indexOf(selectedType)
                selectedType = types[(currentIndex + 1) % types.size]
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Type: ${selectedType.name}")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.commitCrime(selectedType) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Commit Crime")
            }

            Button(
                onClick = { viewModel.clearRecord() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Clear Record")
            }
        }
    }
}