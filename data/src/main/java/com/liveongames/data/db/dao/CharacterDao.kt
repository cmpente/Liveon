// app/src/main/java/com/liveongames/data/db/dao/CharacterDao.kt
package com.liveongames.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liveongames.data.db.entity.CharacterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterById(id: String): CharacterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

    @Query("DELETE FROM characters WHERE id = :id")
    suspend fun deleteCharacter(id: String)

    // Add missing methods for crime system
    @Query("SELECT * FROM characters WHERE id = :characterId")
    fun getCharacter(characterId: String): Flow<CharacterEntity?>

    @Query("UPDATE characters SET money = money + :amount WHERE id = :characterId")
    suspend fun updateMoney(characterId: String, amount: Int)

    @Query("UPDATE characters SET health = health + :amount WHERE id = :characterId")
    suspend fun updateHealth(characterId: String, amount: Int)

    @Query("UPDATE characters SET happiness = happiness + :amount WHERE id = :characterId")
    suspend fun updateHappiness(characterId: String, amount: Int)

    @Query("UPDATE characters SET age = age + :amount WHERE id = :characterId")
    suspend fun updateAge(characterId: String, amount: Int)

    @Query("UPDATE characters SET intelligence = intelligence + :amount WHERE id = :characterId")
    suspend fun updateIntelligence(characterId: String, amount: Int)

    @Query("UPDATE characters SET fitness = fitness + :amount WHERE id = :characterId")
    suspend fun updateFitness(characterId: String, amount: Int)

    @Query("UPDATE characters SET social = social + :amount WHERE id = :characterId")
    suspend fun updateSocial(characterId: String, amount: Int)

    @Query("UPDATE characters SET education = education + :amount WHERE id = :characterId")
    suspend fun updateEducation(characterId: String, amount: Int)

    @Query("UPDATE characters SET career = :career WHERE id = :characterId")
    suspend fun updateCareer(characterId: String, career: String?)

    @Query("UPDATE characters SET relationships = CASE WHEN relationships IS NULL OR relationships = '' THEN :relationship ELSE relationships || ',' || :relationship END WHERE id = :characterId")
    suspend fun addRelationship(characterId: String, relationship: String)

    @Query("UPDATE characters SET relationships = REPLACE(REPLACE(REPLACE(relationships, :relationship || ',', ''), ',' || :relationship, ''), :relationship, '') WHERE id = :characterId")
    suspend fun removeRelationship(characterId: String, relationship: String)

    @Query("UPDATE characters SET achievements = CASE WHEN achievements IS NULL OR achievements = '' THEN :achievement ELSE achievements || ',' || :achievement END WHERE id = :characterId")
    suspend fun addAchievement(characterId: String, achievement: String)

    @Query("UPDATE characters SET events = CASE WHEN events IS NULL OR events = '' THEN :event ELSE events || ',' || :event END WHERE id = :characterId")
    suspend fun addEvent(characterId: String, event: String)

    @Query("UPDATE characters SET jailTime = jailTime + :days WHERE id = :characterId")
    suspend fun updateJailTime(characterId: String, days: Int)

    @Query("UPDATE characters SET notoriety = notoriety + :amount WHERE id = :characterId")
    suspend fun updateNotoriety(characterId: String, amount: Int)
}