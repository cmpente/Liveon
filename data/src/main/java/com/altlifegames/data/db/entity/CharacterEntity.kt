package com.altlifegames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val gender: String,
    val nationality: String,
    val age: Int = 0,
    val alive: Boolean = true,
    val statsJson: String = "{}",
    val traitsJson: String = "[]",
    val careerJson: String? = null,
    val assetsJson: String = "[]",
    val achievementsJson: String = "[]",
    val eventHistoryJson: String = "[]",
    val lastModified: Long = System.currentTimeMillis()
)