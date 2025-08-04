package com.altlifegames.data.repository

import com.altlifegames.data.datasource.GameDataSource
import javax.inject.Inject

class GameRepository @Inject constructor(
    private val dataSource: GameDataSource
) {
    // For now, let's make this a minimal implementation to avoid compilation issues
    // You can expand it later with proper models
    
    fun loadAllContent() {
        // Placeholder
    }
}