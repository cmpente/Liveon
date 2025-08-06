// domain/src/main/java/com/liveongames/domain/model/EventOutcome.kt
package com.liveongames.domain.model

data class EventOutcome(
    val description: String,
    val statChanges: Map<String, Int> = emptyMap(),
    val ageProgression: Int = 0,
    val attribute: String = "", // For backward compatibility
    val change: Int = 0 // For backward compatibility
)