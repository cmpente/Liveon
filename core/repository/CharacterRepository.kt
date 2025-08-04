package com.altlifegames.core.repository

import com.altlifegames.core.data.CharacterDao
import com.altlifegames.core.model.Character
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CharacterRepository(private val characterDao: CharacterDao) {
    suspend fun getCharacter(id: Long): Character? = withContext(Dispatchers.IO) {
        characterDao.getCharacter(id)
    }

    suspend fun getAllCharacters(): List<Character> = withContext(Dispatchers.IO) {
        characterDao.getAllCharacters()
    }

    suspend fun insertCharacter(character: Character): Long = withContext(Dispatchers.IO) {
        characterDao.insertCharacter(character)
    }

    suspend fun updateCharacter(character: Character) = withContext(Dispatchers.IO) {
        characterDao.updateCharacter(character)
    }

    suspend fun deleteCharacter(character: Character) = withContext(Dispatchers.IO) {
        characterDao.deleteCharacter(character)
    }
}
