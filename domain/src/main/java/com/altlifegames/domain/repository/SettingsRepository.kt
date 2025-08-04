package com.altlifegames.domain.repository

interface SettingsRepository {
    suspend fun isSoundEnabled(): Boolean
    suspend fun isMusicEnabled(): Boolean
    suspend fun isNotificationsEnabled(): Boolean
    suspend fun isDarkTheme(): Boolean
    suspend fun isMatureContentEnabled(): Boolean
    
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setMusicEnabled(enabled: Boolean)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setDarkTheme(enabled: Boolean)
    suspend fun setMatureContentEnabled(enabled: Boolean)
}