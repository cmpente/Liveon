package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.Save
import com.altlifegames.domain.repository.SaveRepository
import javax.inject.Inject

class LoadGameUseCase @Inject constructor(
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(saveId: String): Save? {
        return saveRepository.loadSave(saveId)
    }
}