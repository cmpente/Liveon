package com.altlifegames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "careers")
data class CareerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val requiredEducation: String,
    val salary: Int,
    val growthRate: Double
)