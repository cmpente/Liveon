package com.altlifegames.data.repository

import android.content.Context
import com.altlifegames.domain.repository.SettingsRepository
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(private val context: Context) : SettingsRepository {
    private val prefs = context.getSharedPreferences("altlife_settings", Context.MODE_PRIVATE)
    
    override suspend fun isSoundEnabled(): Boolean = prefs.getBoolean("sound_enabled", true)
    override suspend fun isMusicEnabled(): Boolean = prefs.getBoolean("music_enabled", true)
    override suspend fun isNotificationsEnabled(): Boolean = prefs.getBoolean("notifications_enabled", true)
    override suspend fun isDarkTheme(): Boolean = prefs.getBoolean("dark_theme", false)
    override suspend fun isMatureContentEnabled(): Boolean = prefs.getBoolean("mature_content", false)
    
    override suspend fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }
    
    override suspend fun setMusicEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("music_enabled", enabled).apply()
    }
    
    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }
    
    override suspend fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean("dark_theme", enabled).apply()
    }
    
    override suspend fun setMatureContentEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("mature_content", enabled).apply()
    }
}