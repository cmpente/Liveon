package com.liveongames.data.repository

import com.liveongames.domain.model.Education
import com.liveongames.domain.repository.EducationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class EducationRepositoryImpl @Inject constructor() : EducationRepository {

    private val educations = MutableStateFlow<List<Education>>(emptyList())
    private val allEducationOptions = mutableListOf<Education>()

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
        // Implementation for enrolling in education
        val currentEducations = educations.value.toMutableList()
        if (!currentEducations.contains(education)) {
            currentEducations.add(education)
            educations.value = currentEducations
        }
    }

    override suspend fun completeEducation(educationId: String) {
        // Implementation for completing education
        val currentEducations = educations.value.toMutableList()
        val index = currentEducations.indexOfFirst { it.id == educationId }
        if (index != -1) {
            currentEducations.removeAt(index)
            educations.value = currentEducations
        }
    }
}