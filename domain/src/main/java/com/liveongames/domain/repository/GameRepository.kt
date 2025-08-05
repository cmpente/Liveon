package com.liveongames.domain.repository

import com.liveongames.domain.model.Game
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getGames(): Flow<List<Game>>
    suspend fun addGame(game: Game)
    suspend fun updateGame(game: Game)
    suspend fun removeGame(gameId: String)
}
