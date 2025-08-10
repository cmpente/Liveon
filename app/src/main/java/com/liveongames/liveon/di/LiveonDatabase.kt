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
        EducationActionStateEntity::class,
        EducationEntity::class,
        EventEntity::class,
        PetEntity::class,
        RelationshipEntity::class,
        SaveSlotEntity::class,
        ScenarioEntity::class,
        TermStateEntity::class,
        UnlockedAchievementEntity::class,
    ],
    version = 6,
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
    abstract fun educationDao(): EducationDao
    abstract fun educationActionStateDao(): EducationActionStateDao
    abstract fun termStateDao(): TermStateDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migration code for version 1 to 2
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create educations table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `educations` (
                        `id` TEXT NOT NULL,
                        `characterId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `level` TEXT NOT NULL,
                        `cost` INTEGER NOT NULL,
                        `duration` INTEGER NOT NULL,
                        `requiredNotoriety` INTEGER NOT NULL,
                        `completionDate` INTEGER,
                        `isActive` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add any additional migrations for version 4 to 5
            }
        }
    }
}