package com.liveongames.data.repository

import com.liveongames.domain.model.Achievement
import com.liveongames.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class AchievementRepositoryImpl @Inject constructor() : AchievementRepository {

    private val achievements = MutableStateFlow<List<Achievement>>(emptyList())
    private val unlockedAchievements = MutableStateFlow<Set<String>>(emptySet())

    override fun getAchievements(): Flow<List<Achievement>> {
        return achievements
    }

    override suspend fun unlockAchievement(achievementId: String) {
        val currentUnlocked = unlockedAchievements.value.toMutableSet()
        currentUnlocked.add(achievementId)
        unlockedAchievements.value = currentUnlocked
    }

    override suspend fun isAchievementUnlocked(achievementId: String): Boolean {
        return unlockedAchievements.value.contains(achievementId)
    }
}