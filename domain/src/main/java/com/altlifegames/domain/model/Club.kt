package com.altlifegames.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Club(
    val id: String,
    val name: String,
    val description: String,
    val iconRes: Int,
    val category: ClubCategory,
    val gpaRequirement: Double = 0.0,
    val skillRequirement: String? = null,
    val skillLevel: Int = 0,
    val tryoutDifficulty: TryoutDifficulty = TryoutDifficulty.EASY
)

enum class ClubCategory {
    SPORTS, ACADEMIC, CREATIVE, VOLUNTEER, LEADERSHIP
}

enum class TryoutDifficulty {
    EASY, MEDIUM, HARD
}