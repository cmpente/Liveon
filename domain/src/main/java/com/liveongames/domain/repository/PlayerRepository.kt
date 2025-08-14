// domain/src/main/java/com/liveongames/domain/repository/PlayerRepository.kt
package com.liveongames.domain.repository

import com.liveongames.domain.model.Character
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getCharacter(characterId: String): Flow<Character?>
    suspend fun createCharacter(characterId: String, character: Character)
    suspend fun updateMoney(characterId: String, amount: Int)
    suspend fun updateHealth(characterId: String, amount: Int)
    suspend fun updateHappiness(characterId: String, amount: Int)
    suspend fun updateAge(characterId: String, amount: Int)
    suspend fun updateIntelligence(characterId: String, amount: Int)
    suspend fun updateFitness(characterId: String, amount: Int)
    suspend fun updateSocial(characterId: String, amount: Int)
    suspend fun updateEducation(characterId: String, amount: Int)
    suspend fun updateCareer(characterId: String, career: String?)
    suspend fun addRelationship(characterId: String, relationship: String)
    suspend fun removeRelationship(characterId: String, relationship: String)
    suspend fun addAchievement(characterId: String, achievement: String)
    suspend fun addEvent(characterId: String, event: String)
    suspend fun updateJailTime(characterId: String, days: Int)
    suspend fun updateNotoriety(characterId: String, amount: Int)

    companion object {
        const val PLAYER_CHARACTER_ID = "player_character"
    }
}