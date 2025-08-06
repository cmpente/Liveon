// app/src/main/java/com/liveongames/liveon/viewmodel/CrimeViewModel.kt
package com.liveongames.liveon.viewmodel

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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random
import javax.inject.Inject

@HiltViewModel
class CrimeViewModel @Inject constructor(
    private val crimeRepository: CrimeRepository,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _crimes = MutableStateFlow<List<Crime>>(emptyList())
    val crimes: StateFlow<List<Crime>> = _crimes.asStateFlow()

    init {
        // Monitor crimes
        crimeRepository.getCrimes().onEach { crimeList ->
            _crimes.value = crimeList
        }.launchIn(viewModelScope)
    }

    fun commitCrime(type: CrimeType) {
        viewModelScope.launch {
            // Create a crime based on type
            val baseCrime = when (type) {
                CrimeType.THEFT -> Crime(
                    id = UUID.randomUUID().toString(),
                    name = "Theft",
                    description = "Stole something valuable",
                    severity = 3,
                    chanceOfGettingCaught = 0.3,
                    fine = 500,
                    jailTime = 0
                )
                CrimeType.ASSAULT -> Crime(
                    id = UUID.randomUUID().toString(),
                    name = "Assault",
                    description = "Physical altercation",
                    severity = 6,
                    chanceOfGettingCaught = 0.7,
                    fine = 2000,
                    jailTime = 30
                )
                CrimeType.FRAUD -> Crime(
                    id = UUID.randomUUID().toString(),
                    name = "Fraud",
                    description = "Financial deception",
                    severity = 5,
                    chanceOfGettingCaught = 0.4,
                    fine = 5000,
                    jailTime = 15
                )
                CrimeType.DRUG_POSSESSION -> Crime(
                    id = UUID.randomUUID().toString(),
                    name = "Drug Possession",
                    description = "Possession of illegal substances",
                    severity = 4,
                    chanceOfGettingCaught = 0.5,
                    fine = 1000,
                    jailTime = 5
                )
                CrimeType.DRUG_DEALING -> Crime(
                    id = UUID.randomUUID().toString(),
                    name = "Drug Dealing",
                    description = "Selling illegal substances",
                    severity = 8,
                    chanceOfGettingCaught = 0.8,
                    fine = 10000,
                    jailTime = 180
                )
                CrimeType.MURDER -> Crime(
                    id = UUID.randomUUID().toString(),
                    name = "Murder",
                    description = "Taking a life",
                    severity = 10,
                    chanceOfGettingCaught = 0.9,
                    fine = 0,
                    jailTime = 1825 // 5 years
                )
                CrimeType.EXTORTION -> Crime(
                    id = UUID.randomUUID().toString(),
                    name = "Extortion",
                    description = "Threatening for money",
                    severity = 7,
                    chanceOfGettingCaught = 0.6,
                    fine = 3000,
                    jailTime = 60
                )
                CrimeType.VANDALISM -> Crime(
                    id = UUID.randomUUID().toString(),
                    name = "Vandalism",
                    description = "Property damage",
                    severity = 2,
                    chanceOfGettingCaught = 0.4,
                    fine = 200,
                    jailTime = 0
                )
            }

            // Simulate chance outcome: success or failure
            val isSuccess = Random.nextDouble() > 0.3 // 70% base success rate
            val isCaught = Random.nextDouble() < baseCrime.chanceOfGettingCaught

            // Calculate money outcomes
            var moneyChange = 0
            var outcomeDescription = ""

            if (isSuccess) {
                // Successful crime - gain money (except for some crimes)
                moneyChange = when (type) {
                    CrimeType.THEFT -> Random.nextInt(100, 1000)
                    CrimeType.ASSAULT -> 0 // No money from assault
                    CrimeType.FRAUD -> Random.nextInt(500, 5000)
                    CrimeType.DRUG_POSSESSION -> 0 // No money from possession
                    CrimeType.DRUG_DEALING -> Random.nextInt(1000, 10000)
                    CrimeType.MURDER -> 0 // No money from murder
                    CrimeType.EXTORTION -> Random.nextInt(200, 2000)
                    CrimeType.VANDALISM -> Random.nextInt(50, 500)
                }
                outcomeDescription = "Successful crime! Gained $$moneyChange"

                // Apply money change to player
                if (moneyChange > 0) {
                    playerRepository.updateMoney("default_character", moneyChange)
                }
            } else {
                outcomeDescription = "Crime failed!"
            }

            // Handle consequences if caught
            if (isCaught) {
                // Apply fine if there is one
                if (baseCrime.fine > 0) {
                    playerRepository.updateMoney("default_character", -baseCrime.fine)
                    outcomeDescription += " Caught! Fined $$baseCrime.fine"
                }

                // Add jail time to player if applicable
                if (baseCrime.jailTime > 0) {
                    playerRepository.updateJailTime("default_character", baseCrime.jailTime)
                    outcomeDescription += " and jailed for ${baseCrime.jailTime} days"
                }
            }

            // Create the actual crime record with detailed outcome
            val actualCrime = baseCrime.copy(
                id = UUID.randomUUID().toString(),
                description = "${baseCrime.description} - $outcomeDescription"
            )

            // Record the crime
            crimeRepository.recordCrime("default_character", actualCrime)
        }
    }

    fun clearRecord() {
        viewModelScope.launch {
            crimeRepository.clearCriminalRecord("default_character")
            // Cost $5000 to clear record
            playerRepository.updateMoney("default_character", -5000)
        }
    }
}