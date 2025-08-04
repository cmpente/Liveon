package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.Character  // Use domain model
import com.altlifegames.domain.repository.SaveRepository
import javax.inject.Inject

class LoadGameUseCase @Inject constructor(
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(slotId: Long): Character? {
        return saveRepository.loadGame(slotId)
    }
}