package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Asset
import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    fun getAssets(characterId: Long): Flow<List<Asset>>
    suspend fun addAsset(characterId: Long, asset: Asset)
    suspend fun updateAsset(characterId: Long, asset: Asset)
    suspend fun removeAsset(characterId: Long, assetId: Long)
    suspend fun calculateNetWorth(characterId: Long): Double
}