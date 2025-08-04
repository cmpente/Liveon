package com.altlifegames.core.repository

import com.altlifegames.core.data.ScenarioDao
import com.altlifegames.core.model.Scenario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScenarioRepository(private val scenarioDao: ScenarioDao) {
    suspend fun getScenario(id: Long): Scenario? = withContext(Dispatchers.IO) {
        scenarioDao.getScenario(id)
    }

    suspend fun getAllScenarios(): List<Scenario> = withContext(Dispatchers.IO) {
        scenarioDao.getAllScenarios()
    }

    suspend fun insertScenario(scenario: Scenario): Long = withContext(Dispatchers.IO) {
        scenarioDao.insertScenario(scenario)
    }

    suspend fun updateScenario(scenario: Scenario) = withContext(Dispatchers.IO) {
        scenarioDao.updateScenario(scenario)
    }

    suspend fun deleteScenario(scenario: Scenario) = withContext(Dispatchers.IO) {
        scenarioDao.deleteScenario(scenario)
    }
}
