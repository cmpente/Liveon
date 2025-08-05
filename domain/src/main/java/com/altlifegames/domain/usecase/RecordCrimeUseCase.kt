package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.Crime
import com.altlifegames.domain.repository.CrimeRepository
import javax.inject.Inject

class RecordCrimeUseCase @Inject constructor(
    private val crimeRepository: CrimeRepository
) {
    suspend operator fun invoke(characterId: String, crime: Crime) {
        crimeRepository.recordCrime(characterId, crime)
    }
}