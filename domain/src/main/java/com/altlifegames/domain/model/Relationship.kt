package com.altlifegames.domain.model

data class Relationship(
    val id: String,
    val characterId: String,
    val relatedCharacterId: String,
    val type: RelationshipType,
    val startDate: Long,
    val strength: Int = 50,
    val isRomantic: Boolean = false,
    val isFamily: Boolean = false
)

enum class RelationshipType {
    FRIEND,
    FAMILY,
    ROMANTIC,
    WORK,
    ACQUAINTANCE,
    ENEMY
}