package com.liveongames.domain.model

data class Trait(
    val id: String,
    val name: String,
    val description: String,
    val effects: Map<String, Int> = emptyMap()
)