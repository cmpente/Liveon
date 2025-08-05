// app/src/main/java/com/liveongames/data/repository/CrimeRepositoryImpl.kt
package com.liveongames.data.repository

import com.liveongames.data.db.dao.CrimeDao
import com.liveongames.domain.model.Crime
import com.liveongames.domain.repository.CrimeRepository
import com.liveongames.domain.repository.CrimeStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CrimeRepositoryImpl @Inject constructor(
    private val crimeDao: CrimeDao
) : CrimeRepository {

    override fun getCrimes(): Flow<List<Crime>> {
        return crimeDao.getCrimesForCharacter("default_character").map { crimeEntities ->
            crimeEntities.map { crimeEntity ->
                Crime(
                    id = crimeEntity.id,
                    name = crimeEntity.name,
                    description = crimeEntity.description,
                    severity = crimeEntity.severity,
                    chanceOfGettingCaught = crimeEntity.chanceOfGettingCaught,
                    fine = crimeEntity.fine,
                    jailTime = crimeEntity.jailTime
                )
            }
        }
    }

    override suspend fun recordCrime(characterId: String, crime: Crime) {
        val crimeEntity = com.liveongames.data.db.entity.CrimeEntity(
            id = crime.id,
            characterId = characterId,
            name = crime.name,
            description = crime.description,
            severity = crime.severity,
            chanceOfGettingCaught = crime.chanceOfGettingCaught,
            fine = crime.fine,
            jailTime = crime.jailTime
        )
        crimeDao.insertCrime(crimeEntity)
    }

    override suspend fun clearCriminalRecord(characterId: String) {
        crimeDao.clearCrimesForCharacter(characterId)
    }

    override suspend fun getCrimeStats(characterId: String): CrimeStats {
        // Simple implementation - you can expand this
        return CrimeStats()
    }
}