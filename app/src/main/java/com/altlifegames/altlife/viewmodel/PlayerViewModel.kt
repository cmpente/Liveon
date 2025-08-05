package com.altlifegames.altlife.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.altlifegames.domain.usecase.GetCrimesUseCase
import com.altlifegames.domain.usecase.GetCrimeStatsUseCase
import com.altlifegames.domain.model.Crime
import com.altlifegames.domain.repository.CrimeStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val getCrimesUseCase: GetCrimesUseCase,
    private val getCrimeStatsUseCase: GetCrimeStatsUseCase
) : ViewModel() {

    private val _crimes = MutableStateFlow<List<Crime>>(emptyList())
    val crimes: StateFlow<List<Crime>> = _crimes

    private val _crimeStats = MutableStateFlow<CrimeStats?>(null)
    val crimeStats: StateFlow<CrimeStats?> = _crimeStats

    fun loadCrimes(characterId: String) {
        viewModelScope.launch {
            getCrimesUseCase(characterId).collectLatest { crimeList ->
                _crimes.value = crimeList
            }
        }
    }

    fun loadCrimeStats(characterId: String) {
        viewModelScope.launch {
            try {
                val stats = getCrimeStatsUseCase(characterId)
                _crimeStats.value = stats
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }
}