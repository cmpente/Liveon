package com.altlifegames.data.repository

import android.content.Context
import com.altlifegames.domain.model.Scenario
import com.altlifegames.domain.repository.ScenarioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class ScenarioRepositoryImpl @Inject constructor(private val context: Context) : ScenarioRepository {
    
    override fun getScenarios(): Flow<List<Scenario>> = flow {
        val scenarios = listOf(
            Scenario(
                id = "normal_life",
                name = "Normal Life",
                description = "Start a standard life journey",
                startingAge = 0,
                startingMoney = 1000  // Changed from 1000.0 to Int
            ),
            Scenario(
                id = "rich_start",
                name = "Rich Start",
                description = "Begin life with significant wealth",
                startingAge = 0,
                startingMoney = 100000  // Changed from 100000.0 to Int
            ),
            Scenario(
                id = "poor_start",
                name = "Struggling Start",
                description = "Start with very limited resources",
                startingAge = 0,
                startingMoney = 100  // Changed from 100.0 to Int
            )
        )
        emit(scenarios)
    }

    override suspend fun getScenario(id: String): Scenario? {
        return getScenarios().firstOrNull()?.firstOrNull { it.id == id }
    }
}