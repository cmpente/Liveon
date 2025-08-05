// domain/src/main/java/com/liveongames/domain/usecase/DeleteSaveUseCase.kt
package com.liveongames.domain.usecase

import com.liveongames.domain.repository.SaveRepository
import javax.inject.Inject

class DeleteSaveUseCase @Inject constructor(
    private val repository: SaveRepository
) {
    suspend operator fun invoke(saveSlotId: String) {
        repository.deleteSave(saveSlotId)
    }
}