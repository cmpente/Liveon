package com.liveongames.domain.model

data class Career(
    val id: String,
    val name: String,
    val description: String,
    val salary: Int,
    val requiredEducation: String? = null,
    val requiredAge: Int = 0,
    val growthRate: Int = 1
)