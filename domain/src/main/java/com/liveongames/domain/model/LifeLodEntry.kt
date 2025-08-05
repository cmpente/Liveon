// domain/src/main/java/com/liveongames/domain/model/LifeLogEntry.kt
package com.liveongames.domain.model

data class LifeLogEntry(
    val id: String,
    val message: String,
    val timestamp: String,
    val type: String // "event", "achievement", "relationship", etc.
)