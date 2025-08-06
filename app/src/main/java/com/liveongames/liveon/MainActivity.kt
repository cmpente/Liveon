// app/src/main/java/com/liveongames/liveon/MainActivity.kt
package com.liveongames.liveon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
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
import com.liveongames.liveon.ui.LiveonGameScreen // This is your advanced game screen
import dagger.hilt.android.AndroidEntryPoint

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

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            // Use your advanced LiveonGameScreen directly here
            LiveonGameScreen() // This is from com.liveongames.liveon.ui.LiveonGameScreen
        }

        composable("crime") {
            val crimeViewModel: CrimeViewModel = hiltViewModel()
            CrimeScreen(viewModel = crimeViewModel)
        }

        composable("pets") {
            val petsViewModel: PetsViewModel = hiltViewModel()
            PetsScreen(viewModel = petsViewModel)
        }

        composable("settings") {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(viewModel = settingsViewModel)
        }
    }
}