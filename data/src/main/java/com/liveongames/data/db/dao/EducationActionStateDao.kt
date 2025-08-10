package com.liveongames.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liveongames.data.db.entity.EducationActionStateEntity

@Dao
interface EducationActionStateDao {

    @Query("SELECT * FROM education_action_state WHERE characterId = :characterId AND educationId = :educationId AND actionId = :actionId LIMIT 1")
    suspend fun get(characterId: String, educationId: String, actionId: String): EducationActionStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: EducationActionStateEntity)

    @Query("UPDATE education_action_state SET lastUsedAt = :lastUsedAt, usedThisAge = :usedThisAge WHERE characterId = :characterId AND educationId = :educationId AND actionId = :actionId")
    suspend fun update(characterId: String, educationId: String, actionId: String, lastUsedAt: Long, usedThisAge: Int)

    @Query("UPDATE education_action_state SET usedThisAge = 0 WHERE characterId = :characterId AND educationId = :educationId")
    suspend fun resetUsedThisAge(characterId: String, educationId: String)

    @Query("DELETE FROM education_action_state WHERE characterId = :characterId AND educationId = :educationId")
    suspend fun clearForEducation(characterId: String, educationId: String)
}
