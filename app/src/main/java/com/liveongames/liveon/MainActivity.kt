// app/src/main/java/com/liveongames/liveon/MainActivity.kt
package com.liveongames.liveon

import android.util.Log
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.liveongames.liveon.ui.LiveonGameScreen
import com.liveongames.liveon.ui.viewmodel.GameViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiveonApp()
        }
    }
}

@Composable
fun LiveonApp() {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    var showCrimeScreen by remember { mutableStateOf(false) }

    // GET THE SAME GameViewModel INSTANCE FOR BOTH SCREENS
    val sharedGameViewModel: GameViewModel = hiltViewModel()

    // Main app with optional crime screen overlay
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "main"
        ) {
            composable("main") {
                LiveonGameScreen(
                    gameViewModel = sharedGameViewModel, // Use shared instance
                    settingsViewModel = settingsViewModel,
                    onNavigateToCrime = { showCrimeScreen = true },
                    onNavigateToPets = { navController.navigate("pets") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            composable("pets") {
                val petsViewModel: PetsViewModel = hiltViewModel()
                PetsScreen(
                    viewModel = petsViewModel,
                    settingsViewModel = settingsViewModel
                )
            }

            composable("settings") {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }

        // Crime screen as modal overlay - USE THE SAME GameViewModel INSTANCE
        if (showCrimeScreen) {
            val crimeViewModel: CrimeViewModel = hiltViewModel()
            CrimeScreen(
                viewModel = crimeViewModel,
                settingsViewModel = settingsViewModel,
                onCrimeCommitted = {
                    Log.d("MainActivity", "Crime committed, updating GameViewModel immediately")
                    // Force immediate refresh of the shared GameViewModel
                    sharedGameViewModel.refreshPlayerStats()

                    // Also do it with delay to ensure DB sync
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1000) // Give time for DB operations
                        sharedGameViewModel.refreshPlayerStats()
                        Log.d("MainActivity", "Delayed GameViewModel refresh completed")
                    }
                },
                onDismiss = { showCrimeScreen = false }
            )
        }
    }
}