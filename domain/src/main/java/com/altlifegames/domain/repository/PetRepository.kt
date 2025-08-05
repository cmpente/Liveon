package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Pet
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun getPets(): Flow<List<Pet>>
    suspend fun addPet(pet: Pet)
    suspend fun removePet(petId: String)
    suspend fun updatePet(pet: Pet)
    suspend fun getPetById(petId: String): Pet?
}