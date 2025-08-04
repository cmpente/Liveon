package com.altlifegames.domain.model

data class Crime(
    val id: String,
    val characterId: Long,
    val crimeType: String,
    val description: String,
    val severity: Int,
    val dateCommitted: Long,
    val isSolved: Boolean,
    val sentenceYears: Int
)