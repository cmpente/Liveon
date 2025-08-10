package com.liveongames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "term_state")
data class TermStateEntity(
    @PrimaryKey val characterId: String = "player_character",
    val focus: Int = 100,
    val streakDays: Int = 0,
    val professorRelationship: Int = 0,
    val weekIndex: Int = 1,
    val coursePhase: String = "EARLY"
)
