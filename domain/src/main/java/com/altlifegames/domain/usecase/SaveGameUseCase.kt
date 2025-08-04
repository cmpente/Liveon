package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.Character
import com.altlifegames.domain.repository.SaveRepository
import javax.inject.Inject

class SaveGameUseCase @Inject constructor(
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(character: Character) {
        // Fix: Pass only the character parameter
        saveRepository.saveGame(character)
    }
}