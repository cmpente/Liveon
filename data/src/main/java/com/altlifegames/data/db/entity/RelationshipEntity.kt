package com.altlifegames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "relationships")
data class RelationshipEntity(
    @PrimaryKey val id: String,
    val characterId: Long,
    val name: String,
    val relationshipType: String,
    val affection: Int,
    val trust: Int,
    val isRomantic: Boolean
)