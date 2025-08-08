// app/src/main/java/com/liveongames/data/repository/PlayerRepositoryImpl.kt
package com.liveongames.data.repository

import com.liveongames.data.db.dao.CharacterDao
import com.liveongames.domain.model.Character
import com.liveongames.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlayerRepositoryImpl @Inject constructor(
    private val characterDao: CharacterDao
) : PlayerRepository {  // Make sure it implements PlayerRepository, not CharacterRepository

    companion object {
        private const val PLAYER_CHARACTER_ID = "player_character"
    }

    override fun getCharacter(characterId: String): Flow<Character?> {
        return characterDao.getCharacter(characterId).map { entity ->
            entity?.let {
                Character(
                    id = it.id,
                    name = it.name,
                    age = it.age,
                    health = it.health,
                    happiness = it.happiness,
                    money = it.money,
                    intelligence = it.intelligence,
                    fitness = it.fitness,
                    social = it.social,
                    education = it.education,
                    career = it.career,
                    relationships = it.relationships?.split(",")?.filter { r -> r.isNotBlank() }?.toList() ?: emptyList(),
                    achievements = it.achievements?.split(",")?.filter { a -> a.isNotBlank() }?.toList() ?: emptyList(),
                    events = it.events?.split(",")?.filter { e -> e.isNotBlank() }?.toList() ?: emptyList(),
                    jailTime = it.jailTime,
                    notoriety = it.notoriety
                )
            }
        }
    }

    override suspend fun createCharacter(characterId: String, character: Character) {
        val entity = com.liveongames.data.db.entity.CharacterEntity(
            id = characterId,
            name = character.name,
            age = character.age,
            health = character.health,
            happiness = character.happiness,
            money = character.money,
            intelligence = character.intelligence,
            fitness = character.fitness,
            social = character.social,
            education = character.education,
            career = character.career,
            relationships = if (character.relationships.isNotEmpty()) character.relationships.joinToString(",") else null,
            achievements = if (character.achievements.isNotEmpty()) character.achievements.joinToString(",") else null,
            events = if (character.events.isNotEmpty()) character.events.joinToString(",") else null,
            jailTime = character.jailTime,
            notoriety = character.notoriety,
            scenarioId = "default",
            lastPlayed = System.currentTimeMillis()
        )
        characterDao.insertCharacter(entity)
    }

    override suspend fun updateMoney(characterId: String, amount: Int) {
        characterDao.updateMoney(characterId, amount)
    }

    override suspend fun updateHealth(characterId: String, amount: Int) {
        characterDao.updateHealth(characterId, amount)
    }

    override suspend fun updateHappiness(characterId: String, amount: Int) {
        characterDao.updateHappiness(characterId, amount)
    }

    override suspend fun updateAge(characterId: String, amount: Int) {
        characterDao.updateAge(characterId, amount)
    }

    override suspend fun updateIntelligence(characterId: String, amount: Int) {
        characterDao.updateIntelligence(characterId, amount)
    }

    override suspend fun updateFitness(characterId: String, amount: Int) {
        characterDao.updateFitness(characterId, amount)
    }

    override suspend fun updateSocial(characterId: String, amount: Int) {
        characterDao.updateSocial(characterId, amount)
    }

    override suspend fun updateEducation(characterId: String, amount: Int) {
        characterDao.updateEducation(characterId, amount)
    }

    override suspend fun updateCareer(characterId: String, career: String?) {
        characterDao.updateCareer(characterId, career)
    }

    override suspend fun addRelationship(characterId: String, relationship: String) {
        characterDao.addRelationship(characterId, relationship)
    }

    override suspend fun removeRelationship(characterId: String, relationship: String) {
        characterDao.removeRelationship(characterId, relationship)
    }

    override suspend fun addAchievement(characterId: String, achievement: String) {
        characterDao.addAchievement(characterId, achievement)
    }

    override suspend fun addEvent(characterId: String, event: String) {
        characterDao.addEvent(characterId, event)
    }

    override suspend fun updateJailTime(characterId: String, days: Int) {
        characterDao.updateJailTime(characterId, days)
    }

    override suspend fun updateNotoriety(characterId: String, amount: Int) {
        characterDao.updateNotoriety(characterId, amount)
    }
}