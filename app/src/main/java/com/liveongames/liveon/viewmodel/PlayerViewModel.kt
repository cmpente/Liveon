package com.liveongames.liveon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.domain.model.CrimeRecordEntry
import com.liveongames.domain.model.CrimeStats
import com.liveongames.domain.usecase.GetCrimeStatsUseCase
import com.liveongames.domain.usecase.GetCrimesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val getCrimesUseCase: GetCrimesUseCase,
    private val getCrimeStatsUseCase: GetCrimeStatsUseCase
) : ViewModel() {

    // Non-null default so we never assign null to a non-null StateFlow<T>
    private val _crimeStats = MutableStateFlow(CrimeStats(currentYear = 0, earnedThisYear = 0, records = emptyList()))
    val crimeStats: StateFlow<CrimeStats> = _crimeStats

    private val _crimes = MutableStateFlow<List<CrimeRecordEntry>>(emptyList())
    val crimes: StateFlow<List<CrimeRecordEntry>> = _crimes

    /** Stream the player’s criminal record list (CrimeRecordEntry). */
    fun loadCrimes(characterId: String) {
        viewModelScope.launch {
            getCrimesUseCase(characterId).collectLatest { list ->
                _crimes.value = list
            }
        }
    }

    /** Stream full stats (year, earnedThisYear cap, and records list). */
    fun loadCrimeStats(characterId: String) {
        viewModelScope.launch {
            // GetCrimeStatsUseCase returns Flow<CrimeStats>; collect it
            getCrimeStatsUseCase(characterId).collectLatest { stats ->
                _crimeStats.value = stats
            }
        }
    }
}