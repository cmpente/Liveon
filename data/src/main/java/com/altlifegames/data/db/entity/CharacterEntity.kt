package com.altlifegames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val age: Int,
    val health: Int,
    val happiness: Int,
    val intelligence: Int,
    val money: Int,
    val social: Int,
    val scenarioId: String,
    val lastPlayed: Long
)