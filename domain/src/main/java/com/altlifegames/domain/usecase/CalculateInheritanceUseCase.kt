// domain/src/main/java/com/altlifegames/domain/usecase/CalculateInheritanceUseCase.kt
package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.Character
import javax.inject.Inject

class CalculateInheritanceUseCase @Inject constructor() {
    operator fun invoke(character: Character, estateValue: Int): Int {
        // Simple calculation - you can make this more complex
        return (estateValue * 0.5).toInt()
    }
}