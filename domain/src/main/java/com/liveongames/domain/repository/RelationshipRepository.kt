package com.liveongames.domain.repository

import com.liveongames.domain.model.Relationship
import kotlinx.coroutines.flow.Flow

interface RelationshipRepository {
    fun getRelationships(characterId: String): Flow<List<Relationship>>
    suspend fun addRelationship(relationship: Relationship)
    suspend fun updateRelationship(relationship: Relationship)
    suspend fun removeRelationship(relationshipId: String)
}