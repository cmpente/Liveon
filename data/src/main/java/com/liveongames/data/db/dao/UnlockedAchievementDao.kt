package com.liveongames.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liveongames.data.db.entity.UnlockedAchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockedAchievementDao {
    @Query("SELECT * FROM unlocked_achievements")
    fun getAllUnlockedAchievements(): Flow<List<UnlockedAchievementEntity>>

    @Query("SELECT * FROM unlocked_achievements WHERE achievementId = :achievementId")
    suspend fun isAchievementUnlocked(achievementId: String): UnlockedAchievementEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockAchievement(achievement: UnlockedAchievementEntity)

    @Query("DELETE FROM unlocked_achievements WHERE achievementId = :achievementId")
    suspend fun removeAchievement(achievementId: String)
}