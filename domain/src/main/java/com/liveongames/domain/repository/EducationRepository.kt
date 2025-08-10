// domain/src/main/java/com/liveongames/domain/repository/EducationRepository.kt
package com.liveongames.domain.repository

import com.liveongames.domain.model.Education
import kotlinx.coroutines.flow.Flow

interface EducationRepository {
    fun getEducations(): Flow<List<Education>>
    suspend fun getEducationById(educationId: String): Education?
    suspend fun addEducation(education: Education)
    suspend fun updateEducation(education: Education)
    suspend fun removeEducation(educationId: String)
    suspend fun clearEducations()
}