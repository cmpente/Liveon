// app/src/main/java/com/liveongames/liveon/viewmodel/CrimeViewModel.kt
package com.liveongames.liveon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.domain.model.Crime
import com.liveongames.domain.model.CrimeType
import com.liveongames.domain.repository.CrimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CrimeViewModel @Inject constructor(
    private val crimeRepository: CrimeRepository
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
            val crime = when (type) {
                CrimeType.THEFT -> Crime(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Theft",
                    description = "Stole something valuable",
                    severity = 3,
                    chanceOfGettingCaught = 0.3,
                    fine = 500,
                    jailTime = 0
                )
                CrimeType.ASSAULT -> Crime(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Assault",
                    description = "Physical altercation",
                    severity = 6,
                    chanceOfGettingCaught = 0.7,
                    fine = 2000,
                    jailTime = 30
                )
                CrimeType.FRAUD -> Crime(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Fraud",
                    description = "Financial deception",
                    severity = 5,
                    chanceOfGettingCaught = 0.4,
                    fine = 5000,
                    jailTime = 15
                )
                CrimeType.DRUG_POSSESSION -> Crime(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Drug Possession",
                    description = "Possession of illegal substances",
                    severity = 4,
                    chanceOfGettingCaught = 0.5,
                    fine = 1000,
                    jailTime = 5
                )
                CrimeType.DRUG_DEALING -> Crime(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Drug Dealing",
                    description = "Selling illegal substances",
                    severity = 8,
                    chanceOfGettingCaught = 0.8,
                    fine = 10000,
                    jailTime = 180
                )
                CrimeType.MURDER -> Crime(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Murder",
                    description = "Taking a life",
                    severity = 10,
                    chanceOfGettingCaught = 0.9,
                    fine = 0,
                    jailTime = 1825 // 5 years
                )
                CrimeType.OTHER -> Crime(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Other Crime",
                    description = "Miscellaneous criminal activity",
                    severity = 2,
                    chanceOfGettingCaught = 0.2,
                    fine = 100,
                    jailTime = 0
                )
            }

            // Use a default character ID since we don't have character management yet
            crimeRepository.recordCrime("default_character", crime)
        }
    }

    fun clearRecord() {
        viewModelScope.launch {
            crimeRepository.clearCriminalRecord("default_character")
        }
    }
}