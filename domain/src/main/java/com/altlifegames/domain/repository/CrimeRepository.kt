package com.altlifegames.domain.repository

import com.altlifegames.domain.model.CrimeRecord
import kotlinx.coroutines.flow.Flow

interface CrimeRepository {
    fun getCrimes(characterId: Long): Flow<List<CrimeRecord>>
    suspend fun recordCrime(characterId: Long, crime: CrimeRecord)
    suspend fun clearCriminalRecord(characterId: Long)
}