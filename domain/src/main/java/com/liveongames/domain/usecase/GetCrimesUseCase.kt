package com.liveongames.domain.usecase

import com.liveongames.domain.model.Crime
import com.liveongames.domain.repository.CrimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCrimesUseCase @Inject constructor(
    private val crimeRepository: CrimeRepository
) {
    operator fun invoke(characterId: String): Flow<List<Crime>> {
        return crimeRepository.getCrimes()
    }
}