package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.Crime
import com.altlifegames.domain.repository.CrimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCrimesUseCase @Inject constructor(
    private val crimeRepository: CrimeRepository
) {
    operator fun invoke(characterId: String): Flow<List<Crime>> {
        return crimeRepository.getCrimes()
    }
}