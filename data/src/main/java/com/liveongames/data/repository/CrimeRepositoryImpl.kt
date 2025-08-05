package com.liveongames.data.repository

import com.liveongames.domain.model.Crime
import com.liveongames.domain.repository.CrimeRepository
import com.liveongames.domain.repository.CrimeStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

// Create a data class to track crimes per character
data class CharacterCrimeRecord(
    val characterId: String,
    val crime: Crime,
    val timestamp: Long = System.currentTimeMillis()
)

class CrimeRepositoryImpl @Inject constructor() : CrimeRepository {

    private val crimeRecords = MutableStateFlow<List<CharacterCrimeRecord>>(emptyList())

    override fun getCrimes(): Flow<List<Crime>> {
        // Return all crimes, not grouped by character
        return MutableStateFlow(crimeRecords.value.map { it.crime })
    }

    override suspend fun recordCrime(characterId: String, crime: Crime) {
        val currentRecords = crimeRecords.value.toMutableList()
        currentRecords.add(CharacterCrimeRecord(characterId, crime))
        crimeRecords.value = currentRecords
    }

    override suspend fun clearCriminalRecord(characterId: String) {
        val currentRecords = crimeRecords.value.toMutableList()
        val filteredRecords = currentRecords.filter { it.characterId != characterId }
        crimeRecords.value = filteredRecords
    }

    override suspend fun getCrimeStats(characterId: String): CrimeStats {
        val characterCrimes = crimeRecords.value.filter { it.characterId == characterId }

        // Calculate severity distribution
        val severityMap = characterCrimes
            .groupBy { it.crime.severity }
            .mapValues { it.value.size }

        // Find last crime timestamp
        val lastCrimeTimestamp = characterCrimes
            .maxOfOrNull { it.timestamp }

        return CrimeStats(
            totalCrimes = characterCrimes.size,
            crimesBySeverity = severityMap,
            lastCrimeDate = lastCrimeTimestamp
        )
    }
}