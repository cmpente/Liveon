// domain/src/main/java/com/liveongames/domain/model/LifeLogEntry.kt
package com.liveongames.domain.model

data class LifeLogEntry(
    val id: String = System.currentTimeMillis().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val description: String,
    val category: String,
    val age: Int,
    val statChanges: Map<String, Int> = emptyMap()
)