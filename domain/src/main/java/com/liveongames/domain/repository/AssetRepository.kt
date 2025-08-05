package com.liveongames.domain.repository

import com.liveongames.domain.model.Asset
import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    fun getAssets(): Flow<List<Asset>>
    suspend fun addAsset(asset: Asset)
    suspend fun updateAsset(asset: Asset)
    suspend fun removeAsset(assetId: String)
    suspend fun calculateNetWorth(): Int
}
