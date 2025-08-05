package com.liveongames.domain.model

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val type: String = "random",
    val isShown: Boolean = false,
    val choices: List<EventChoice> = emptyList()
)