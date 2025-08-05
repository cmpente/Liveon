package com.liveongames.data.repository

import com.liveongames.domain.model.Game
import com.liveongames.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class GameRepositoryImpl @Inject constructor() : GameRepository {

    private val games = MutableStateFlow<List<Game>>(emptyList())

    override fun getGames(): Flow<List<Game>> {
        return games
    }

    override suspend fun addGame(game: Game) {
        val currentGames = games.value.toMutableList()
        currentGames.add(game)
        games.value = currentGames
    }

    override suspend fun updateGame(game: Game) {
        val currentGames = games.value.toMutableList()
        val index = currentGames.indexOfFirst { it.id == game.id }
        if (index != -1) {
            currentGames[index] = game
            games.value = currentGames
        }
    }

    override suspend fun removeGame(gameId: String) {
        val currentGames = games.value.toMutableList()
        val index = currentGames.indexOfFirst { it.id == gameId }
        if (index != -1) {
            currentGames.removeAt(index)
            games.value = currentGames
        }
    }
}