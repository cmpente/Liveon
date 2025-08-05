package com.liveongames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "save_slots")
data class SaveSlotEntity(
    @PrimaryKey val id: String,
    val characterName: String,
    val age: Int,
    val lastPlayed: Long,
    val scenarioId: String
)