package com.liveongames.liveon.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.liveongames.liveon.ui.screens.CrimeScreen
import com.liveongames.liveon.ui.screens.education.EducationSheet
import com.liveongames.liveon.ui.screens.home.LiveonGameScreen
import com.liveongames.liveon.viewmodel.GameViewModel

@Composable
fun RootScaffold(
    navController: NavHostController = rememberNavController(),
    gameViewModel: GameViewModel = hiltViewModel()
) {
    Scaffold(
        // Persistent panel (stats/life-management) – always present
        bottomBar = {
            PersistentStatsBar(
                vm = gameViewModel,
                onOpenLifeManagement = { /* open your panel or route */ }
            )
        }
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(inner)
        ) {
            composable("home") {
                LiveonGameScreen(
                    onNavigateToCrime = { navController.navigate("crime") },
                    onNavigateToEducation = { navController.navigate("education") }
                )
            }
            composable("crime") {
                CrimeScreen(
                    onDismiss = { navController.popBackStack() },
                    onCrimeCommitted = { gameViewModel.refreshPlayerStats() }
                )
            }
            composable("education") {
                EducationSheet(
                    onDismiss = { navController.popBackStack() }
                )
            }
        }
    }
}
