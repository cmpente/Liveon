package com.liveongames.liveon.model

import com.liveongames.domain.model.EducationLevel

data class EducationActionDef(
    val id: String,
    val name: String,
    val iconRes: Int = 0,
    val baseDelta: Double,
    val cooldownSeconds: Int,
    val capPerAge: Int,
    val minGpa: Double = 0.0,
    val allowedLevels: List<EducationLevel> = emptyList(),
    val requiresMilestonesAny: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val minigame: MiniGameSpec? = null,
    val costs: Costs? = null,
    val recovery: Recovery? = null,
    val availability: Availability? = null,
    val critChance: Double = 0.0,
    val critMultiplier: Double = 1.0
) {
    data class MiniGameSpec(
        val type: MiniGameType,
        val difficulty: Int = 1,
        val tiers: List<Tier> = emptyList()
    )
    enum class MiniGameType { TIMING, MEMORY, QUIZ, DRAG }
    data class Tier(val label: String, val multiplier: Double, val threshold: Int)
    data class Costs(val money: Int? = null, val energy: Int? = null)
    data class Recovery(val fatigue: Int? = null)
    data class Availability(val phase: CoursePhase? = null)
    enum class CoursePhase { EARLY, MID, LATE }
}
