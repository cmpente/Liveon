package com.altlifegames.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.altlifegames.data.db.dao.AssetDao
import com.altlifegames.data.db.dao.CareerDao
import com.altlifegames.data.db.dao.CharacterDao
import com.altlifegames.data.db.dao.CrimeDao
import com.altlifegames.data.db.dao.EventDao
import com.altlifegames.data.db.dao.PetDao
import com.altlifegames.data.db.dao.RelationshipDao
import com.altlifegames.data.db.dao.SaveSlotDao
import com.altlifegames.data.db.dao.ScenarioDao
import com.altlifegames.data.db.dao.UnlockedAchievementDao
import com.altlifegames.data.db.entity.AssetEntity
import com.altlifegames.data.db.entity.CareerEntity
import com.altlifegames.data.db.entity.CharacterEntity
import com.altlifegames.data.db.entity.CrimeEntity
import com.altlifegames.data.db.entity.EventEntity
import com.altlifegames.data.db.entity.PetEntity
import com.altlifegames.data.db.entity.RelationshipEntity
import com.altlifegames.data.db.entity.SaveSlotEntity
import com.altlifegames.data.db.entity.ScenarioEntity
import com.altlifegames.data.db.entity.UnlockedAchievementEntity

@Database(
    entities = [
        AssetEntity::class,
        CareerEntity::class,
        CharacterEntity::class,
        CrimeEntity::class,
        EventEntity::class,
        PetEntity::class,
        RelationshipEntity::class,
        SaveSlotEntity::class,
        ScenarioEntity::class,
        UnlockedAchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AltLifeDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun careerDao(): CareerDao
    abstract fun characterDao(): CharacterDao
    abstract fun crimeDao(): CrimeDao
    abstract fun eventDao(): EventDao
    abstract fun petDao(): PetDao
    abstract fun relationshipDao(): RelationshipDao
    abstract fun saveSlotDao(): SaveSlotDao
    abstract fun scenarioDao(): ScenarioDao
    abstract fun unlockedAchievementDao(): UnlockedAchievementDao
}