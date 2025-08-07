// app/src/main/java/com/liveongames/data/repository/CrimeRepositoryImpl.kt
package com.liveongames.data.repository

import android.util.Log
import com.liveongames.data.db.dao.CrimeDao
import com.liveongames.data.db.entity.toCrime
import com.liveongames.domain.model.Crime
import com.liveongames.domain.repository.CrimeRepository
import com.liveongames.domain.repository.CrimeStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CrimeRepositoryImpl @Inject constructor(
    private val crimeDao: CrimeDao
) : CrimeRepository {

    companion object {
        private const val TAG = "CrimeRepository"
        private const val CHARACTER_ID = "player_character"
    }

    override fun getCrimes(): Flow<List<Crime>> {
        Log.d(TAG, "getCrimes called for character: $CHARACTER_ID")
        return crimeDao.getCrimesForCharacter(CHARACTER_ID).map { crimeEntities ->
            Log.d(TAG, "DB returned ${crimeEntities.size} crime entities for $CHARACTER_ID")
            crimeEntities.map { it.toCrime() }.also { crimes ->
                Log.d(TAG, "Mapped to ${crimes.size} crime models")
            }
        }
    }

    override suspend fun recordCrime(characterId: String, crime: Crime) {
        Log.d(TAG, "recordCrime called for character: $characterId, crime: ${crime.name}")
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
        Log.d(TAG, "Inserting crime entity: ${crimeEntity.id} for character: $characterId")
        crimeDao.insertCrime(crimeEntity)
        Log.d(TAG, "Crime inserted successfully")
    }

    override suspend fun clearCriminalRecord(characterId: String) {
        Log.d(TAG, "clearCriminalRecord called for character: $characterId")
        crimeDao.clearCrimesForCharacter(characterId)
        Log.d(TAG, "Criminal record cleared for character: $characterId")
    }

    override suspend fun getCrimeStats(characterId: String): CrimeStats {
        Log.d(TAG, "getCrimeStats called for character: $characterId")
        return CrimeStats()
    }
}