package com.altlifegames.domain.usecase

import com.altlifegames.domain.repository.CrimeRepository
import com.altlifegames.domain.repository.CrimeStats
import javax.inject.Inject

class GetCrimeStatsUseCase @Inject constructor(
    private val crimeRepository: CrimeRepository
) {
    suspend operator fun invoke(characterId: String): CrimeStats {
        return crimeRepository.getCrimeStats(characterId)
    }
}