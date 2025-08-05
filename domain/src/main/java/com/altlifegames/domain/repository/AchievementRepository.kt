package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Achievement
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun getAchievements(): Flow<List<Achievement>>
    suspend fun unlockAchievement(achievementId: String)
    suspend fun isAchievementUnlocked(achievementId: String): Boolean
}