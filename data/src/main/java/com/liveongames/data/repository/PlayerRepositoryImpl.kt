// app/src/main/java/com/liveongames/data/repository/PlayerRepositoryImpl.kt
package com.liveongames.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.liveongames.domain.model.Character
import com.liveongames.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PlayerRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : PlayerRepository {

    companion object {
        private const val CHARACTER_PREF_KEY = "character_"
    }

    override fun getCharacter(characterId: String): Flow<Character?> = flow {
        val characterJson = sharedPreferences.getString("${CHARACTER_PREF_KEY}${characterId}", null)
        val character = if (characterJson != null) {
            gson.fromJson(characterJson, Character::class.java)
        } else {
            null
        }
        emit(character)
    }

    override suspend fun updateMoney(characterId: String, amount: Int) {
        // First try to get existing character
        val characterJson = sharedPreferences.getString("${CHARACTER_PREF_KEY}${characterId}", null)

        if (characterJson != null) {
            val character = gson.fromJson(characterJson, Character::class.java)
            val newMoney = character.money + amount
            val updatedCharacter = character.copy(money = newMoney)
            saveCharacter(characterId, updatedCharacter)
        } else {
            // Create new character with the money amount
            val newCharacter = Character(
                id = characterId,
                name = "Default Character",
                age = 18,
                health = 100,
                happiness = 50,
                money = amount, // Use the amount as initial money
                intelligence = 10,
                fitness = 10,
                social = 10,
                education = 0,
                career = null,
                relationships = emptyList(),
                achievements = emptyList(),
                events = emptyList(),
                jailTime = 0
            )
            saveCharacter(characterId, newCharacter)
        }
    }

    override suspend fun setMoney(characterId: String, amount: Int) {
        val characterJson = sharedPreferences.getString("${CHARACTER_PREF_KEY}${characterId}", null)

        if (characterJson != null) {
            val character = gson.fromJson(characterJson, Character::class.java)
            val updatedCharacter = character.copy(money = amount)
            saveCharacter(characterId, updatedCharacter)
        } else {
            // Create new character with specified money
            val newCharacter = Character(
                id = characterId,
                name = "Default Character",
                age = 18,
                health = 100,
                happiness = 50,
                money = amount,
                intelligence = 10,
                fitness = 10,
                social = 10,
                education = 0,
                career = null,
                relationships = emptyList(),
                achievements = emptyList(),
                events = emptyList(),
                jailTime = 0
            )
            saveCharacter(characterId, newCharacter)
        }
    }

    override suspend fun updateJailTime(characterId: String, days: Int) {
        val characterJson = sharedPreferences.getString("${CHARACTER_PREF_KEY}${characterId}", null)

        if (characterJson != null) {
            val character = gson.fromJson(characterJson, Character::class.java)
            val newJailTime = character.jailTime + days
            val updatedCharacter = character.copy(jailTime = newJailTime)
            saveCharacter(characterId, updatedCharacter)
        } else {
            // Create new character with jail time
            val newCharacter = Character(
                id = characterId,
                name = "Default Character",
                age = 18,
                health = 100,
                happiness = 50,
                money = 1000,
                intelligence = 10,
                fitness = 10,
                social = 10,
                education = 0,
                career = null,
                relationships = emptyList(),
                achievements = emptyList(),
                events = emptyList(),
                jailTime = days
            )
            saveCharacter(characterId, newCharacter)
        }
    }

    override suspend fun createCharacter(characterId: String, character: Character) {
        saveCharacter(characterId, character)
    }

    override suspend fun updateCharacter(characterId: String, character: Character) {
        saveCharacter(characterId, character)
    }

    private fun saveCharacter(characterId: String, character: Character) {
        val characterJson = gson.toJson(character)
        sharedPreferences.edit()
            .putString("${CHARACTER_PREF_KEY}${characterId}", characterJson)
            .apply()
    }
}