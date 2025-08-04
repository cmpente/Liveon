package com.altlifegames.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "scenarios")
data class Scenario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String,
    val startingCharacter: Character,
    @TypeConverters(EventConverter::class)
    val events: List<Event>
)
