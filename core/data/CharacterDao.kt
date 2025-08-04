package com.altlifegames.core.data

import androidx.room.*
import com.altlifegames.core.model.Character

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacter(id: Long): Character?

    @Query("SELECT * FROM characters")
    suspend fun getAllCharacters(): List<Character>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: Character): Long

    @Update
    suspend fun updateCharacter(character: Character)

    @Delete
    suspend fun deleteCharacter(character: Character)
}
