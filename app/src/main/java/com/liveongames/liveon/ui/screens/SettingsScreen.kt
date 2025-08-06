// app/src/main/java/com/liveongames/liveon/ui/screens/SettingsScreen.kt
package com.liveongames.liveon.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.liveon.viewmodel.SettingsViewModel
import com.liveongames.liveon.ui.theme.AllGameThemes

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    var darkMode by remember { mutableStateOf(false) }
    val matureContent by viewModel.matureContentEnabled.collectAsState()
    val selectedThemeIndex by viewModel.selectedThemeIndex.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dark Mode",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = darkMode,
                        onCheckedChange = { darkMode = it }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mature Content",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = matureContent,
                        onCheckedChange = { viewModel.toggleMatureContent() }
                    )
                }
            }
        }

        // Theme Selection Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                AllGameThemes.forEachIndexed { index, theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectTheme(index) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = index == selectedThemeIndex,
                            onClick = { viewModel.selectTheme(index) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = theme.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}