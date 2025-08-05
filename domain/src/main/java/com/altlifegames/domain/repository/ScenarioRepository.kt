// domain/src/main/java/com/altlifegames/domain/repository/ScenarioRepository.kt
package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Scenario
import kotlinx.coroutines.flow.Flow

interface ScenarioRepository {
    fun getScenarios(): Flow<List<Scenario>>
    suspend fun getAvailableScenarios(characterLevel: Int): List<Scenario>
    suspend fun getScenarioById(scenarioId: String): Scenario?
}