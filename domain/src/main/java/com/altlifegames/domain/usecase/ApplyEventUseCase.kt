// domain/src/main/java/com/altlifegames/domain/usecase/ApplyEventUseCase.kt
package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.CharacterStats
import com.altlifegames.domain.model.GameEvent
import javax.inject.Inject

class ApplyEventUseCase @Inject constructor() {
    operator fun invoke(characterStats: CharacterStats, event: GameEvent): CharacterStats {
        // Simple implementation - expand as needed
        return characterStats
    }
}