package com.liveongames.liveon.model

import com.liveongames.domain.model.EducationLevel

/**
 * Definition of an offerable course loaded from assets/education_courses.json.
 * Separate from a player's enrollment (Education).
 */
data class EducationCourse(
    val id: String,
    val name: String,
    val tier: Int,
    val level: EducationLevel,
    val cost: Int,
    val durationMonths: Int,
    val requiredGpa: Double,
    val prerequisites: List<String>,
    val careerBoosts: List<String>,
    val flavorText: String,
    val schoolCrestResId: String?, // optional, map to drawable if present
    val milestones: List<String> = emptyList()
)
