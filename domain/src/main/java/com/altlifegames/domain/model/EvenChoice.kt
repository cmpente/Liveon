package com.altlifegames.domain.model

data class EventChoice(
    val id: String,
    val text: String,
    val description: String = "",
    val outcomes: List<EventOutcome> = emptyList()
)