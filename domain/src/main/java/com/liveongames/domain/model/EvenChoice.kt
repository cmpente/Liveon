// domain/src/main/java/com/liveongames/domain/model/EventChoice.kt
package com.liveongames.domain.model

data class EventChoice(
    val id: String,
    val description: String,
    val text: String = "", // For backward compatibility
    val outcomes: List<EventOutcome> = emptyList()
)