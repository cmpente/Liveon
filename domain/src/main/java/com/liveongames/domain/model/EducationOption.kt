// domain/src/main/java/com/liveongames/domain/model/EducationOption.kt
package com.liveongames.domain.model

data class EducationOption(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val duration: Int,
    val skillIncrease: Int,
    val minimumAge: Int,
    val minimumGPA: Double = 0.0,
    val prerequisites: List<String> = emptyList()
)