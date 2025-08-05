package com.liveongames.core.repository

import com.liveongames.core.data.ScenarioDao
import com.liveongames.core.model.Scenario
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
