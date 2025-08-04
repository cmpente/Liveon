package com.altlifegames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a character's career.
 */
@Entity(tableName = "careers")
data class CareerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val characterId: Long,
    val title: String,
    val company: String,
    val salary: Double,
    val level: Int,
    val experience: Int
)