package com.liveongames.domain.model

data class CrimeRecord(
    val id: String,
    val characterId: String,
    val crimeType: String,
    val description: String,
    val penalty: Int,
    val timestamp: Long
)