package com.altlifegames.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.altlifegames.core.model.*

@Database(
    entities = [Character::class, Achievement::class, Event::class, Scenario::class],
    version = 1
)
@TypeConverters(
    RelationshipConverter::class, AchievementConverter::class, EventChoiceConverter::class, EventConverter::class
)
abstract class AltLifeDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun achievementDao(): AchievementDao
    abstract fun eventDao(): EventDao
    abstract fun scenarioDao(): ScenarioDao
}
