// domain/src/main/java/com/liveongames/domain/usecase/AdvanceYearUseCase.kt
package com.liveongames.domain.usecase

import com.liveongames.domain.model.CharacterStats
import javax.inject.Inject

class AdvanceYearUseCase @Inject constructor() {
    operator fun invoke(characterStats: CharacterStats): CharacterStats {
        return characterStats.copy(age = characterStats.age + 1)
    }
}