package com.liveongames.domain.repository

import com.liveongames.domain.model.Character
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun getCharacter(characterId: String): Flow<Character?>
    suspend fun updateCharacter(character: Character)
    suspend fun deleteCharacter(characterId: String)
}
