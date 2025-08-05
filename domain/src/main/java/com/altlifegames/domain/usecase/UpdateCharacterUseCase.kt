package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.Character
import com.altlifegames.domain.repository.CharacterRepository
import javax.inject.Inject

class UpdateCharacterUseCase @Inject constructor(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(character: Character) {
        characterRepository.updateCharacter(character)
    }
}