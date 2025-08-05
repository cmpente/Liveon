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
                val currentStats = _uiState.value.playerStats
                if (currentStats != null) {
                    // Get available events - using try-catch for now since repos aren't implemented
                    val events = try {
                         getAvailableEventsUseCase()
                        emptyList<GameEvent>()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    _uiState.value = _uiState.value.copy(
                        playerStats = advanceYearUseCase(currentStats), // This returns CharacterStats
                        showEventDialog = events.isNotEmpty(),
                        activeEvents = events.ifEmpty {
                            listOf(
                                GameEvent(
                                    id = "default_event",
                                    title = "Another Year Passes",
                                    description = "Time flies! You are now ${currentStats.age + 1} years old.",
                                    choices = emptyList()
                                )
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun makeChoice(eventId: String, choiceId: String) {
        viewModelScope.launch {
            try {
                // Apply choice outcomes using your use case
                // This would involve finding the event, choice, and applying outcomes
                _uiState.value = _uiState.value.copy(showEventDialog = false)
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
}

fun saveGame() {
    // Placeholder for future save functionality
    // TODO: Implement actual save logic
}
