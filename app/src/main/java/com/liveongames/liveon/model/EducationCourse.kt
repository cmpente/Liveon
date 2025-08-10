package com.liveongames.liveon.model

import com.liveongames.domain.model.EducationLevel

data class EducationCourse(
    val id: String,
    val name: String,
    val tier: Int,
    val level: EducationLevel,
    val cost: Int,
    val durationMonths: Int,
    val requiredGpa: Double,
    val prerequisites: List<String> = emptyList(),
    val careerBoosts: List<String> = emptyList(),
    val flavorText: String = "",
    val milestones: List<String> = emptyList(),
    val difficultyMod: Double = 1.0
)
