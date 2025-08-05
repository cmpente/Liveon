// domain/src/main/java/com/altlifegames/domain/usecase/AdvanceYearUseCase.kt
package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.CharacterStats
import javax.inject.Inject

class AdvanceYearUseCase @Inject constructor() {
    operator fun invoke(characterStats: CharacterStats): CharacterStats {
        return characterStats.copy(age = characterStats.age + 1)
    }
}