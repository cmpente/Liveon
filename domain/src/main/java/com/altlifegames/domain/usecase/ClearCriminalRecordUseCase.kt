package com.altlifegames.domain.usecase

import com.altlifegames.domain.repository.CrimeRepository
import javax.inject.Inject
import com.altlifegames.domain.model.Character
import com.altlifegames.domain.model.EventOutcome

/**
 * Clears the character's criminal record.  Use carefully, only in situations like presidential pardon.
 */
class ClearCriminalRecordUseCase @Inject constructor(private val repository: CrimeRepository) {
    suspend operator fun invoke(characterId: Long) {
        repository.clearCriminalRecord(characterId)
    }
}