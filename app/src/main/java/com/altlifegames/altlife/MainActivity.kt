package com.altlifegames.altlife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.altlifegames.altlife.viewmodel.GameViewModel
import com.altlifegames.altlife.viewmodel.Character
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AltLifeTheme {
                AltLifeGameScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AltLifeGameScreen(viewModel: GameViewModel = hiltViewModel()) {
    val character by viewModel.currentCharacter.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AltLife Alpha") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Life Simulator",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Divider()
            
            character?.let { char ->
                CharacterCard(character = char)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { viewModel.advanceYear() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Advance Year")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { viewModel.createNewCharacter() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("New Character")
                    }
                }
            } ?: run {
                Text("Loading character...")
            }
        }
    }
}

@Composable
fun CharacterCard(character: Character) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${character.firstName} ${character.lastName}",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Text("Age: ${character.age}")
            Text("Health: ${character.health}")
            Text("Happiness: ${character.happiness}")
            Text("Intelligence: ${character.intelligence}")
            Text("Money: $${character.money}")
        }
    }
}

@Composable
fun AltLifeTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        content()
    }
}