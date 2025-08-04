package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Character
import com.altlifegames.domain.model.SaveSlot

interface SaveRepository {
    suspend fun saveGame(character: Character)
    suspend fun loadGame(slotId: Long): Character?
    suspend fun getSaveSlots(): List<SaveSlot>
    suspend fun deleteSaveSlot(slotId: Long)
}