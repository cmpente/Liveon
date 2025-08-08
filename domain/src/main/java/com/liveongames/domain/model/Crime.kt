// domain/src/main/java/com/liveongames/domain/model/Crime.kt
package com.liveongames.domain.model

data class Crime(
    val id: String,
    val name: String,
    val description: String,
    val riskTier: RiskTier,
    val notorietyRequired: Int,
    val baseSuccessChance: Double,
    val payoutMin: Int,
    val payoutMax: Int,
    val jailMin: Int,
    val jailMax: Int,
    val notorietyGain: Int,
    val notorietyLoss: Int,
    val iconDescription: String,
    val scenario: String,
    val success: Boolean? = null,
    val caught: Boolean? = null,
    val moneyGained: Int? = null,
    val actualJailTime: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class RiskTier(
    val displayName: String,
    val color: String,
    val notorietyRequired: Int,
    val successChanceRange: ClosedFloatingPointRange<Double>,
    val notorietyGain: Int,
    val notorietyLoss: Int
) {
    LOW_RISK("Low Risk", "Green", 0, 0.6..0.8, 2, -3),
    MEDIUM_RISK("Medium Risk", "Yellow", 20, 0.5..0.65, 4, -6),
    HIGH_RISK("High Risk", "Orange", 50, 0.35..0.5, 7, -10),
    EXTREME_RISK("Extreme Risk", "Red", 80, 0.15..0.4, 10, -15)
}