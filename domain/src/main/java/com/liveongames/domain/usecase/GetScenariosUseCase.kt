package com.liveongames.domain.usecase

import com.liveongames.domain.model.Scenario
import com.liveongames.domain.repository.ScenarioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetScenariosUseCase @Inject constructor(
    private val scenarioRepository: ScenarioRepository
) {
    operator fun invoke(): Flow<List<Scenario>> {
        return scenarioRepository.getScenarios()
    }
}