package com.altlifegames.data.repository

import com.altlifegames.domain.model.Scenario
import com.altlifegames.domain.repository.ScenarioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class ScenarioRepositoryImpl @Inject constructor() : ScenarioRepository {

    private val scenarios = MutableStateFlow<List<Scenario>>(emptyList())

    override fun getScenarios(): Flow<List<Scenario>> {
        return scenarios
    }

    override suspend fun getAvailableScenarios(characterLevel: Int): List<Scenario> {
        // Return scenarios where character level meets requirement
        return scenarios.value.filter { it.requiredLevel <= characterLevel }
    }

    override suspend fun getScenarioById(scenarioId: String): Scenario? {
        return scenarios.value.find { it.id == scenarioId }
    }
}