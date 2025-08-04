package com.altlifegames.data.repository

import com.altlifegames.data.db.dao.CrimeDao
import com.altlifegames.data.db.entity.CrimeEntity
import com.altlifegames.domain.model.CrimeOutcome
import com.altlifegames.domain.model.CrimeRecord
import com.altlifegames.domain.model.CrimeType
import com.altlifegames.domain.repository.CrimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CrimeRepositoryImpl @Inject constructor(private val crimeDao: CrimeDao) : CrimeRepository {
    
    override fun getCrimes(characterId: Long): Flow<List<CrimeRecord>> =
        crimeDao.getCrimesForCharacter(characterId).map { entities ->
            entities.map { entity ->
                CrimeRecord(
                    id = entity.id,
                    crimeType = CrimeType.valueOf(entity.crimeType),
                    severity = entity.severity,
                    outcome = CrimeOutcome.ESCAPED, // This field doesn't exist in entity, so using default
                    sentenceYears = entity.sentenceYears
                )
            }
        }

    override suspend fun recordCrime(characterId: Long, crime: CrimeRecord) {
        val entity = CrimeEntity(
            id = crime.id,
            characterId = characterId,
            crimeType = crime.crimeType.name,
            description = "", // Required field with default value
            severity = crime.severity,
            dateCommitted = System.currentTimeMillis(), // Required field
            isSolved = false, // Required field
            sentenceYears = crime.sentenceYears
        )
        crimeDao.insert(entity)
    }

    override suspend fun clearCriminalRecord(characterId: Long) {
        crimeDao.clearCriminalRecord(characterId)
    }
}