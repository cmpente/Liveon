package com.altlifegames.domain.model

enum class RelationshipType {
    PET_OWNER,
    FAMILY,
    FRIEND,
    ROMANTIC,
    WORK
}

data class Relationship(
    val id: String,
    val characterId: Long,
    val relatedCharacterId: Long,
    val relationshipType: RelationshipType,
    val strength: Int = 50
)