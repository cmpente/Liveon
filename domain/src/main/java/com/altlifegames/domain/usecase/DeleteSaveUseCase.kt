// domain/src/main/java/com/altlifegames/domain/usecase/DeleteSaveUseCase.kt
package com.altlifegames.domain.usecase

import com.altlifegames.domain.repository.SaveRepository
import javax.inject.Inject

class DeleteSaveUseCase @Inject constructor(
    private val repository: SaveRepository
) {
    suspend operator fun invoke(saveSlotId: String) {
        repository.deleteSave(saveSlotId)
    }
}