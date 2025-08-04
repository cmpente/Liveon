package com.altlifegames.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a personality trait or perk that influences event outcomes or stat changes.
 */
@Serializable
data class Trait(
    val id: String,
    val name: String,
    val description: String,
    val effects: Map<String, Int> = emptyMap() // map of stat name to modifier
)