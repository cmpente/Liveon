// Updated GameEvent.kt to match your JSON structure exactly
package com.altlifegames.altlifealpha.data.models

data class GameEvent(
    val id: String,
    val title: String,
    val description: String,
    val type: String = "NEUTRAL",
    val isMature: Boolean = false,
    val choices: List<EventChoice>,
    val minAge: Int,
    val maxAge: Int,
    val probability: Double = 1.0,
    val isRepeatable: Boolean = false,
    val category: String = "life"
)

data class EventChoice(
    val id: String? = null,  // Some JSON entries don't have this
    val description: String,
    val outcomes: List<EventOutcome>,
    val text: String? = null  // For older format compatibility
) {
    // Helper to get the display text
    val displayText: String
        get() = text ?: description ?: ""
}

data class EventOutcome(
    val description: String,
    val statChanges: Map<String, Int>,
    val ageProgression: Int = 0
)