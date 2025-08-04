package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.SaveSlot
import com.altlifegames.domain.repository.SaveRepository
import javax.inject.Inject
import com.altlifegames.domain.model.Character
import com.altlifegames.domain.model.EventOutcome

/**
 * Retrieves all available save slots for the player to choose from.
 */
class GetSaveSlotsUseCase @Inject constructor(private val repository: SaveRepository) {
    suspend operator fun invoke(): List<SaveSlot> {
        return repository.getSaveSlots()
    }
}