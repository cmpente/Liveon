package com.liveongames.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liveongames.data.db.entity.CrimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CrimeDao {
    @Query("SELECT * FROM crimes WHERE characterId = :characterId")
    fun getCrimesForCharacter(characterId: String): Flow<List<CrimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrime(crime: CrimeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrimes(crimes: List<CrimeEntity>)

    @Query("DELETE FROM crimes WHERE characterId = :characterId")
    suspend fun clearCrimesForCharacter(characterId: String)
}