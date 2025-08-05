package com.altlifegames.data.repository

import com.altlifegames.domain.model.Save
import com.altlifegames.domain.repository.SaveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class SaveRepositoryImpl @Inject constructor() : SaveRepository {

    private val saves = MutableStateFlow<List<Save>>(emptyList())

    override fun getAllSaves(): Flow<List<Save>> {
        return saves
    }

    override suspend fun createSave(save: Save) {
        val currentSaves = saves.value.toMutableList()
        currentSaves.add(save)
        saves.value = currentSaves
    }

    override suspend fun loadSave(saveId: String): Save? {
        return saves.value.find { it.id == saveId }
    }

    override suspend fun deleteSave(saveId: String) {
        val currentSaves = saves.value.toMutableList()
        val index = currentSaves.indexOfFirst { it.id == saveId }
        if (index != -1) {
            currentSaves.removeAt(index)
            saves.value = currentSaves
        }
    }

    override suspend fun updateSave(save: Save) {
        val currentSaves = saves.value.toMutableList()
        val index = currentSaves.indexOfFirst { it.id == save.id }
        if (index != -1) {
            currentSaves[index] = save
            saves.value = currentSaves
        }
    }
}