package com.altlifegames.data.repository

import com.altlifegames.data.db.dao.AssetDao
import com.altlifegames.data.db.entity.AssetEntity
import com.altlifegames.domain.model.Asset
import com.altlifegames.domain.model.AssetType
import com.altlifegames.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AssetRepositoryImpl @Inject constructor(private val assetDao: AssetDao) : AssetRepository {
    override fun getAssets(characterId: Long): Flow<List<Asset>> =
        assetDao.getAssetsForCharacter(characterId).map { entities ->
            entities.map { entity ->
                Asset(
                    id = entity.id,
                    name = entity.name,
                    type = entity.type,
                    purchasePrice = entity.purchasePrice,
                    currentValue = entity.currentValue,
                    depreciationRate = entity.depreciationRate,
                    appreciationRate = entity.appreciationRate,
                    isMortgaged = entity.isMortgaged
                )
            }
        }

    override suspend fun addAsset(characterId: Long, asset: Asset) {
        val entity = AssetEntity(
            id = asset.id,
            characterId = characterId,
            name = asset.name,
            type = asset.type,
            purchasePrice = asset.purchasePrice,
            currentValue = asset.currentValue,
            depreciationRate = asset.depreciationRate,
            appreciationRate = asset.appreciationRate,
            isMortgaged = asset.isMortgaged
        )
        assetDao.insert(entity)
    }

    override suspend fun updateAsset(characterId: Long, asset: Asset) {
        val entity = AssetEntity(
            id = asset.id,
            characterId = characterId,
            name = asset.name,
            type = asset.type,
            purchasePrice = asset.purchasePrice,
            currentValue = asset.currentValue,
            depreciationRate = asset.depreciationRate,
            appreciationRate = asset.appreciationRate,
            isMortgaged = asset.isMortgaged
        )
        assetDao.update(entity)
    }

    override suspend fun removeAsset(characterId: Long, assetId: Long) {
        assetDao.deleteById(assetId)
    }

    override suspend fun calculateNetWorth(characterId: Long): Double {
        val assets = assetDao.getAssetsForCharacter(characterId).first()
        return assets.sumOf { it.currentValue }
    }
}