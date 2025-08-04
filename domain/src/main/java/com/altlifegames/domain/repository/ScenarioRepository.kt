package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Scenario
import kotlinx.coroutines.flow.Flow

/**
 * Repository for providing starting scenarios.  Scenarios are loaded from JSON assets and
 * allow players to begin with unique stat distributions and traits.
 */
interface ScenarioRepository {
    fun getScenarios(): Flow<List<Scenario>>
    suspend fun getScenario(id: String): Scenario?
}