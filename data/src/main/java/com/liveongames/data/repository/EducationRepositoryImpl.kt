// data/src/main/java/com/liveongames/data/repository/EducationRepositoryImpl.kt
package com.liveongames.data.repository

import com.liveongames.domain.model.Education
import com.liveongames.domain.repository.EducationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class EducationRepositoryImpl @Inject constructor() : EducationRepository {

    private val educations = MutableStateFlow<List<Education>>(emptyList())
    private val allEducationOptions = getEducationOptions()

    override fun getAvailableEducation(): Flow<List<Education>> {
        return educations
    }

    override fun getAllEducationOptions(): List<Education> {
        return allEducationOptions
    }

    override suspend fun getEducationById(id: String): Education? {
        return allEducationOptions.find { it.id == id }
    }

    override suspend fun enrollInEducation(education: Education) {
        val currentEducations = educations.value.toMutableList()
        if (!currentEducations.contains(education)) {
            currentEducations.add(education.copy(enrollmentTimestamp = System.currentTimeMillis()))
            educations.value = currentEducations
        }
    }

    override suspend fun completeEducation(educationId: String) {
        val currentEducations = educations.value.toMutableList()
        val index = currentEducations.indexOfFirst { it.id == educationId }
        if (index != -1) {
            currentEducations.removeAt(index)
            educations.value = currentEducations
        }
    }

    private fun getEducationOptions(): List<Education> {
        return listOf(
            Education(
                id = "grade_school",
                name = "Grade School",
                description = "Basic education for young students",
                cost = 0,
                duration = 6,
                minimumAge = 6,
                skillIncrease = 1,
                minimumGPA = 0.0
            ),
            Education(
                id = "high_school",
                name = "High School",
                description = "Secondary education preparing for college",
                cost = 0,
                duration = 4,
                minimumAge = 14,
                skillIncrease = 2,
                minimumGPA = 2.0,
                prerequisites = listOf("grade_school")
            ),
            Education(
                id = "university",
                name = "University",
                description = "Higher education for career advancement",
                cost = 20000,
                duration = 4,
                minimumAge = 18,
                skillIncrease = 5,
                minimumGPA = 2.5,
                prerequisites = listOf("high_school")
            )
            // Add more education options as needed
        )
    }
}