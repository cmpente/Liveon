package com.altlifegames.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Scenario(
    val id: String = "",
    val title: String = "",
    val name: String = "",
    val description: String = "",
    val startingAge: Int = 0,
    val maxAge: Int = 122,
    val startingStats: Stats = Stats(),
    val startingTraits: List<String> = emptyList(),
    val startingMoney: Int = 0,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val specialRules: Map<String, String> = emptyMap()
)

enum class Difficulty {
    EASY, NORMAL, HARD, EXTREME
}