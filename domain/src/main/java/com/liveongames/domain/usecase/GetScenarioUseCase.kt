package com.liveongames.domain.usecase

import com.liveongames.domain.model.Scenario
import com.liveongames.domain.repository.ScenarioRepository
import javax.inject.Inject

class GetScenarioUseCase @Inject constructor(
    private val scenarioRepository: ScenarioRepository
) {
    suspend operator fun invoke(scenarioId: String): Scenario? {
        return scenarioRepository.getScenarioById(scenarioId)
    }
}