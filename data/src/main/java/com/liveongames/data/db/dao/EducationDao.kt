// app/src/main/java/com/liveongames/data/db/dao/EducationDao.kt
package com.liveongames.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liveongames.data.db.entity.EducationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EducationDao {
    @Query("SELECT * FROM educations WHERE characterId = :characterId ORDER BY id DESC")
    fun getEducationsForCharacter(characterId: String): Flow<List<EducationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEducation(education: EducationEntity)

    @Query("DELETE FROM educations WHERE id = :educationId AND characterId = :characterId")
    suspend fun removeEducation(educationId: String, characterId: String)

    @Query("DELETE FROM educations WHERE characterId = :characterId")
    suspend fun clearEducationsForCharacter(characterId: String)

    @Query("SELECT * FROM educations WHERE characterId = :characterId AND id = :educationId")
    suspend fun getEducationById(characterId: String, educationId: String): EducationEntity?

    @Query("SELECT * FROM educations WHERE characterId = :characterId")
    suspend fun getEducationsForCharacterSync(characterId: String): List<EducationEntity>
}