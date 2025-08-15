// domain/src/main/java/com/liveongames/domain/usecase/GetCrimesUseCase.kt
package com.liveongames.domain.usecase

import com.liveongames.domain.model.CrimeRecordEntry
import com.liveongames.domain.repository.CrimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCrimesUseCase @Inject constructor(
    private val crimeRepository: CrimeRepository
) {
    operator fun invoke(characterId: String): Flow<List<CrimeRecordEntry>> =
        crimeRepository.observeStats().map { it.records }
}