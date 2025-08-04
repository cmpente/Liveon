// app/src/main/java/com/altlifegames/altlife/model/Club.kt
package com.altlifegames.altlife.model

data class Club(
    val id: String,
    val name: String,
    val description: String,
    val iconRes: Int,
    val category: ClubCategory,
    val gpaRequirement: Double = 0.0,
    val skillRequirement: String? = null, // e.g., "Athletics", "Intelligence"
    val skillLevel: Int = 0, // 1-10
    val tryoutDifficulty: TryoutDifficulty = TryoutDifficulty.MEDIUM
)

enum class ClubCategory {
    SPORTS, 
    ACADEMIC, 
    CREATIVE, 
    VOLUNTEER, 
    LEADERSHIP
}

enum class TryoutDifficulty {
    EASY, 
    MEDIUM, 
    HARD
}