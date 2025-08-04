package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Character

interface CharacterRepository {
    suspend fun getCharacter(id: Long): Character?
    suspend fun updateCharacter(character: Character)
    suspend fun createCharacter(character: Character): Character
}