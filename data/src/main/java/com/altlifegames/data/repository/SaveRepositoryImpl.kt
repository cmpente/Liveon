package com.altlifegames.data.repository

import com.altlifegames.data.db.dao.SaveSlotDao
import com.altlifegames.data.db.entity.SaveSlotEntity
import com.altlifegames.domain.model.Character
import com.altlifegames.domain.model.SaveSlot
import com.altlifegames.domain.repository.SaveRepository
import javax.inject.Inject

class SaveRepositoryImpl @Inject constructor(
    private val saveSlotDao: SaveSlotDao
) : SaveRepository {
    
    override suspend fun saveGame(character: Character) {
        val saveSlot = SaveSlotEntity(
            id = 0,
            timestamp = System.currentTimeMillis()
            // Map character data to entity fields as needed
        )
        saveSlotDao.insert(saveSlot)
    }
    
    override suspend fun loadGame(slotId: Long): Character? {
        val saveSlotEntity = saveSlotDao.getById(slotId)
        // Convert SaveSlotEntity to Character
        return null // Implement actual conversion logic
    }
    
    override suspend fun getSaveSlots(): List<SaveSlot> {
        val entities = saveSlotDao.getAllSlots()
        // Convert List<SaveSlotEntity> to List<SaveSlot>
        return emptyList() // Implement actual conversion logic
    }
    
    override suspend fun deleteSaveSlot(slotId: Long) {
        saveSlotDao.delete(slotId)
    }
}