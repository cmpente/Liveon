// app/src/main/java/com/altlifegames/altlife/ui/viewmodel/GameUiState.kt
package com.altlifegames.altlife.ui.viewmodel

import com.altlifegames.domain.model.CharacterStats
import com.altlifegames.domain.model.GameEvent

data class GameUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val playerStats: CharacterStats? = null,
    val showEventDialog: Boolean = false,
    val activeEvents: List<GameEvent> = emptyList()
)