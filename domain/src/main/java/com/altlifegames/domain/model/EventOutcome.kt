package com.altlifegames.domain.model

data class EventOutcome(
    val attribute: String,
    val change: Int,
    val description: String = "",
    val statChanges: Map<String, Int> = emptyMap(),
    val ageProgression: Int = 0
)