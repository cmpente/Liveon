// app/src/main/java/com/liveongames/data/repository/PlayerRepositoryImpl.kt
package com.liveongames.data.repository

import com.liveongames.domain.model.Character
import com.liveongames.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PlayerRepositoryImpl : PlayerRepository {

    // In a real app, this would use a database or data source
    private val characters = mutableMapOf<String, Character>()

    override fun getCharacter(characterId: String): Flow<Character?> = flow {
        emit(characters[characterId])
    }

    override suspend fun updateMoney(characterId: String, amount: Int) {
        val character = characters[characterId]
        if (character != null) {
            val newMoney = (character.money ?: 0) + amount
            characters[characterId] = character.copy(money = newMoney)
        }
    }

    override suspend fun setMoney(characterId: String, amount: Int) {
        val character = characters[characterId]
        if (character != null) {
            characters[characterId] = character.copy(money = amount)
        }
    }

    override suspend fun updateJailTime(characterId: String, days: Int) {
        val character = characters[characterId]
        if (character != null) {
            val newJailTime = (character.jailTime ?: 0) + days
            characters[characterId] = character.copy(jailTime = newJailTime)
        }
    }

    override suspend fun createCharacter(characterId: String, character: Character) {
        characters[characterId] = character
    }

    override suspend fun updateCharacter(characterId: String, character: Character) {
        characters[characterId] = character
    }
}