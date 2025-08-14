// app/src/main/java/com/liveongames/liveon/MainActivity.kt
package com.liveongames.liveon

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.liveongames.liveon.ui.LiveonGameScreen
import com.liveongames.liveon.ui.screens.CrimeScreen
import com.liveongames.liveon.ui.screens.PetsScreen
import com.liveongames.liveon.ui.screens.SettingsScreen
import com.liveongames.liveon.ui.screens.education.EducationSheet
import com.liveongames.liveon.ui.theme.LiveonTheme
import com.liveongames.liveon.viewmodel.CrimeViewModel
import com.liveongames.liveon.viewmodel.EducationViewModel
import com.liveongames.liveon.viewmodel.GameViewModel
import com.liveongames.liveon.viewmodel.PetsViewModel
import com.liveongames.liveon.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LiveonTheme {
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
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    )
        }
    }
}

@Composable
fun LiveonApp() {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val sharedGameViewModel: GameViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            LiveonGameScreen(
                gameViewModel = sharedGameViewModel,
                settingsViewModel = settingsViewModel,
                onNavigateToCrime = { navController.navigate("crime") },
                onNavigateToPets = { navController.navigate("pets") },
                onNavigateToEducation = { navController.navigate("education_popup") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("crime") {
            val crimeViewModel: CrimeViewModel = hiltViewModel()
            CrimeScreen(
                viewModel = crimeViewModel,
                settingsViewModel = settingsViewModel,
                onCrimeCommitted = { sharedGameViewModel.refreshPlayerStats() },
                onDismiss = { navController.popBackStack() }
            )
        }

        // Education POPUP destination -> show the new sheet
        composable("education_popup") {
            val eduVm: EducationViewModel = hiltViewModel()
            EducationSheet(
                onDismiss = { navController.popBackStack() },
                viewModel = eduVm
            )
        }

        composable("settings") {
            SettingsScreen(viewModel = settingsViewModel)
        }
    }
}