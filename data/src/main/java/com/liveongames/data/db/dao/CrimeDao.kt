// app/src/main/java/com/liveongames/data/db/dao/CrimeDao.kt
package com.liveongames.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liveongames.data.db.entity.CrimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CrimeDao {
    @Query("SELECT * FROM crimes WHERE characterId = :characterId ORDER BY id DESC")
    fun getCrimesForCharacter(characterId: String): Flow<List<CrimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrime(crime: CrimeEntity)

    @Query("DELETE FROM crimes WHERE characterId = :characterId")
    suspend fun clearCrimesForCharacter(characterId: String)

    @Query("SELECT * FROM crimes WHERE characterId = :characterId")
    suspend fun getCrimesForCharacterSync(characterId: String): List<CrimeEntity>
}