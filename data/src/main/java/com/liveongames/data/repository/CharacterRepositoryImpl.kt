// app/src/main/java/com/liveongames/data/repository/CharacterRepositoryImpl.kt
package com.liveongames.data.repository

import com.liveongames.data.db.dao.CharacterDao
import com.liveongames.domain.model.Character
import com.liveongames.domain.repository.CharacterRepository  // Make sure this is CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CharacterRepositoryImpl @Inject constructor(
    private val characterDao: CharacterDao
) : CharacterRepository {  // Implements CharacterRepository

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

    override suspend fun updateCharacter(character: Character) {
        val entity = com.liveongames.data.db.entity.CharacterEntity(
            id = character.id,
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

    override suspend fun deleteCharacter(characterId: String) {
        characterDao.deleteCharacter(characterId)
    }
}