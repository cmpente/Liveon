package com.liveongames.domain.repository

import com.liveongames.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<Settings>
    suspend fun updateSettings(settings: Settings)
    suspend fun resetToDefaults()
}