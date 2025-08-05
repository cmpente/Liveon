package com.altlifegames.data.repository

import com.altlifegames.domain.model.Settings
import com.altlifegames.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor() : SettingsRepository {

    private val settings = MutableStateFlow<Settings>(Settings())

    override fun getSettings(): Flow<Settings> {
        return settings
    }

    override suspend fun updateSettings(settings: Settings) {
        this.settings.value = settings
    }

    override suspend fun resetToDefaults() {
        settings.value = Settings()
    }
}