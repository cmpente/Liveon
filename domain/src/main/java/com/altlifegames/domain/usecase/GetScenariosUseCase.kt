package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.Scenario
import com.altlifegames.domain.repository.ScenarioRepository
import javax.inject.Inject
import com.altlifegames.domain.model.Character
import com.altlifegames.domain.model.EventOutcome

class GetScenarioUseCase @Inject constructor(private val repository: ScenarioRepository) {
    suspend operator fun invoke(id: String): Scenario? {
        return repository.getScenario(id)
    }
}