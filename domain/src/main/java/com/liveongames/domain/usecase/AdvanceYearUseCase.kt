// domain/src/main/java/com/liveongames/domain/usecase/AdvanceYearUseCase.kt
package com.liveongames.domain.usecase

import com.liveongames.domain.model.CharacterStats
import javax.inject.Inject

class AdvanceYearUseCase @Inject constructor(
    private val autoProgressEducationUseCase: AutoProgressEducationUseCase
) {
    suspend operator fun invoke(characterStats: CharacterStats): CharacterStats {
        val updatedStats = characterStats.copy(age = characterStats.age + 1)
 autoProgressEducationUseCase.execute(updatedStats.age)
 return updatedStats
    }
}