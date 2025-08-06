// app/src/main/java/com/liveongames/liveon/ui/viewmodel/GameViewModel.kt
package com.liveongames.liveon.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.domain.model.CharacterStats
import com.liveongames.domain.model.GameEvent
import com.liveongames.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val removePetUseCase: RemovePetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        loadGame()
    }

    private fun loadGame() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                // Load initial game state
                val initialStats = CharacterStats(
                    health = 100,
                    happiness = 50,
                    intelligence = 20,
                    money = 1000,
                    social = 30,
                    age = 18
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    playerStats = initialStats
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
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