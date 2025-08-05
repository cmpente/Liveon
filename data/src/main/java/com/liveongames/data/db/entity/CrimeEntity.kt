package com.liveongames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crimes")
data class CrimeEntity(
    @PrimaryKey val id: String,
    val characterId: String,
    val crimeType: String,
    val year: Int,
    val sentence: Int,
    val fine: Int
)