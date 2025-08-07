// app/src/main/java/com/liveongames/liveon/ui/viewmodel/GameViewModel.kt
package com.liveongames.liveon.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.domain.model.CharacterStats
import com.liveongames.domain.model.GameEvent
import com.liveongames.domain.repository.PlayerRepository
import com.liveongames.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val advanceYearUseCase: AdvanceYearUseCase,
    private val getAvailableEventsUseCase: GetAvailableEventsUseCase,
    private val applyChoiceOutcomesUseCase: ApplyChoiceOutcomesUseCase,
    private val updateStatsUseCase: UpdateStatsUseCase,
    private val loadGameUseCase: LoadGameUseCase,
    private val saveGameUseCase: SaveGameUseCase,
    private val getSaveSlotsUseCase: GetSaveSlotsUseCase,
    private val deleteSaveUseCase: DeleteSaveUseCase,
    private val getMatureContentStatusUseCase: GetMatureContentStatusUseCase,
    private val toggleMatureContentUseCase: ToggleMatureContentUseCase,
    private val getCrimeStatsUseCase: GetCrimeStatsUseCase,
    private val getCrimesUseCase: GetCrimesUseCase,
    private val recordCrimeUseCase: RecordCrimeUseCase,
    private val clearCriminalRecordUseCase: ClearCriminalRecordUseCase,
    private val adoptPetUseCase: AdoptPetUseCase,
    private val removePetUseCase: RemovePetUseCase,
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

                // Load actual character data
                val character = playerRepository.getCharacter(CHARACTER_ID).first()

                val initialStats = if (character != null) {
                    CharacterStats(
                        health = character.health,
                        happiness = character.happiness,
                        intelligence = character.intelligence,
                        money = character.money,
                        social = character.social,
                        age = character.age
                    )
                } else {
                    CharacterStats(
                        health = 100,
                        happiness = 50,
                        intelligence = 20,
                        money = 1000,
                        social = 30,
                        age = 18
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    playerStats = initialStats
                )
                Log.d(TAG, "Game loaded with money: ${initialStats.money}")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading game", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun refreshPlayerStats() {
        Log.d(TAG, "refreshPlayerStats CALLED")
        viewModelScope.launch {
            try {
                Log.d(TAG, "refreshPlayerStats: About to fetch from repository")
                // Fetch fresh character data from repository
                val character = playerRepository.getCharacter("player_character").first()
                Log.d(TAG, "refreshPlayerStats: Repository returned character with money: ${character?.money}")

                character?.let { char ->
                    val currentStats = _uiState.value.playerStats
                    if (currentStats != null) {
                        Log.d(TAG, "refreshPlayerStats: Current UI state money: ${currentStats.money}")
                        Log.d(TAG, "refreshPlayerStats: New money from DB: ${char.money}")

                        // FORCE UPDATE by creating completely new object
                        val updatedStats = currentStats.copy(
                            money = char.money,
                            health = char.health,
                            happiness = char.happiness,
                            intelligence = char.intelligence,
                            social = char.social,
                            age = char.age
                        )

                        // Force state update with a small delay to ensure detection
                        _uiState.value = _uiState.value.copy(
                            playerStats = updatedStats
                        )

                        Log.d(TAG, "refreshPlayerStats: State updated, new money in UI: ${char.money}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in refreshPlayerStats", e)
            }
        }
    }

    // Helper methods for debugging
    fun getCurrentMoney(): Int {
        return _uiState.value.playerStats?.money ?: 0
    }

    fun debugState() {
        Log.d(TAG, "GameViewModel DEBUG - Current money: ${getCurrentMoney()}")
    }

    // Alternative refresh method that specifically updates money
    fun updateMoneyFromRepository() {
        Log.d(TAG, "Updating money from repository...")
        viewModelScope.launch {
            try {
                val character = playerRepository.getCharacter(CHARACTER_ID).first()
                character?.let { char ->
                    val currentStats = _uiState.value.playerStats
                    if (currentStats != null) {
                        val updatedStats = currentStats.copy(money = char.money)
                        _uiState.value = _uiState.value.copy(playerStats = updatedStats)
                        Log.d(TAG, "Money updated in UI to: ${char.money}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating money", e)
            }
        }
    }

    fun ageUp() {
        viewModelScope.launch {
            try {
                val currentStats = _uiState.value.playerStats ?: return@launch

                // Advance year using use case
                val newStats = advanceYearUseCase(currentStats)

                // Get available events - using try-catch for now since repos aren't implemented
                val events: List<GameEvent> = try {
                    getAvailableEventsUseCase() as List<GameEvent>
                } catch (e: Exception) {
                    emptyList<GameEvent>()
                }

                _uiState.value = _uiState.value.copy(
                    playerStats = newStats,
                    showEventDialog = events.isNotEmpty(),
                    activeEvents = if (events.isNotEmpty()) {
                        events
                    } else {
                        listOf(
                            GameEvent(
                                id = "default_event",
                                title = "Another Year Passes",
                                description = "Time flies! You are now ${newStats.age} years old.",
                                choices = emptyList()
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun makeChoice(eventId: String, choiceId: String) {
        viewModelScope.launch {
            try {
                // Find the event and choice to get stat changes
                val event = _uiState.value.activeEvents.find { it.id == eventId }
                if (event != null) {
                    val choice = event.choices.find { it.id == choiceId }
                    if (choice != null && choice.outcomes.isNotEmpty()) {
                        // Apply stat changes from the choice outcome
                        val outcome = choice.outcomes[0] // Get first outcome
                        val statChanges = outcome.statChanges

                        // Update stats based on outcome
                        val currentStats = _uiState.value.playerStats ?: return@launch

                        // Create the changes map for updateStatsUseCase
                        val updatedStats = updateStatsUseCase(currentStats, statChanges)

                        _uiState.value = _uiState.value.copy(
                            playerStats = updatedStats,
                            showEventDialog = false,
                            activeEvents = emptyList()
                        )
                    }
                }
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

    fun saveGame() {
        viewModelScope.launch {
            try {
                // TODO: Implement actual save logic with saveGameUseCase
                // saveGameUseCase(_uiState.value)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}