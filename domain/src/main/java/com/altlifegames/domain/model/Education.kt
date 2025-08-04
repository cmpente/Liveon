package com.altlifegames.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EducationOption(
    val id: String,
    val name: String,
    val minAge: Int,
    val maxAge: Int,
    val cost: Int,
    val description: String,
    val iconRes: Int,
    val yearsRequired: Int,
    val prerequisites: List<String> = emptyList(),
    val gpaRequirement: Double = 0.0
)