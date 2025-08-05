// domain/src/main/java/com/liveongames/domain/repository/ScenarioRepository.kt
package com.liveongames.domain.repository

import com.liveongames.domain.model.Scenario
import kotlinx.coroutines.flow.Flow

interface ScenarioRepository {
    fun getScenarios(): Flow<List<Scenario>>
    suspend fun getAvailableScenarios(characterLevel: Int): List<Scenario>
    suspend fun getScenarioById(scenarioId: String): Scenario?
}