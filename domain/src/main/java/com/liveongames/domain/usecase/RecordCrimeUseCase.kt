// domain/src/main/java/com/liveongames/domain/usecase/RecordCrimeUseCase.kt
package com.liveongames.domain.usecase

import com.liveongames.domain.model.CrimeRecordEntry
import com.liveongames.domain.repository.CrimeRepository
import javax.inject.Inject

class RecordCrimeUseCase @Inject constructor(
    private val crimeRepository: CrimeRepository
) {
    suspend operator fun invoke(characterId: String, entry: CrimeRecordEntry) {
        crimeRepository.appendRecord(entry)
    }
}
