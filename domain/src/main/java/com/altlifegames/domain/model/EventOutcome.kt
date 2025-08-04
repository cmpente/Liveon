package com.altlifegames.domain.model

data class EventOutcome(
    val statChanges: Map<String, Int> = emptyMap(),
    val ageProgression: Int = 0,
    val traitsGained: List<String> = emptyList(),
    val traitsLost: List<String> = emptyList()
)