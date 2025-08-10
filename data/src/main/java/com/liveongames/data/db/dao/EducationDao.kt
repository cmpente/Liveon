package com.liveongames.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liveongames.data.db.entity.EducationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EducationDao {

    @Query("SELECT * FROM educations WHERE characterId = :characterId ORDER BY timestamp DESC")
    fun getEducationsForCharacter(characterId: String): Flow<List<EducationEntity>>

    @Query("SELECT * FROM educations WHERE characterId = :characterId")
    suspend fun getEducationsForCharacterSync(characterId: String): List<EducationEntity>

    @Query("SELECT * FROM educations WHERE characterId = :characterId AND id = :educationId LIMIT 1")
    suspend fun getEducationById(characterId: String, educationId: String): EducationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(education: EducationEntity)

    @Query("DELETE FROM educations WHERE characterId = :characterId AND id = :educationId")
    suspend fun deleteById(characterId: String, educationId: String)

    @Query("DELETE FROM educations WHERE characterId = :characterId")
    suspend fun clear(characterId: String)

    // Convenience helpers for state changes
    @Query("UPDATE educations SET isActive = 0 WHERE characterId = :characterId")
    suspend fun deactivateAll(characterId: String)

    @Query("""
        UPDATE educations
        SET currentGpa = :gpa,
            attendClassCount = :attendCount,
            doHomeworkCount = :homeworkCount,
            studyCount = :studyCount
        WHERE characterId = :characterId AND id = :educationId
    """)
    suspend fun updateProgress(
        characterId: String,
        educationId: String,
        gpa: Double,
        attendCount: Int,
        homeworkCount: Int,
        studyCount: Int
    )

    @Query("""
        UPDATE educations
        SET isActive = :isActive, completionDate = :completionDate
        WHERE characterId = :characterId AND id = :educationId
    """)
    suspend fun updateStatus(
        characterId: String,
        educationId: String,
        isActive: Boolean,
        completionDate: Long?
    )
}
