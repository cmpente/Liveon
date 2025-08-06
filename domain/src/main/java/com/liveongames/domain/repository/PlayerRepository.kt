// app/src/main/java/com/liveongames/domain/repository/PlayerRepository.kt
package com.liveongames.domain.repository

import com.liveongames.domain.model.Character
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getCharacter(characterId: String): Flow<Character?>
    suspend fun updateMoney(characterId: String, amount: Int)
    suspend fun setMoney(characterId: String, amount: Int)
    suspend fun updateJailTime(characterId: String, days: Int)
    suspend fun createCharacter(characterId: String, character: Character)
    suspend fun updateCharacter(characterId: String, character: Character)
}