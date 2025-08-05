package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.Scenario
import com.altlifegames.domain.repository.ScenarioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetScenariosUseCase @Inject constructor(
    private val scenarioRepository: ScenarioRepository
) {
    operator fun invoke(): Flow<List<Scenario>> {
        return scenarioRepository.getScenarios()
    }
}