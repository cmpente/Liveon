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
        private const val PLAYER_CHARACTER_ID = "player_character"
    }

    override fun getCrimes(): Flow<List<Crime>> {
        Log.d(TAG, "getCrimes called for character: $PLAYER_CHARACTER_ID")
        return crimeDao.getCrimesForCharacter(PLAYER_CHARACTER_ID).map { crimeEntities ->
            Log.d(TAG, "DB returned ${crimeEntities.size} crime entities for $PLAYER_CHARACTER_ID")
            crimeEntities.map { it.toCrime() }.also { crimes ->
                Log.d(TAG, "Mapped to ${crimes.size} crime models")
            }
        }
    }

    override suspend fun recordCrime(characterId: String, crime: Crime) {
        Log.d(TAG, "recordCrime called for character: $characterId, crime: ${crime.name}")
        val crimeEntity = com.liveongames.data.db.entity.CrimeEntity(
            id = crime.id,
            characterId = PLAYER_CHARACTER_ID,  // Use consistent character ID
            name = crime.name,
            description = crime.description,
            riskTier = crime.riskTier.name,
            notorietyRequired = crime.notorietyRequired,
            baseSuccessChance = crime.baseSuccessChance,
            payoutMin = crime.payoutMin,
            payoutMax = crime.payoutMax,
            jailMin = crime.jailMin,
            jailMax = crime.jailMax,
            notorietyGain = crime.notorietyGain,
            notorietyLoss = crime.notorietyLoss,
            iconDescription = crime.iconDescription,
            scenario = crime.scenario,
            success = crime.success,
            caught = crime.caught,
            moneyGained = crime.moneyGained,
            actualJailTime = crime.actualJailTime,
            timestamp = crime.timestamp
        )
        Log.d(TAG, "Inserting crime entity: ${crimeEntity.id} for character: $PLAYER_CHARACTER_ID")
        crimeDao.insertCrime(crimeEntity)
        Log.d(TAG, "Crime inserted successfully")
    }

    override suspend fun clearCriminalRecord(characterId: String) {
        Log.d(TAG, "clearCriminalRecord called for character: $characterId")
        crimeDao.clearCrimesForCharacter(PLAYER_CHARACTER_ID)
        Log.d(TAG, "Criminal record cleared for character: $characterId")
    }

    override suspend fun getCrimeStats(characterId: String): CrimeStats {
        Log.d(TAG, "getCrimeStats called for character: $characterId")
        return CrimeStats()
    }
}