package com.liveongames.domain.model

/**
 * Domain model for a crime definition (and, optionally, a last-run snapshot).
 *
 * NOTE: Some fields (success/caught/moneyGained/actualJailTime/timestamp)
 * are nullable to support mappers that attach the *last outcome* when
 * projecting from storage. If you only use static definitions, leave them null.
 */
data class Crime(
    // Definition
    val id: String,
    val name: String,
    val description: String,
    val riskTier: RiskTier,
    val notorietyRequired: Int = 0,
    /** Base success chance as a percentage (0..100). */
    val baseSuccessChance: Int = 50,

    /** Payout range if successful (inclusive). */
    val payoutMin: Int = 0,
    val payoutMax: Int = 0,

    /** Jail time range if caught (inclusive), in days (or your chosen unit). */
    val jailMin: Int = 0,
    val jailMax: Int = 0,

    /** Notoriety deltas used by your caps logic. */
    val notorietyGain: Int = 0,
    val notorietyLoss: Int = 0,

    /** Optional icon/content hints. */
    val iconDescription: String = "",
    val scenario: String? = null,

    // Optional last-run snapshot (nullable; safe defaults)
    val success: Boolean? = null,
    val caught: Boolean? = null,
    val moneyGained: Int? = null,
    val actualJailTime: Int? = null,
    val timestamp: Long? = null
)
