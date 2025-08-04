package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Achievement

interface AchievementRepository {
    suspend fun loadAchievements()
    suspend fun getAchievements(): List<Achievement>
    suspend fun unlockAchievement(achievementId: String): Boolean
    suspend fun isAchievementUnlocked(achievementId: String): Boolean
}