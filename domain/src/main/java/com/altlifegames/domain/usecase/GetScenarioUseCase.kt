package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.Scenario
import com.altlifegames.domain.repository.ScenarioRepository
import javax.inject.Inject

class GetScenarioUseCase @Inject constructor(
    private val scenarioRepository: ScenarioRepository
) {
    suspend operator fun invoke(scenarioId: String): Scenario? {
        return scenarioRepository.getScenarioById(scenarioId)
    }
}