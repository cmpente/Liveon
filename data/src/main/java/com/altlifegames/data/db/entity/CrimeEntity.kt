package com.altlifegames.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "crimes",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("characterId")] // This fixes the warning
)
data class CrimeEntity(
    @PrimaryKey val id: String,
    val characterId: Long,
    val crimeType: String,
    val description: String,
    val severity: Int,
    val dateCommitted: Long,
    val isSolved: Boolean,
    val sentenceYears: Int
)