// app/src/main/java/com/liveongames/liveon/ui/viewmodel/GameViewModel.kt
package com.liveongames.liveon.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.domain.model.CharacterStats
import com.liveongames.domain.model.GameEvent
import com.liveongames.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.liveongames.liveon.character.PlayerProfile
import com.liveongames.liveon.character.PlayerStats
import kotlinx.coroutines.flow.update

@HiltViewModel
class GameViewModel @Inject constructor(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "GameViewModel"
        private const val CHARACTER_ID = "player_character"
    }

    init {
        loadGame()
    }

    private fun loadGame() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                Log.d(TAG, "Starting game load...")

                // Load actual character data
                playerRepository.getCharacter(CHARACTER_ID).collect { character ->
                    Log.d(TAG, "Repository emitted character: $character")
                    if (character != null) {
                        Log.d(TAG, "Found existing character with money: ${character.money}")
                        val initialStats = CharacterStats(
                            health = character.health,
                            happiness = character.happiness,
                            intelligence = character.intelligence,
                            money = character.money,
                            social = character.social,
                            age = character.age
                        )
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            playerStats = initialStats
                        )
                        Log.d(TAG, "UI state updated with stats: $initialStats")
                    } else {
                        Log.d(TAG, "No existing character found, creating new one")
                        createNewCharacter()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading game", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun createNewCharacter() {
        try {
            val initialStats = CharacterStats(
                health = 100,
                happiness = 50,
                intelligence = 20,
                money = 1000,
                social = 30,
                age = 18
            )

            // Create character domain model
            val character = com.liveongames.domain.model.Character(
                id = CHARACTER_ID,
                name = "Player",
                age = initialStats.age,
                health = initialStats.health,
                happiness = initialStats.happiness,
                money = initialStats.money,
                intelligence = initialStats.intelligence,
                fitness = 10,
                social = initialStats.social,
                education = 0,
                career = null,
                relationships = emptyList(),
                achievements = emptyList(),
                events = emptyList(),
                jailTime = 0,
                notoriety = 0
            )

            Log.d(TAG, "Creating new character with money: ${initialStats.money}")
            playerRepository.createCharacter(CHARACTER_ID, character)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                playerStats = initialStats
            )
            Log.d(TAG, "New character created successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error creating new character", e)
            _uiState.value = _uiState.value.copy(
                error = e.message,
                isLoading = false
            )
        }
    }

    fun startNewLife(profile: PlayerProfile, stats: PlayerStats) {
        viewModelScope.launch {
            try {
                // Create a fresh domain Character with the chosen name & stats
                val character = com.liveongames.domain.model.Character(
                    id = CHARACTER_ID,
                    name = "${profile.firstName} ${profile.lastName}",
                    age = stats.age,
                    health = stats.health,
                    happiness = stats.happiness,
                    money = stats.money,
                    intelligence = stats.intelligence,
                    fitness = 10,                 // keep default or derive if you track it
                    social = stats.social,
                    education = 0,
                    career = null,
                    relationships = emptyList(),
                    achievements = emptyList(),
                    events = emptyList(),
                    jailTime = 0,
                    notoriety = 0
                )

                // Overwrite the active character in the repository
                playerRepository.createCharacter(CHARACTER_ID, character)

                // Hydrate UI immediately
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        playerStats = com.liveongames.domain.model.CharacterStats(
                            health = stats.health,
                            happiness = stats.happiness,
                            intelligence = stats.intelligence,
                            money = stats.money,
                            social = stats.social,
                            age = stats.age
                        ),
                        showEventDialog = false,
                        activeEvents = emptyList()
                    )
                }

                // Optionally re-sync from DB (uncomment if you want to verify)
                // refreshPlayerStats()

            } catch (e: Exception) {
                Log.e(TAG, "startNewLife error", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun refreshPlayerStats() {
        Log.d(TAG, "refreshPlayerStats CALLED")
        viewModelScope.launch {
            try {
                Log.d(TAG, "refreshPlayerStats: About to fetch from repository")
                // Force collect latest data
                val job = playerRepository.getCharacter(CHARACTER_ID).take(1).collect { character ->
                    Log.d(TAG, "refreshPlayerStats: Repository returned character: $character")
                    character?.let { char ->
                        val updatedStats = CharacterStats(
                            health = char.health,
                            happiness = char.happiness,
                            intelligence = char.intelligence,
                            money = char.money,
                            social = char.social,
                            age = char.age
                        )

                        _uiState.value = _uiState.value.copy(
                            playerStats = updatedStats
                        )

                        Log.d(TAG, "refreshPlayerStats: State updated with: $updatedStats")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in refreshPlayerStats", e)
            }
        }
    }

    fun ageUp() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "ageUp called")

                // Update the character in the database using repository methods
                playerRepository.updateAge(CHARACTER_ID, 1)  // Add 1 year
                playerRepository.updateHealth(CHARACTER_ID, -2)  // Lose 2 health
                playerRepository.updateHappiness(CHARACTER_ID, -1)  // Lose 1 happiness
                playerRepository.updateIntelligence(CHARACTER_ID, 1)  // Gain 1 intelligence
                playerRepository.updateFitness(CHARACTER_ID, -1)  // Lose 1 fitness
                playerRepository.updateMoney(CHARACTER_ID, 100)  // Gain $100 (birthday money)

                // Small delay to ensure database operations complete
                kotlinx.coroutines.delay(200)

                // Force refresh the UI with updated data
                refreshPlayerStats()

                // Show simple success message
                val currentAge = uiState.value.playerStats?.age ?: 18
                _uiState.value = _uiState.value.copy(
                    showEventDialog = true,
                    activeEvents = listOf(
                        GameEvent(
                            id = "birthday_${System.currentTimeMillis()}",
                            title = "Birthday!",
                            description = "Happy Birthday! You are now ${currentAge + 1} years old. +$100!",
                            choices = emptyList()
                        )
                    )
                )

                Log.d(TAG, "ageUp completed successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Error aging up", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun makeChoice(eventId: String, choiceId: String) {
        viewModelScope.launch {
            try {
                // Simply close the dialog
                _uiState.value = _uiState.value.copy(
                    showEventDialog = false,
                    activeEvents = emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun dismissEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showEventDialog = false,
                activeEvents = emptyList()
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // DEBUG METHODS
    fun debugCharacter() {
        viewModelScope.launch {
            try {
                playerRepository.getCharacter(CHARACTER_ID).take(1).collect { character ->
                    Log.d(TAG, "=== CHARACTER DEBUG ===")
                    Log.d(TAG, "Character exists: ${character != null}")
                    character?.let {
                        Log.d(TAG, "ID: ${it.id}")
                        Log.d(TAG, "Name: ${it.name}")
                        Log.d(TAG, "Age: ${it.age}")
                        Log.d(TAG, "Money: ${it.money}")
                        Log.d(TAG, "Health: ${it.health}")
                        Log.d(TAG, "Happiness: ${it.happiness}")
                        Log.d(TAG, "Intelligence: ${it.intelligence}")
                        Log.d(TAG, "Social: ${it.social}")
                        Log.d(TAG, "Fitness: ${it.fitness}")
                        Log.d(TAG, "Notoriety: ${it.notoriety}")
                    }
                    Log.d(TAG, "=====================")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Debug error: ${e.message}")
            }
        }
    }

    fun testCreateCharacter() {
        viewModelScope.launch {
            try {
                val testCharacter = com.liveongames.domain.model.Character(
                    id = "test_char",
                    name = "Test Character",
                    age = 25,
                    health = 80,
                    happiness = 60,
                    money = 5000,
                    intelligence = 30,
                    fitness = 15,
                    social = 40,
                    education = 5,
                    career = "Tester",
                    relationships = listOf("friend1", "friend2"),
                    achievements = listOf("test_ach1"),
                    events = listOf("test_event1"),
                    jailTime = 0,
                    notoriety = 10
                )

                playerRepository.createCharacter("test_char", testCharacter)
                Log.d(TAG, "Test character created successfully!")

                // Verify it was saved
                playerRepository.getCharacter("test_char").take(1).collect { saved ->
                    Log.d(TAG, "Saved character money: ${saved?.money}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error creating test character: ${e.message}")
            }
        }
    }

    fun testDirectDatabaseWrite() {
        viewModelScope.launch {
            try {
                // Try to directly update the database
                playerRepository.updateMoney(CHARACTER_ID, 5000)
                Log.d(TAG, "Direct money update completed")

                // Force refresh to see change
                refreshPlayerStats()

            } catch (e: Exception) {
                Log.e(TAG, "Direct database write error: ${e.message}")
            }
        }
    }

    fun showCurrentState() {
        Log.d(TAG, "=== CURRENT UI STATE ===")
        Log.d(TAG, "Player stats: ${_uiState.value.playerStats}")
        Log.d(TAG, "Is loading: ${_uiState.value.isLoading}")
        Log.d(TAG, "Error: ${_uiState.value.error}")
        Log.d(TAG, "========================")
    }
}