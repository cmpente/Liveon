package com.liveongames.domain.model

data class Crime(
    val id: String,
    val name: String,
    val description: String,
    val severity: Int, // 1-10 scale
    val chanceOfGettingCaught: Double, // 0.0-1.0
    val fine: Int = 0,
    val jailTime: Int = 0 // in game days
)