package com.altlifegames.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String,
    @TypeConverters(EventChoiceConverter::class)
    val choices: List<EventChoice>
)

data class EventChoice(
    val text: String,
    val result: EventResult
)

data class EventResult(
    val statChanges: StatChange,
    val nextEventId: Long? = null,
    val unlockAchievementId: Long? = null
)

data class StatChange(
    val health: Int = 0,
    val happiness: Int = 0,
    val smarts: Int = 0,
    val looks: Int = 0,
    val money: Long = 0,
    val reputation: Int = 0
)
