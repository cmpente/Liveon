package com.altlifegames.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.altlifegames.data.db.dao.AssetDao
import com.altlifegames.data.db.dao.CareerDao
import com.altlifegames.data.db.dao.CharacterDao
import com.altlifegames.data.db.dao.CrimeDao
import com.altlifegames.data.db.dao.PetDao
import com.altlifegames.data.db.dao.RelationshipDao
import com.altlifegames.data.db.dao.SaveSlotDao
import com.altlifegames.data.db.dao.UnlockedAchievementDao
import com.altlifegames.data.db.entity.AssetEntity
import com.altlifegames.data.db.entity.CareerEntity
import com.altlifegames.data.db.entity.CharacterEntity
import com.altlifegames.data.db.entity.CrimeEntity
import com.altlifegames.data.db.entity.PetEntity
import com.altlifegames.data.db.entity.RelationshipEntity
import com.altlifegames.data.db.entity.SaveSlotEntity
import com.altlifegames.data.db.entity.UnlockedAchievementEntity

@Database(
    entities = [
        CharacterEntity::class,
        RelationshipEntity::class,
        AssetEntity::class,
        CareerEntity::class,
        UnlockedAchievementEntity::class,
        PetEntity::class,
        CrimeEntity::class,
        SaveSlotEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AltLifeDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun relationshipDao(): RelationshipDao
    abstract fun assetDao(): AssetDao
    abstract fun careerDao(): CareerDao
    abstract fun unlockedAchievementDao(): UnlockedAchievementDao
    abstract fun petDao(): PetDao
    abstract fun crimeDao(): CrimeDao
    abstract fun saveSlotDao(): SaveSlotDao
    
    companion object {
        const val DATABASE_NAME = "altlife.db"
    }
}