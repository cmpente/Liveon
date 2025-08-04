package com.altlifegames.domain.model

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val minAge: Int,
    val maxAge: Int,
    val probability: Double,
    val isRepeatable: Boolean,
    val requiredTraits: List<String> = emptyList(),
    val requiredStats: Map<String, Int> = emptyMap(),
    val outcomes: List<EventOutcome> = emptyList()
)