package com.liveongames.domain.repository

import com.liveongames.domain.model.Career
import kotlinx.coroutines.flow.Flow

interface CareerRepository {
    fun getAvailableCareers(): Flow<List<Career>>
    suspend fun getCareerById(careerId: String): Career?
    suspend fun getCurrentCareer(): Career?
    suspend fun changeCareer(career: Career)
}