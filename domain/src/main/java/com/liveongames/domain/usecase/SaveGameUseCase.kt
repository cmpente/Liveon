package com.liveongames.domain.usecase

import com.liveongames.domain.model.Save
import com.liveongames.domain.repository.SaveRepository
import javax.inject.Inject

class SaveGameUseCase @Inject constructor(
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(save: Save) {
        saveRepository.createSave(save)
    }
}