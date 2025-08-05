// domain/src/main/java/com/liveongames/domain/model/GameEvent.kt
package com.liveongames.domain.model

data class GameEvent(
    val id: String,
    val title: String,
    val description: String,
    val choices: List<EventChoice> = emptyList(),
    val type: String = "NEUTRAL",
    val isMature: Boolean = false,
    val minAge: Int = 0,
    val maxAge: Int = 100,
    val probability: Double = 1.0,
    val isRepeatable: Boolean = false,
    val category: String = "life"
)