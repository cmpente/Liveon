package com.altlifegames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for achievements unlocked by a character.  Achievements themselves are defined in JSON assets and loaded at runtime.
 */
@Entity(tableName = "unlocked_achievements")
data class UnlockedAchievementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val characterId: Long,
    val achievementId: String
)