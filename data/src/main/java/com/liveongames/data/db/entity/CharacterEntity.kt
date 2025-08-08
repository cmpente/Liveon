package com.liveongames.data.db.entity

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
    val scenarioId: String = "default",
    val lastPlayed: Long = System.currentTimeMillis(),
    val fitness: Int = 0,
    val education: Int = 0,
    val career: String? = null,
    val relationships: String? = null,
    val achievements: String? = null,
    val events: String? = null,
    val jailTime: Int = 0,
    val notoriety: Int = 0
)