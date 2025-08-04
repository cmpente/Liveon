package com.altlifegames.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.altlifegames.data.db.entity.UnlockedAchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockedAchievementDao {
    @Query("SELECT * FROM unlocked_achievements WHERE characterId = :characterId")
    fun getUnlockedAchievementsForCharacter(characterId: Long): Flow<List<UnlockedAchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(achievement: UnlockedAchievementEntity): Long

    @Query("DELETE FROM unlocked_achievements WHERE characterId = :characterId AND achievementId = :achievementId")
    suspend fun deleteByAchievementId(characterId: Long, achievementId: String)
}