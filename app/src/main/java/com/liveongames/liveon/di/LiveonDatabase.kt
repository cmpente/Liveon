// app/src/main/java/com/liveongames/data/db/LiveonDatabase.kt
package com.liveongames.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.liveongames.data.db.dao.*
import com.liveongames.data.db.entity.*

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
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
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
                // Migration code
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
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
            }
        }
    }
}