// app/src/main/java/com/liveongames/liveon/MainActivity.kt
package com.liveongames.liveon

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
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
import com.liveongames.liveon.ui.screens.education.EducationScreen // <-- Corrected import path (if in /education subfolder)
import com.liveongames.liveon.ui.LiveonGameScreen
import com.liveongames.liveon.ui.viewmodel.GameViewModel
import com.liveongames.liveon.viewmodel.CrimeViewModel
import com.liveongames.liveon.viewmodel.EducationViewModel
import com.liveongames.liveon.viewmodel.PetsViewModel
import com.liveongames.liveon.viewmodel.SettingsViewModel
import com.liveongames.liveon.ui.screens.CrimeScreen
import com.liveongames.liveon.ui.screens.PetsScreen // Assuming these are in this package
import com.liveongames.liveon.ui.screens.SettingsScreen // Assuming these are in this package
import com.liveongames.liveon.ui.theme.LiveonTheme // Import the theme wrapper composable
import com.liveongames.liveon.ui.theme.PremiumSleek // Import a default theme instance
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
            LiveonTheme(liveonTheme = PremiumSleek) { // Wrap with theme
                LiveonApp()
            }
        }
        enableFullScreen()
    }

    private fun enableFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    )
        }
    }
}

@Composable
fun LiveonApp() {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    var showCrimeScreen by remember { mutableStateOf(false) }
    var showEducationScreen by remember { mutableStateOf(false) }

    val sharedGameViewModel: GameViewModel = hiltViewModel()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "main"
        ) {
            composable("main") {
                LiveonGameScreen(
                    gameViewModel = sharedGameViewModel,
                    settingsViewModel = settingsViewModel,
                    onNavigateToCrime = { showCrimeScreen = true },
                    onNavigateToPets = { navController.navigate("pets") },
                    onNavigateToEducation = { showEducationScreen = true },
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

        if (showCrimeScreen) {
            val crimeViewModel: CrimeViewModel = hiltViewModel()
            CrimeScreen(
                viewModel = crimeViewModel,
                settingsViewModel = settingsViewModel,
                onCrimeCommitted = {
                    sharedGameViewModel.refreshPlayerStats()
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1000)
                        sharedGameViewModel.refreshPlayerStats()
                    }
                },
                onDismiss = { showCrimeScreen = false }
            )
        }

        if (showEducationScreen) {
            val educationViewModel: EducationViewModel = hiltViewModel()

            EducationScreen(
                viewModel = educationViewModel
            )
        }
    }
}