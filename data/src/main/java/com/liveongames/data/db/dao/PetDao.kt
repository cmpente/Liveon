// app/src/main/java/com/liveongames/data/db/dao/PetDao.kt
package com.liveongames.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liveongames.data.db.entity.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets WHERE characterId = :characterId")
    fun getPetsForCharacter(characterId: String): Flow<List<PetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: PetEntity)

    @Query("DELETE FROM pets WHERE id = :petId")
    suspend fun removePet(petId: String)
}