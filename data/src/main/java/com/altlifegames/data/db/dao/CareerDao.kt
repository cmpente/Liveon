package com.altlifegames.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.altlifegames.data.db.entity.CareerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CareerDao {
    @Query("SELECT * FROM careers WHERE characterId = :characterId")
    fun getCareersForCharacter(characterId: Long): Flow<List<CareerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(career: CareerEntity): Long

    @Update
    suspend fun update(career: CareerEntity)

    @Delete
    suspend fun delete(career: CareerEntity)
}