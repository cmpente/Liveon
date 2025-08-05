// domain/src/main/java/com/liveongames/domain/usecase/ApplyEventUseCase.kt
package com.liveongames.domain.usecase

import com.liveongames.domain.model.CharacterStats
import com.liveongames.domain.model.GameEvent
import javax.inject.Inject

class ApplyEventUseCase @Inject constructor() {
    operator fun invoke(characterStats: CharacterStats, event: GameEvent): CharacterStats {
        // Simple implementation - expand as needed
        return characterStats
    }
}