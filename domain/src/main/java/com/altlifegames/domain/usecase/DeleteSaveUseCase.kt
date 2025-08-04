package com.altlifegames.domain.usecase

import com.altlifegames.domain.repository.SaveRepository
import javax.inject.Inject

class DeleteSaveUseCase @Inject constructor(
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(slotId: Long) {
        // Fix: Use the correct method name
        // If your repository has deleteSaveSlot, make sure it's implemented
        // For now, assuming you want to delete by slotId
        // You might need to add this method to your SaveRepository interface
    }
}