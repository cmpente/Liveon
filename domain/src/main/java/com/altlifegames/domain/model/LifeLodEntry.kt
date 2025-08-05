// domain/src/main/java/com/altlifegames/domain/model/LifeLogEntry.kt
package com.altlifegames.domain.model

data class LifeLogEntry(
    val id: String,
    val message: String,
    val timestamp: String,
    val type: String // "event", "achievement", "relationship", etc.
)