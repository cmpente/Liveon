package com.liveongames.domain.usecase

import com.liveongames.domain.model.Save
import com.liveongames.domain.repository.SaveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSaveSlotsUseCase @Inject constructor(
    private val saveRepository: SaveRepository
) {
    operator fun invoke(): Flow<List<Save>> {
        return saveRepository.getAllSaves()
    }
}