package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<Settings>
    suspend fun updateSettings(settings: Settings)
    suspend fun resetToDefaults()
}