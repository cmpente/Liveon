
package com.liveongames.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object LiveonMigrations {

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """                CREATE TABLE IF NOT EXISTS `term_state`(
                    `characterId` TEXT NOT NULL PRIMARY KEY,
                    `focus` INTEGER NOT NULL DEFAULT 100,
                    `streakDays` INTEGER NOT NULL DEFAULT 0,
                    `professorRelationship` INTEGER NOT NULL DEFAULT 0,
                    `weekIndex` INTEGER NOT NULL DEFAULT 1,
                    `coursePhase` TEXT NOT NULL DEFAULT 'EARLY'
                )"""
            )

            db.execSQL(
                """                CREATE TABLE IF NOT EXISTS `education_action_state`(
                    `characterId` TEXT NOT NULL,
                    `educationId` TEXT NOT NULL,
                    `actionId` TEXT NOT NULL,
                    `lastUsedAt` INTEGER NOT NULL DEFAULT 0,
                    `usedThisAge` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`characterId`, `educationId`, `actionId`)
                )"""
            )
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """                CREATE TABLE IF NOT EXISTS `educations_new`(
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `characterId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `level` TEXT NOT NULL,
                    `cost` INTEGER NOT NULL,
                    `durationMonths` INTEGER NOT NULL,
                    `requiredGpa` REAL NOT NULL DEFAULT 0.0,
                    `currentGpa` REAL NOT NULL DEFAULT 0.0,
                    `isActive` INTEGER NOT NULL,
                    `timestamp` INTEGER NOT NULL,
                    `completionDate` INTEGER,
                    `attendClassCount` INTEGER NOT NULL DEFAULT 0,
                    `doHomeworkCount` INTEGER NOT NULL DEFAULT 0,
                    `studyCount` INTEGER NOT NULL DEFAULT 0
                )"""
            )

            try {
                db.execSQL(
                    """                    INSERT INTO `educations_new`(
                        id, characterId, name, description, level, cost, durationMonths,
                        requiredGpa, currentGpa, isActive, timestamp, completionDate,
                        attendClassCount, doHomeworkCount, studyCount
                    )
                    SELECT
                        id, characterId, name, description, level, cost,
                        COALESCE(duration, durationMonths, 0),
                        CASE
                            WHEN (SELECT 1 FROM pragma_table_info('educations') WHERE name='requiredGpa') = 1
                                THEN COALESCE(requiredGpa, 0.0)
                            WHEN (SELECT 1 FROM pragma_table_info('educations') WHERE name='requiredNotoriety') = 1
                                THEN CAST(COALESCE(requiredNotoriety, 0) AS REAL)
                            ELSE 0.0
                        END,
                        COALESCE(currentGpa, 0.0),
                        isActive, timestamp, completionDate,
                        COALESCE(attendClassCount, 0),
                        COALESCE(doHomeworkCount, 0),
                        COALESCE(studyCount, 0)
                    FROM `educations`"""
                )
            } catch (e: Exception) { }

            try { db.execSQL("DROP TABLE IF EXISTS `educations`") } catch (_: Exception) {}
            db.execSQL("ALTER TABLE `educations_new` RENAME TO `educations`")
        }
    }
}
