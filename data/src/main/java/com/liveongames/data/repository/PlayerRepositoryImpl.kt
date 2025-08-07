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
        private const val DEFAULT_MONEY = 1000
    }

    override fun getCharacter(characterId: String): Flow<Character?> = flow {
        val prefKey = "${CHARACTER_PREF_KEY}${characterId}"
        val characterJson = sharedPreferences.getString(prefKey, null)
        println("PlayerRepository - Getting character: $characterId, found: ${characterJson != null}")

        val character = if (characterJson != null && characterJson.isNotEmpty()) {
            try {
                val parsedCharacter = gson.fromJson(characterJson, Character::class.java)
                println("PlayerRepository - Parsed character money: ${parsedCharacter.money}")
                parsedCharacter
            } catch (e: Exception) {
                println("PlayerRepository - Error parsing character JSON: ${e.message}")
                createDefaultCharacter(characterId)
            }
        } else {
            println("PlayerRepository - No character found, creating default")
            createDefaultCharacter(characterId)
        }
        emit(character)
    }

    override suspend fun updateMoney(characterId: String, amount: Int) {
        println("PlayerRepository - updateMoney called for $characterId with amount $amount")

        val prefKey = "${CHARACTER_PREF_KEY}${characterId}"
        val characterJson = sharedPreferences.getString(prefKey, null)
        println("PlayerRepository - Current character JSON exists: ${characterJson != null}")

        if (characterJson != null && characterJson.isNotEmpty()) {
            try {
                val character = gson.fromJson(characterJson, Character::class.java)
                val newMoney = (character.money + amount).coerceAtLeast(0) // Ensure non-negative
                val updatedCharacter = character.copy(money = newMoney)
                saveCharacter(characterId, updatedCharacter)
                println("PlayerRepository - Money updated successfully. New money: $newMoney")
            } catch (e: Exception) {
                println("PlayerRepository - Error updating money: ${e.message}")
                // Create new character if parsing failed
                createAndSaveCharacterWithMoney(characterId, amount)
            }
        } else {
            // Create new character since none exists
            createAndSaveCharacterWithMoney(characterId, amount)
        }
    }

    override suspend fun setMoney(characterId: String, amount: Int) {
        val prefKey = "${CHARACTER_PREF_KEY}${characterId}"
        val characterJson = sharedPreferences.getString(prefKey, null)

        if (characterJson != null && characterJson.isNotEmpty()) {
            try {
                val character = gson.fromJson(characterJson, Character::class.java)
                val updatedCharacter = character.copy(money = amount.coerceAtLeast(0))
                saveCharacter(characterId, updatedCharacter)
                println("PlayerRepository - Money set to: $amount")
            } catch (e: Exception) {
                println("PlayerRepository - Error setting money: ${e.message}")
                createAndSaveCharacterWithMoney(characterId, amount)
            }
        } else {
            createAndSaveCharacterWithMoney(characterId, amount)
        }
    }

    override suspend fun updateJailTime(characterId: String, days: Int) {
        val prefKey = "${CHARACTER_PREF_KEY}${characterId}"
        val characterJson = sharedPreferences.getString(prefKey, null)

        if (characterJson != null && characterJson.isNotEmpty()) {
            try {
                val character = gson.fromJson(characterJson, Character::class.java)
                val newJailTime = (character.jailTime + days).coerceAtLeast(0)
                val updatedCharacter = character.copy(jailTime = newJailTime)
                saveCharacter(characterId, updatedCharacter)
                println("PlayerRepository - Jail time updated. New jail time: $newJailTime")
            } catch (e: Exception) {
                println("PlayerRepository - Error updating jail time: ${e.message}")
                createAndSaveCharacterWithJailTime(characterId, days)
            }
        } else {
            createAndSaveCharacterWithJailTime(characterId, days)
        }
    }

    override suspend fun createCharacter(characterId: String, character: Character) {
        saveCharacter(characterId, character)
        println("PlayerRepository - Character created: ${character.name}, money: ${character.money}")
    }

    override suspend fun updateCharacter(characterId: String, character: Character) {
        saveCharacter(characterId, character)
        println("PlayerRepository - Character updated: money: ${character.money}")
    }

    private fun saveCharacter(characterId: String, character: Character) {
        val prefKey = "${CHARACTER_PREF_KEY}${characterId}"
        val characterJson = gson.toJson(character)
        sharedPreferences.edit()
            .putString(prefKey, characterJson)
            .apply()
        println("PlayerRepository - Character saved to prefs under key: $prefKey")
        println("PlayerRepository - Saved character money: ${character.money}")
    }

    private fun createDefaultCharacter(characterId: String): Character {
        val defaultCharacter = Character(
            id = characterId,
            name = "Default Character",
            age = 18,
            health = 100,
            happiness = 50,
            money = DEFAULT_MONEY,
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
        saveCharacter(characterId, defaultCharacter)
        println("PlayerRepository - Created default character with money: ${defaultCharacter.money}")
        return defaultCharacter
    }

    private fun createAndSaveCharacterWithMoney(characterId: String, money: Int) {
        val newCharacter = Character(
            id = characterId,
            name = "Default Character",
            age = 18,
            health = 100,
            happiness = 50,
            money = money.coerceAtLeast(0),
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
        println("PlayerRepository - Created character with money: ${newCharacter.money}")
    }

    private fun createAndSaveCharacterWithJailTime(characterId: String, jailTime: Int) {
        val newCharacter = Character(
            id = characterId,
            name = "Default Character",
            age = 18,
            health = 100,
            happiness = 50,
            money = DEFAULT_MONEY,
            intelligence = 10,
            fitness = 10,
            social = 10,
            education = 0,
            career = null,
            relationships = emptyList(),
            achievements = emptyList(),
            events = emptyList(),
            jailTime = jailTime.coerceAtLeast(0)
        )
        saveCharacter(characterId, newCharacter)
        println("PlayerRepository - Created character with jail time: ${newCharacter.jailTime}")
    }
}