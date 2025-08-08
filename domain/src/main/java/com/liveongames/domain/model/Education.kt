// domain/src/main/java/com/liveongames/domain/model/Education.kt
package com.liveongames.domain.model

data class Education(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val duration: Int,
    val minimumAge: Int,
    val minimumGPA: Double = 0.0,
    val skillIncrease: Int,
    val prerequisites: List<String> = emptyList(),
    val enrollmentTimestamp: Long = System.currentTimeMillis()
)