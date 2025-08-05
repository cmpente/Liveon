package com.altlifegames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "relationships")
data class RelationshipEntity(
    @PrimaryKey val id: String,
    val characterId: String,
    val relatedCharacterId: String,
    val relationshipType: String,
    val strength: Int
)