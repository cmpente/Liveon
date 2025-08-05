package com.altlifegames.domain.usecase

import com.altlifegames.domain.repository.CrimeRepository
import javax.inject.Inject

class ClearCriminalRecordUseCase @Inject constructor(
    private val crimeRepository: CrimeRepository
) {
    suspend operator fun invoke(characterId: String): Boolean {
        crimeRepository.clearCriminalRecord(characterId)
        return true
    }
}