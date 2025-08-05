package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.Save
import com.altlifegames.domain.repository.SaveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSaveSlotsUseCase @Inject constructor(
    private val saveRepository: SaveRepository
) {
    operator fun invoke(): Flow<List<Save>> {
        return saveRepository.getAllSaves()
    }
}