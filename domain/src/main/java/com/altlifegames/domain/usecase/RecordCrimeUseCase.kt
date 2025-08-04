package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.CrimeRecord
import com.altlifegames.domain.repository.CrimeRepository
import javax.inject.Inject
import com.altlifegames.domain.model.Character
import com.altlifegames.domain.model.EventOutcome

/**
 * Records a new crime committed by the character.  The crime will be persisted in the character's
 * criminal record.
 */
class RecordCrimeUseCase @Inject constructor(private val repository: CrimeRepository) {
    suspend operator fun invoke(characterId: Long, crime: CrimeRecord) {
        repository.recordCrime(characterId, crime)
    }
}