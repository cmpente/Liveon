package com.liveongames.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liveongames.data.db.entity.TermStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TermStateDao {
    @Query("SELECT * FROM term_state WHERE characterId = :characterId LIMIT 1")
    fun observe(characterId: String): Flow<TermStateEntity?>

    @Query("SELECT * FROM term_state WHERE characterId = :characterId LIMIT 1")
    suspend fun get(characterId: String): TermStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: TermStateEntity)

    @Query("UPDATE term_state SET focus = :focus, streakDays = :streakDays, professorRelationship = :professorRelationship, weekIndex = :weekIndex, coursePhase = :coursePhase WHERE characterId = :characterId")
    suspend fun update(characterId: String, focus: Int, streakDays: Int, professorRelationship: Int, weekIndex: Int, coursePhase: String)
}
