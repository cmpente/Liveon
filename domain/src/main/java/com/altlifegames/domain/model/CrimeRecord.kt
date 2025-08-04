package com.altlifegames.domain.model

data class CrimeRecord(
    val id: String,
    val crimeType: CrimeType,
    val severity: Int,
    val outcome: CrimeOutcome,
    val sentenceYears: Int
)