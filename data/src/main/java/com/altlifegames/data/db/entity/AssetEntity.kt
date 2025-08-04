package com.altlifegames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.altlifegames.domain.model.AssetType

/**
 * Room entity representing an asset owned by a character.
 */
@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val characterId: Long,
    val name: String,
    val type: AssetType,
    val purchasePrice: Double,
    val currentValue: Double,
    val depreciationRate: Double,
    val appreciationRate: Double,
    val isMortgaged: Boolean
)