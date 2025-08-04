package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Career
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing careers.  Provides lists of job openings, applies to jobs, and handles promotions and demotions.
 */
interface CareerRepository {
    fun getAvailableCareers(): Flow<List<Career>>
    suspend fun applyForCareer(characterId: Long, career: Career): Boolean
    suspend fun promote(characterId: Long)
    suspend fun demote(characterId: Long)
    fun getCurrentCareer(characterId: Long): Flow<Career?>
}