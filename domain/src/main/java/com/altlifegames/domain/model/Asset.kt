package com.altlifegames.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents an asset owned by a character.  Assets include homes, vehicles, collectibles and financial instruments.
 */
@Serializable
data class Asset(
    val id: Long = 0,
    val name: String,
    val type: AssetType,
    val purchasePrice: Double,
    val currentValue: Double,
    val depreciationRate: Double = 0.0,
    val appreciationRate: Double = 0.0,
    val isMortgaged: Boolean = false
)

enum class AssetType {
    REAL_ESTATE,
    VEHICLE,
    LUXURY,
    COLLECTIBLE,
    STOCK,
    BOND,
    CRYPTO,
    BUSINESS,
    OTHER
}