package com.altlifegames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey val id: Long,
    val characterId: Long,
    val name: String,
    val type: String,
    val happiness: Int,
    val health: Int
)