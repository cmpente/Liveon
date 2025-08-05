// domain/src/main/java/com/liveongames/domain/repository/CrimeRepository.kt
package com.liveongames.domain.repository

import com.liveongames.domain.model.Crime
import kotlinx.coroutines.flow.Flow

// Add CrimeStats data class
data class CrimeStats(
    val totalCrimes: Int = 0,
    val crimesBySeverity: Map<Int, Int> = emptyMap(),
    val lastCrimeDate: Long? = null
)

interface CrimeRepository {
    fun getCrimes(): Flow<List<Crime>>
    suspend fun recordCrime(characterId: String, crime: Crime)
    suspend fun clearCriminalRecord(characterId: String)
    suspend fun getCrimeStats(characterId: String): CrimeStats
}