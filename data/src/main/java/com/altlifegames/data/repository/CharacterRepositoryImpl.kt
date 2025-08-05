package com.altlifegames.data.repository

import com.altlifegames.domain.model.Character
import com.altlifegames.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class CharacterRepositoryImpl @Inject constructor() : CharacterRepository {

    private val characters = MutableStateFlow<Map<String, Character>>(emptyMap())

    override fun getCharacter(characterId: String): Flow<Character?> {
        return MutableStateFlow(characters.value[characterId])
    }

    override suspend fun updateCharacter(character: Character) {
        val currentCharacters = characters.value.toMutableMap()
        currentCharacters[character.id] = character
        characters.value = currentCharacters
    }

    override suspend fun deleteCharacter(characterId: String) {
        val currentCharacters = characters.value.toMutableMap()
        currentCharacters.remove(characterId)
        characters.value = currentCharacters
    }
}