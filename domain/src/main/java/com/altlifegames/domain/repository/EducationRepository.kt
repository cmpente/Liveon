package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Education
import kotlinx.coroutines.flow.Flow

interface EducationRepository {
    fun getAvailableEducation(): Flow<List<Education>>
    fun getAllEducationOptions(): List<Education>
    suspend fun getEducationById(id: String): Education?
    suspend fun enrollInEducation(education: Education)
    suspend fun completeEducation(educationId: String)
}