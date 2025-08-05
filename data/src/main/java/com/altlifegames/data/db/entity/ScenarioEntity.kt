package com.altlifegames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scenarios")
data class ScenarioEntity(
    @PrimaryKey val id: String,
    val title: String,
    val name: String,
    val description: String,
    val startingAge: Int,
    val maxAge: Int,
    val startingHealth: Int,
    val startingHappiness: Int,
    val startingIntelligence: Int,
    val startingMoney: Int,
    val startingSocial: Int,
    val difficulty: String
)