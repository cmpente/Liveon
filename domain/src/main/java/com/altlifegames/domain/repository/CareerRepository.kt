package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Career
import kotlinx.coroutines.flow.Flow

interface CareerRepository {
    fun getAvailableCareers(): Flow<List<Career>>
    suspend fun getCareerById(careerId: String): Career?
    suspend fun getCurrentCareer(): Career?
    suspend fun changeCareer(career: Career)
}