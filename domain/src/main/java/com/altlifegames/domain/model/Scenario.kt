// domain/src/main/java/com/altlifegames/domain/model/Scenario.kt
package com.altlifegames.domain.model

data class Scenario(
    val id: String,
    val title: String,
    val description: String,
    val choices: List<ScenarioChoice>,
    val outcomes: List<ScenarioOutcome> = emptyList(),
    val requiredLevel: Int = 1
)

data class ScenarioChoice(
    val id: String,
    val text: String,
    val requirements: Map<String, Int> = emptyMap(),
    val outcomes: List<ScenarioOutcome> = emptyList()
)

data class ScenarioOutcome(
    val attribute: String,
    val change: Int,
    val condition: String? = null
)