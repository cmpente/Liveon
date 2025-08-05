package com.liveongames.domain.usecase

import com.liveongames.domain.model.Save
import com.liveongames.domain.repository.SaveRepository
import javax.inject.Inject

class LoadGameUseCase @Inject constructor(
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(saveId: String): Save? {
        return saveRepository.loadSave(saveId)
    }
}