package com.liveongames.data.repository

import com.liveongames.domain.model.Relationship
import com.liveongames.domain.repository.RelationshipRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class RelationshipRepositoryImpl @Inject constructor() : RelationshipRepository {

    private val relationships = MutableStateFlow<Map<String, List<Relationship>>>(emptyMap())

    override fun getRelationships(characterId: String): Flow<List<Relationship>> {
        return MutableStateFlow(relationships.value[characterId] ?: emptyList())
    }

    override suspend fun addRelationship(relationship: Relationship) {
        val currentRelationships = relationships.value.toMutableMap()
        val characterRelationships = currentRelationships.getOrDefault(relationship.characterId, mutableListOf()).toMutableList()
        characterRelationships.add(relationship)
        currentRelationships[relationship.characterId] = characterRelationships
        relationships.value = currentRelationships
    }

    override suspend fun updateRelationship(relationship: Relationship) {
        val currentRelationships = relationships.value.toMutableMap()
        val characterRelationships = currentRelationships.getOrDefault(relationship.characterId, mutableListOf()).toMutableList()
        val index = characterRelationships.indexOfFirst { it.id == relationship.id }
        if (index != -1) {
            characterRelationships[index] = relationship
            currentRelationships[relationship.characterId] = characterRelationships
            relationships.value = currentRelationships
        }
    }

    override suspend fun removeRelationship(relationshipId: String) {
        val currentRelationships = relationships.value.toMutableMap()
        val updatedRelationships = currentRelationships.mapValues { entry ->
            entry.value.filter { it.id != relationshipId }
        }
        relationships.value = updatedRelationships
    }
}