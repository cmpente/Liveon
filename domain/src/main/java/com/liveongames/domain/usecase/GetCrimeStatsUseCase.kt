package com.liveongames.domain.usecase

import com.liveongames.domain.repository.CrimeRepository
import com.liveongames.domain.repository.CrimeStats
import javax.inject.Inject

class GetCrimeStatsUseCase @Inject constructor(
    private val crimeRepository: CrimeRepository
) {
    suspend operator fun invoke(characterId: String): CrimeStats {
        return crimeRepository.getCrimeStats(characterId)
    }
}