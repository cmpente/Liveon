package com.liveongames.domain.usecase

import com.liveongames.domain.model.Character
import com.liveongames.domain.repository.CharacterRepository
import javax.inject.Inject

class UpdateCharacterUseCase @Inject constructor(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(character: Character) {
        characterRepository.updateCharacter(character)
    }
}