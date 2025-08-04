package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.Character
import com.altlifegames.domain.model.EventOutcome
import javax.inject.Inject

class CalculateInheritanceUseCase @Inject constructor() {
    // Simplified version
    operator fun invoke(character: Character, deceasedCharacter: Character): Character {
        // Placeholder implementation
        return character
    }
}