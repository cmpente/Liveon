// app/src/main/java/com/liveongames/liveon/MainActivity.kt
package com.liveongames.liveon

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.liveongames.liveon.character.CharacterCreationScreen
import com.liveongames.liveon.character.NewLifePayload
import com.liveongames.liveon.ui.LiveonChromeHost
import com.liveongames.liveon.ui.LiveonGameScreen
import com.liveongames.liveon.ui.LocalChromeInsets
import com.liveongames.liveon.ui.screens.CrimeScreen
import com.liveongames.liveon.ui.screens.SettingsScreen
import com.liveongames.liveon.ui.screens.education.EducationSheet
import com.liveongames.liveon.ui.theme.AllGameThemes
import com.liveongames.liveon.ui.theme.LiveonTheme
import com.liveongames.liveon.viewmodel.CrimeViewModel
import com.liveongames.liveon.viewmodel.EducationViewModel
import com.liveongames.liveon.viewmodel.GameViewModel
import com.liveongames.liveon.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.currentBackStackEntryAsState
import com.liveongames.liveon.ui.LocalChromeInsets

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LiveonApp() }
        enableFullScreen()
    }

    private fun enableFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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

    val settingsVm: SettingsViewModel = hiltViewModel()
    val gameVm: GameViewModel = hiltViewModel()

    val selectedThemeIndex by settingsVm.selectedThemeIndex.collectAsStateWithLifecycle()
    val selectedTheme = AllGameThemes.getOrElse(selectedThemeIndex) {
        AllGameThemes.firstOrNull() ?: AllGameThemes[0]
    }

    LiveonTheme(
        theme = selectedTheme,
        darkTheme = isSystemInDarkTheme()
    ) {
        // Persistent chrome (stats + life management)
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val showBrandHeader = currentRoute == "main"
        LiveonChromeHost(
            showHeader = showBrandHeader,
            gameViewModel = gameVm,
            settingsViewModel = settingsVm,
            onNavigateToCrime = {
                navController.navigate("crime") {
                    popUpTo("main") { inclusive = false }
                    launchSingleTop = true
                }
            },
            onNavigateToEducation = {
                navController.navigate("education") {
                    popUpTo("main") { inclusive = false }
                    launchSingleTop = true
                }
            },
            onNavigateToPets = { /* later */ },
            onNavigateToSettings = {
                navController.navigate("settings") {
                    popUpTo("main") { inclusive = false }
                    launchSingleTop = true
                }
            },
            onNavigateToNewLife = {
                navController.navigate("new_life") {
                    popUpTo("main") { inclusive = false }
                    launchSingleTop = true
                }
            }
        ) {
            // App navigation lives inside the chrome so it persists across screens
            NavHost(
                navController = navController,
                startDestination = "main"
            ) {
                composable("main") {
                    LiveonGameScreen(
                        gameViewModel = gameVm,
                        settingsViewModel = settingsVm,
                        onNavigateToCrime = { navController.navigate("crime") },
                        onNavigateToPets = { /* later */ },
                        onNavigateToEducation = { navController.navigate("education") },
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToNewLife = { navController.navigate("new_life") }
                    )
                }

                composable("crime") {
                    val insets = LocalChromeInsets.current
                    val crimeVm: CrimeViewModel = hiltViewModel()
                    Box(Modifier.padding(bottom = insets.bottom)) {
                        CrimeScreen(
                            viewModel = crimeVm,
                            onCrimeCommitted = { gameVm.refreshPlayerStats() },
                            onDismiss = { navController.popBackStack() }
                        )
                    }
                }

                composable("education") {
                    val insets = LocalChromeInsets.current
                    val eduVm: EducationViewModel = hiltViewModel()
                    Box(Modifier.padding(bottom = insets.bottom)) {
                        EducationSheet(
                            onDismiss = { navController.popBackStack() },
                            viewModel = eduVm
                        )
                    }
                }

                // New Life (Character Creation)
                composable("new_life") {
                    val insets = LocalChromeInsets.current
                    Box(Modifier.padding(bottom = insets.bottom)) {
                        CharacterCreationScreen(
                            onDismiss = { navController.popBackStack() },
                            onComplete = { payload: NewLifePayload ->
                                // Wire into your game state and return to main
                                gameVm.startNewLife(payload.profile, payload.stats)
                                navController.popBackStack(route = "main", inclusive = false)
                            }
                        )
                    }
                }

                composable("settings") {
                    SettingsScreen(viewModel = settingsVm)
                }
            }
        }
    }
}