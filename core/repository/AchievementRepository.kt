package com.liveongames.core.repository

import com.liveongames.core.data.AchievementDao
import com.liveongames.core.model.Achievement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AchievementRepository(private val achievementDao: AchievementDao) {
    suspend fun getAchievement(id: Long): Achievement? = withContext(Dispatchers.IO) {
        achievementDao.getAchievement(id)
    }

    suspend fun getAllAchievements(): List<Achievement> = withContext(Dispatchers.IO) {
        achievementDao.getAllAchievements()
    }

    suspend fun insertAchievement(achievement: Achievement): Long = withContext(Dispatchers.IO) {
        achievementDao.insertAchievement(achievement)
    }

    suspend fun updateAchievement(achievement: Achievement) = withContext(Dispatchers.IO) {
        achievementDao.updateAchievement(achievement)
    }

    suspend fun deleteAchievement(achievement: Achievement) = withContext(Dispatchers.IO) {
        achievementDao.deleteAchievement(achievement)
    }
}
