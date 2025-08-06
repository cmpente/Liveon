// data/src/main/java/com/liveongames/data/model/JsonEvent.kt
package com.liveongames.data.model

data class JsonEvent(
    val id: Any, // Can be String or Int
    val title: String,
    val description: String,
    val type: String? = "NEUTRAL",
    val isMature: Boolean? = false,
    val choices: List<JsonChoice>,
    val minAge: Int? = 0,
    val maxAge: Int? = 100,
    val probability: Double? = 1.0,
    val isRepeatable: Boolean? = false,
    val category: String? = "life",
    val tags: List<String>? = null // For childhood events
)

data class JsonChoice(
    val id: String? = null,
    val text: String? = null,
    val description: String,
    val result: JsonResult? = null, // For childhood events
    val outcomes: List<JsonOutcome>? = null // For regular events
)

data class JsonResult(
    val outcome: String,
    val statChanges: Map<String, Int> = emptyMap()
)

data class JsonOutcome(
    val description: String,
    val statChanges: Map<String, Int> = emptyMap(),
    val ageProgression: Int = 0
)