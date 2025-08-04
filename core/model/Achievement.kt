package com.altlifegames.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: Long,
    val name: String,
    val description: String,
    val type: AchievementType,
    val icon: String,
    val isUnlocked: Boolean = false
)

enum class AchievementType {
    CAREER, RARE_EVENT, RELATIONSHIP, LIFESPAN, WEALTH, CUSTOM
}
