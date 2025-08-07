// domain/src/main/java/com/liveongames/domain/model/Crime.kt
package com.liveongames.domain.model

data class Crime(
    val id: String,
    val name: String,
    val description: String,
    val severity: Int,  // 1-10 scale
    val chanceOfGettingCaught: Double,
    val fine: Int,
    val jailTime: Int  // in days
)