// domain/src/main/java/com/liveongames/domain/model/Education.kt
package com.liveongames.domain.model


data class Education(
    val id: String,
    val name: String,
    val description: String,
    val level: EducationLevel,
    val cost: Int,
    val duration: Int,
    val requiredGPA: Double,
    val currentGPA: Double = 0.0,
    val isActive: Boolean = false,
    val timestamp: Long = 0L,
    val completionDate: Long? = null,
    val attendClassCount: Int = 0,
    val doHomeworkCount: Int = 0,
    val studyCount: Int = 0
)

enum class EducationLevel(val displayName: String) {
    BASIC("Basic Education"),
    HIGH_SCHOOL("High School"),
    ASSOCIATE("Associate Degree"),
    BACHELOR("Bachelor's Degree"),
    MASTER("Master's Degree"),
    CERTIFICATION("Certification")
}