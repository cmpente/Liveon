// domain/src/main/java/com/liveongames/domain/usecase/GetCrimeStatsUseCase.kt
package com.liveongames.domain.usecase

import com.liveongames.domain.model.CrimeStats
import com.liveongames.domain.repository.CrimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCrimeStatsUseCase @Inject constructor(
    private val crimeRepository: CrimeRepository
) {
    operator fun invoke(characterId: String): Flow<CrimeStats> =
        crimeRepository.observeStats()
}