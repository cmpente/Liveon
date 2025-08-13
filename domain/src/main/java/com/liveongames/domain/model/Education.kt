// domain/model/Education.kt (Potentially with deprecation)
package com.liveongames.domain.model

@Deprecated("Use Enrollment and EducationProgram for educational actions logic.")
data class Education(
    val id: String,
    val name: String,
    val description: String,
    val level: EducationLevel, // Legacy enum
    val cost: Int,
    val duration: Int,         // months
    val requiredGPA: Double,   // Static GPA requirement from catalog/profiles, not dynamic GPA
    val currentGPA: Double = 0.0,
    val isActive: Boolean = false,
    val timestamp: Long = 0L,
    val completionDate: Long? = null,
    val attendClassCount: Int = 0,
    val doHomeworkCount: Int = 0,
    val studyCount: Int = 0
)