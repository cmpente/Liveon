package com.liveongames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey val id: String,
    val characterId: String,
    val name: String,
    val type: String,
    val happiness: Int,
    val health: Int
)