package com.altlifegames.data.repository

import com.altlifegames.domain.model.Asset
import com.altlifegames.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class AssetRepositoryImpl @Inject constructor() : AssetRepository {

    private val assets = MutableStateFlow<List<Asset>>(emptyList())

    override fun getAssets(): Flow<List<Asset>> {
        return assets
    }

    override suspend fun addAsset(asset: Asset) {
        val currentAssets = assets.value.toMutableList()
        currentAssets.add(asset)
        assets.value = currentAssets
    }

    override suspend fun updateAsset(asset: Asset) {
        val currentAssets = assets.value.toMutableList()
        val index = currentAssets.indexOfFirst { it.id == asset.id }
        if (index != -1) {
            currentAssets[index] = asset
            assets.value = currentAssets
        }
    }

    override suspend fun removeAsset(assetId: String) {
        val currentAssets = assets.value.toMutableList()
        val index = currentAssets.indexOfFirst { it.id == assetId }
        if (index != -1) {
            currentAssets.removeAt(index)
            assets.value = currentAssets
        }
    }

    override suspend fun calculateNetWorth(): Int {
        return assets.value.sumOf { it.value }
    }
}