// domain/src/main/java/com/liveongames/domain/repository/CrimeRepository.kt
package com.liveongames.domain.repository

import com.liveongames.domain.model.CrimeRecordEntry
import com.liveongames.domain.model.CrimeStats
import kotlinx.coroutines.flow.Flow

interface CrimeRepository {
    fun observeStats(): Flow<CrimeStats>

    suspend fun ensureYear(year: Int)
    suspend fun getEarnedThisYear(): Int
    suspend fun setEarnedThisYear(value: Int)
    suspend fun resetEarnedForNewYear(year: Int)

    suspend fun appendRecord(entry: CrimeRecordEntry, maxKeep: Int = 200)
}