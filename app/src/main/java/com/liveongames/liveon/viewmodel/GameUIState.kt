// app/src/main/java/com/liveongames/liveon/ui/viewmodel/GameUiState.kt
package com.liveongames.liveon.ui.viewmodel

import com.liveongames.domain.model.CharacterStats
import com.liveongames.domain.model.GameEvent

data class GameUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val playerStats: CharacterStats? = null,
    val showEventDialog: Boolean = false,
    val activeEvents: List<GameEvent> = emptyList()
)