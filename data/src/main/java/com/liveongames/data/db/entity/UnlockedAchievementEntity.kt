package com.liveongames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlocked_achievements")
data class UnlockedAchievementEntity(
    @PrimaryKey val id: String,
    val achievementId: String,
    val unlockedDate: Long
)