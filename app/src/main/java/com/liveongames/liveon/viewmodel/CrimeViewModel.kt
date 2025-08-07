// app/src/main/java/com/liveongames/liveon/viewmodel/CrimeViewModel.kt
package com.liveongames.liveon.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.domain.model.Crime
import com.liveongames.domain.model.CrimeType
import com.liveongames.domain.repository.CrimeRepository
import com.liveongames.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random
import javax.inject.Inject
import kotlinx.coroutines.flow.collect // Add this import

@HiltViewModel
class CrimeViewModel @Inject constructor(
    private val crimeRepository: CrimeRepository,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    companion object {
        private const val CHARACTER_ID = "player_character"
        private const val TAG = "CrimeViewModel"
    }

    private val _crimes = MutableStateFlow<List<Crime>>(emptyList())
    val crimes: StateFlow<List<Crime>> = _crimes.asStateFlow()

    init {
        observeCrimes()
        viewModelScope.launch {
            ensureCharacterExists()
        }
    }

    private fun observeCrimes() {
        Log.d(TAG, "Observing crimes...")
        viewModelScope.launch {
            try {
                crimeRepository.getCrimes().collect { crimeList ->
                    Log.d(TAG, "Observed crime list update: ${crimeList.size} crimes")
                    if (_crimes.value != crimeList) {
                        _crimes.value = crimeList
                        Log.d(TAG, "Updated _crimes StateFlow with ${crimeList.size} crimes")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing crimes", e)
            }
        }
    }

    fun commitCrime(type: CrimeType) {
        Log.d(TAG, "Attempting to commit crime: $type")
        viewModelScope.launch {
            try {
                ensureCharacterExists()

                val baseCrime = createCrime(type)
                Log.d(TAG, "Created base crime: ${baseCrime.name}")

                // Generate unique ID for the crime
                val crimeId = UUID.randomUUID().toString()

                // Simulate chance outcome: success or failure
                val isSuccess = Random.nextDouble() > 0.3 // 70% base success rate
                val isCaught = Random.nextDouble() < baseCrime.chanceOfGettingCaught

                Log.d(TAG, "Crime success: $isSuccess, caught: $isCaught")

                // Calculate money outcomes
                var moneyChange = 0
                var outcomeDescription = ""

                if (isSuccess) {
                    // Successful crime - gain money
                    moneyChange = calculateMoneyGain(type)
                    outcomeDescription = "Gained $$moneyChange"
                    Log.d(TAG, "Crime successful, gained money: $moneyChange")
                } else {
                    outcomeDescription = "Crime failed"
                    Log.d(TAG, "Crime failed, no money gained")
                }

                // Handle consequences if caught
                if (isCaught) {
                    // Apply fine if there is one
                    if (baseCrime.fine > 0) {
                        playerRepository.updateMoney(CHARACTER_ID, -baseCrime.fine)
                        outcomeDescription += if (outcomeDescription.isNotEmpty()) {
                            " • Fined $$baseCrime.fine"
                        } else {
                            "Fined $$baseCrime.fine"
                        }
                    }

                    // Add jail time based on crime severity
                    if (baseCrime.jailTime > 0) {
                        outcomeDescription += if (outcomeDescription.isNotEmpty()) {
                            " • ${baseCrime.jailTime} days jail"
                        } else {
                            "${baseCrime.jailTime} days jail"
                        }
                    }

                    outcomeDescription += " • 🔒 Caught!"
                } else {
                    outcomeDescription += " • 🏃 Escaped!"
                }

                // Create the actual crime record with detailed outcome
                val actualCrime = baseCrime.copy(
                    id = crimeId,
                    description = "${baseCrime.description} - $outcomeDescription"
                )

                Log.d(TAG, "Recording crime: ${actualCrime.name} - ${actualCrime.description}")

                // Record the crime
                crimeRepository.recordCrime(CHARACTER_ID, actualCrime)

                // Apply money change to player if successful
                if (moneyChange > 0) {
                    playerRepository.updateMoney(CHARACTER_ID, moneyChange)
                    Log.d(TAG, "Updated player money by: $moneyChange")
                }

                Log.d(TAG, "Crime committed successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Error committing crime", e)
            }
        }
    }

    private fun createCrime(type: CrimeType): Crime {
        return when (type) {
            CrimeType.THEFT -> Crime(
                id = "",
                name = "Theft",
                description = "Stole something valuable",
                severity = 3,
                chanceOfGettingCaught = 0.3,
                fine = 500,
                jailTime = 0
            )
            CrimeType.ASSAULT -> Crime(
                id = "",
                name = "Assault",
                description = "Physical altercation",
                severity = 6,
                chanceOfGettingCaught = 0.7,
                fine = 2000,
                jailTime = 30
            )
            CrimeType.FRAUD -> Crime(
                id = "",
                name = "Fraud",
                description = "Financial deception",
                severity = 5,
                chanceOfGettingCaught = 0.4,
                fine = 5000,
                jailTime = 15
            )
            CrimeType.DRUG_POSSESSION -> Crime(
                id = "",
                name = "Drug Possession",
                description = "Possession of illegal substances",
                severity = 4,
                chanceOfGettingCaught = 0.5,
                fine = 1000,
                jailTime = 5
            )
            CrimeType.DRUG_DEALING -> Crime(
                id = "",
                name = "Drug Dealing",
                description = "Selling illegal substances",
                severity = 8,
                chanceOfGettingCaught = 0.8,
                fine = 10000,
                jailTime = 180
            )
            CrimeType.MURDER -> Crime(
                id = "",
                name = "Murder",
                description = "Taking a life",
                severity = 10,
                chanceOfGettingCaught = 0.9,
                fine = 0,
                jailTime = 1825
            )
            CrimeType.EXTORTION -> Crime(
                id = "",
                name = "Extortion",
                description = "Threatening for money",
                severity = 7,
                chanceOfGettingCaught = 0.6,
                fine = 3000,
                jailTime = 60
            )
            CrimeType.VANDALISM -> Crime(
                id = "",
                name = "Vandalism",
                description = "Property damage",
                severity = 2,
                chanceOfGettingCaught = 0.4,
                fine = 200,
                jailTime = 0
            )
        }
    }

    private fun calculateMoneyGain(type: CrimeType): Int {
        return when (type) {
            CrimeType.THEFT -> Random.nextInt(75, 5000)
            CrimeType.ASSAULT -> 200
            CrimeType.FRAUD -> Random.nextInt(500, 50000)
            CrimeType.DRUG_POSSESSION -> 100
            CrimeType.DRUG_DEALING -> Random.nextInt(150, 9999)
            CrimeType.MURDER -> Random.nextInt(25000, 80000)
            CrimeType.EXTORTION -> Random.nextInt(1500, 15000)
            CrimeType.VANDALISM -> Random.nextInt(50, 1999)
        }
    }

    private suspend fun ensureCharacterExists() {
        try {
            Log.d(TAG, "Ensuring character exists...")
            // Check if character exists
            val character = playerRepository.getCharacter(CHARACTER_ID).first()
            if (character == null) {
                Log.d(TAG, "Creating default character...")
                // Create default character with money
                val defaultCharacter = com.liveongames.domain.model.Character(
                    id = CHARACTER_ID,
                    name = "Default Character",
                    age = 18,
                    health = 100,
                    happiness = 50,
                    money = 1000,
                    intelligence = 10,
                    fitness = 10,
                    social = 10,
                    education = 0,
                    career = null,
                    relationships = emptyList(),
                    achievements = emptyList(),
                    events = emptyList(),
                    jailTime = 0
                )
                playerRepository.createCharacter(CHARACTER_ID, defaultCharacter)
                Log.d(TAG, "Default character created successfully")
            } else {
                Log.d(TAG, "Character already exists: ${character.id}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error ensuring character exists", e)
        }
    }

    fun clearRecord() {
        Log.d(TAG, "Clearing criminal record...")
        viewModelScope.launch {
            try {
                crimeRepository.clearCriminalRecord(CHARACTER_ID)
                // Cost $5000 to clear record
                playerRepository.updateMoney(CHARACTER_ID, -5000)
                Log.d(TAG, "Criminal record cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing criminal record", e)
            }
        }
    }
}