package com.liveongames.domain.model

data class EducationOption(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val duration: Int,
    val skillIncrease: Int,
    val minimumAge: Int
)