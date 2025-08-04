package com.altlifegames.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a career path for a character.  Includes job title, company, salary and level.
 */
@Serializable
data class Career(
    val id: Long = 0,
    val title: String,
    val company: String,
    val salary: Double,
    val level: Int = 0,
    val experience: Int = 0
)