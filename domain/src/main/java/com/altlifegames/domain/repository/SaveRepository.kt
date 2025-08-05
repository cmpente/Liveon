package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Save
import kotlinx.coroutines.flow.Flow

interface SaveRepository {
    fun getAllSaves(): Flow<List<Save>>
    suspend fun createSave(save: Save)
    suspend fun loadSave(saveId: String): Save?
    suspend fun deleteSave(saveId: String)
    suspend fun updateSave(save: Save)
}