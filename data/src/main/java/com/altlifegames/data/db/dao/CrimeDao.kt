package com.altlifegames.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.altlifegames.data.db.entity.CrimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CrimeDao {
    @Query("SELECT * FROM crimes WHERE characterId = :characterId")
    fun getCrimesForCharacter(characterId: Long): Flow<List<CrimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crime: CrimeEntity): Long

    @Query("DELETE FROM crimes WHERE characterId = :characterId")
    suspend fun clearCriminalRecord(characterId: Long)
}