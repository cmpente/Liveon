package com.altlifegames.domain.model

data class Settings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val difficulty: String = "normal"
)