package com.liveongames.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liveongames.data.db.entity.SaveSlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaveSlotDao {
    @Query("SELECT * FROM save_slots ORDER BY lastPlayed DESC")
    fun getSaveSlots(): Flow<List<SaveSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaveSlot(saveSlot: SaveSlotEntity)

    @Query("DELETE FROM save_slots WHERE id = :id")
    suspend fun deleteSaveSlot(id: String)
}