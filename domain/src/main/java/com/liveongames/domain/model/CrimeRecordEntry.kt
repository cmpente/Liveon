package com.liveongames.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CrimeRecordEntry(
    val id: String,
    val typeKey: String,     // CrimeType.name
    val success: Boolean,
    val caught: Boolean,
    val money: Int,
    val jailDays: Int,
    val year: Int,
    val timestamp: Long,
    val summary: String      // short human string to list
)
