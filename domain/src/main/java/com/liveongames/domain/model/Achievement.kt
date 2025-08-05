package com.liveongames.domain.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val achievedDate: Long,
    val points: Int = 0,
    val category: AchievementCategory = AchievementCategory.GENERAL
)

enum class AchievementCategory {
    GENERAL,
    CAREER,
    SOCIAL,
    HEALTH,
    EDUCATION,
    FINANCE
}