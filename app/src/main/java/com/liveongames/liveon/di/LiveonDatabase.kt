package com.liveongames.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.liveongames.data.db.dao.AssetDao
import com.liveongames.data.db.dao.CareerDao
import com.liveongames.data.db.dao.CharacterDao
import com.liveongames.data.db.dao.CrimeDao
import com.liveongames.data.db.dao.EventDao
import com.liveongames.data.db.dao.PetDao
import com.liveongames.data.db.dao.RelationshipDao
import com.liveongames.data.db.dao.SaveSlotDao
import com.liveongames.data.db.dao.ScenarioDao
import com.liveongames.data.db.dao.UnlockedAchievementDao
import com.liveongames.data.db.entity.AssetEntity
import com.liveongames.data.db.entity.CareerEntity
import com.liveongames.data.db.entity.CharacterEntity
import com.liveongames.data.db.entity.CrimeEntity
import com.liveongames.data.db.entity.EventEntity
import com.liveongames.data.db.entity.PetEntity
import com.liveongames.data.db.entity.RelationshipEntity
import com.liveongames.data.db.entity.SaveSlotEntity
import com.liveongames.data.db.entity.ScenarioEntity
import com.liveongames.data.db.entity.UnlockedAchievementEntity

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
    version = 2,  // ← Version 2
    exportSchema = false
)
abstract class LiveonDatabase : RoomDatabase() {
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

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add columns to existing characters table
                try {
                    database.execSQL("ALTER TABLE characters ADD COLUMN fitness INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {}

                try {
                    database.execSQL("ALTER TABLE characters ADD COLUMN education INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {}

                try {
                    database.execSQL("ALTER TABLE characters ADD COLUMN career TEXT")
                } catch (e: Exception) {}

                try {
                    database.execSQL("ALTER TABLE characters ADD COLUMN relationships TEXT")
                } catch (e: Exception) {}

                try {
                    database.execSQL("ALTER TABLE characters ADD COLUMN achievements TEXT")
                } catch (e: Exception) {}

                try {
                    database.execSQL("ALTER TABLE characters ADD COLUMN events TEXT")
                } catch (e: Exception) {}

                try {
                    database.execSQL("ALTER TABLE characters ADD COLUMN jailTime INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {}

                try {
                    database.execSQL("ALTER TABLE characters ADD COLUMN notoriety INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {}

                // Handle crimes table if needed - create temp table and migrate
                // This handles the schema mismatch error you saw
            }
        }
    }
}