// app/src/main/java/com/liveongames/liveon/ui/screens/SaveScreen.kt
package com.liveongames.liveon.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.liveon.R
import com.liveongames.liveon.managers.GameSaveManager
import com.liveongames.liveon.models.PlayerSaveData
import com.liveongames.liveon.models.SaveFileInfo
import com.liveongames.liveon.ui.theme.AllGameThemes
import com.liveongames.liveon.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SaveScreen(
    currentPlayerData: PlayerSaveData,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {},
    onLoadSave: (PlayerSaveData) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectedThemeIndex by settingsViewModel.selectedThemeIndex.collectAsState()
    val currentTheme = AllGameThemes.getOrElse(selectedThemeIndex) { AllGameThemes[0] }

    val saveManager = remember { GameSaveManager(context) }
    var saveFiles by remember { mutableStateOf<List<SaveFileInfo>>(emptyList()) }
    var showNewSaveDialog by remember { mutableStateOf(false) }
    var showLoadConfirmation by remember { mutableStateOf<SaveFileInfo?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<SaveFileInfo?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Load saves when screen opens
    LaunchedEffect(Unit) {
        saveFiles = saveManager.getSaveFiles()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onDismiss() }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .background(currentTheme.surface, RoundedCornerShape(20.dp))
                .clickable(enabled = false) { }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Save Management",
                        style = MaterialTheme.typography.headlineMedium,
                        color = currentTheme.text,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = currentTheme.text
                        )
                    }
                }

                // Current game stats
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = currentTheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Current Game",
                                style = MaterialTheme.typography.titleMedium,
                                color = currentTheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Age: ${currentPlayerData.age}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = currentTheme.text
                        )
                        Text(
                            text = "Money: $${currentPlayerData.money}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = currentTheme.text
                        )
                        Text(
                            text = "Intelligence: ${currentPlayerData.intelligence}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = currentTheme.text
                        )
                    }
                }

                // Save actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { showNewSaveDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_save),
                                contentDescription = "Save",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Game")
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            saveManager.autoSave(currentPlayerData)
                            showToast(scope) { toastMessage = "Auto-saved successfully!" }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.secondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_continue),
                                contentDescription = "Auto Save",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auto Save")
                        }
                    }
                }

                // Save file list
                Text(
                    text = "Saved Games",
                    style = MaterialTheme.typography.titleLarge,
                    color = currentTheme.text,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                if (saveFiles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No saved games found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = currentTheme.accent
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(saveFiles) { saveFile ->
                            SaveFileItem(
                                saveFile = saveFile,
                                theme = currentTheme,
                                onLoad = { showLoadConfirmation = saveFile },
                                onDelete = { showDeleteConfirmation = saveFile }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                thickness = 1.dp,
                                color = currentTheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // Toast message
        toastMessage?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = currentTheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            LaunchedEffect(message) {
                kotlinx.coroutines.delay(3000)
                toastMessage = null
            }
        }
    }

    // New Save Dialog
    if (showNewSaveDialog) {
        NewSaveDialog(
            theme = currentTheme,
            onSave = { saveName ->
                if (saveManager.saveGame(saveName, currentPlayerData)) {
                    saveFiles = saveManager.getSaveFiles()
                    showToast(scope) { toastMessage = "Game saved successfully!" }
                } else {
                    showToast(scope) { toastMessage = "Failed to save game" }
                }
                showNewSaveDialog = false
            },
            onDismiss = { showNewSaveDialog = false }
        )
    }

    // Load Confirmation Dialog
    showLoadConfirmation?.let { saveFile ->
        AlertDialog(
            onDismissRequest = { showLoadConfirmation = null },
            title = {
                Text(
                    text = "Load Save",
                    style = MaterialTheme.typography.headlineSmall,
                    color = currentTheme.primary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to load this save? Your current progress will be lost.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = currentTheme.text
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        saveManager.loadGame(saveFile.name)?.let { gameSave ->
                            onLoadSave(gameSave.playerData)
                            showLoadConfirmation = null
                            onDismiss()
                        } ?: run {
                            showToast(scope) { toastMessage = "Failed to load save" }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Load")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLoadConfirmation = null },
                    border = BorderStroke(1.dp, currentTheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = currentTheme.primary)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = currentTheme.surface
        )
    }

    // Delete Confirmation Dialog
    showDeleteConfirmation?.let { saveFile ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = {
                Text(
                    text = "Delete Save",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Red
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this save? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = currentTheme.text
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (saveManager.deleteSave(saveFile.name)) {
                            saveFiles = saveManager.getSaveFiles()
                            showToast(scope) { toastMessage = "Save deleted successfully" }
                        } else {
                            showToast(scope) { toastMessage = "Failed to delete save" }
                        }
                        showDeleteConfirmation = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmation = null },
                    border = BorderStroke(1.dp, currentTheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = currentTheme.primary)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = currentTheme.surface
        )
    }
}

@Composable
fun SaveFileItem(
    saveFile: SaveFileInfo,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLoad() },
        colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = saveFile.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = theme.text,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = saveFile.lastPlayed,
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.accent
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Age: ${saveFile.playerAge}",
                style = MaterialTheme.typography.bodyMedium,
                color = theme.text
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onLoad,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFF4CAF50))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_continue),
                        contentDescription = "Load"
                    )
                }

                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Red)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_save),
                        contentDescription = "Delete"
                    )
                }
            }
        }
    }
}

@Composable
fun NewSaveDialog(
    theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var saveName by remember { mutableStateOf("") }
    val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Save Game",
                style = MaterialTheme.typography.headlineSmall,
                color = theme.primary
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter a name for your save:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.text
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = saveName,
                    onValueChange = { saveName = it },
                    label = { Text("Save Name", color = theme.accent) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = theme.surface,
                        unfocusedContainerColor = theme.surface,
                        focusedTextColor = theme.text,
                        unfocusedTextColor = theme.text,
                        focusedLabelColor = theme.primary,
                        unfocusedLabelColor = theme.accent
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Auto-generated name: Save_$currentDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.accent
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalSaveName = saveName.ifBlank { "Save_$currentDate" }
                    onSave(finalSaveName)
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                enabled = true
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, theme.primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.primary)
            ) {
                Text("Cancel")
            }
        },
        containerColor = theme.surface
    )
}

private fun showToast(scope: CoroutineScope, showToast: () -> Unit) {
    scope.launch {
        showToast()
        kotlinx.coroutines.delay(3000)
    }
}