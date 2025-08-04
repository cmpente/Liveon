package com.altlifegames.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.altlifegames.data.db.entity.SaveSlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaveSlotDao {
    @Query("SELECT * FROM save_slots")
    suspend fun getAllSlots(): List<SaveSlotEntity>
    
    @Query("SELECT * FROM save_slots WHERE id = :id")
    suspend fun getById(id: Long): SaveSlotEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(saveSlot: SaveSlotEntity)
    
    @Query("DELETE FROM save_slots WHERE id = :id")
    suspend fun delete(id: Long)
}