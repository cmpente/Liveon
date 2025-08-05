package com.liveongames.domain.usecase

import com.liveongames.domain.repository.CrimeRepository
import javax.inject.Inject

class ClearCriminalRecordUseCase @Inject constructor(
    private val crimeRepository: CrimeRepository
) {
    suspend operator fun invoke(characterId: String): Boolean {
        crimeRepository.clearCriminalRecord(characterId)
        return true
    }
}