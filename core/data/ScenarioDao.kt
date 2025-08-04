package com.altlifegames.core.data

import androidx.room.*
import com.altlifegames.core.model.Scenario

@Dao
interface ScenarioDao {
    @Query("SELECT * FROM scenarios")
    suspend fun getAllScenarios(): List<Scenario>

    @Query("SELECT * FROM scenarios WHERE id = :id")
    suspend fun getScenario(id: Long): Scenario?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenario(scenario: Scenario): Long

    @Update
    suspend fun updateScenario(scenario: Scenario)

    @Delete
    suspend fun deleteScenario(scenario: Scenario)
}
