package com.liveongames.domain.model

data class CrimeStats(
    val currentYear: Int,
    val earnedThisYear: Int,
    val records: List<CrimeRecordEntry>
)