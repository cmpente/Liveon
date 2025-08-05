package com.liveongames.domain.usecase

import com.liveongames.domain.model.Crime
import com.liveongames.domain.repository.CrimeRepository
import javax.inject.Inject

class RecordCrimeUseCase @Inject constructor(
    private val crimeRepository: CrimeRepository
) {
    suspend operator fun invoke(characterId: String, crime: Crime) {
        crimeRepository.recordCrime(characterId, crime)
    }
}