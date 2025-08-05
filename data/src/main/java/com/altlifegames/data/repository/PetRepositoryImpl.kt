package com.altlifegames.data.repository

import com.altlifegames.domain.model.Pet
import com.altlifegames.domain.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor() : PetRepository {

    private val pets = MutableStateFlow<List<Pet>>(emptyList())

    override fun getPets(): Flow<List<Pet>> {
        return pets
    }

    override suspend fun addPet(pet: Pet) {
        val currentPets = pets.value.toMutableList()
        currentPets.add(pet)
        pets.value = currentPets
    }

    override suspend fun removePet(petId: String) {
        val currentPets = pets.value.toMutableList()
        val index = currentPets.indexOfFirst { it.id == petId }
        if (index != -1) {
            currentPets.removeAt(index)
            pets.value = currentPets
        }
    }

    override suspend fun updatePet(pet: Pet) {
        val currentPets = pets.value.toMutableList()
        val index = currentPets.indexOfFirst { it.id == pet.id }
        if (index != -1) {
            currentPets[index] = pet
            pets.value = currentPets
        }
    }

    override suspend fun getPetById(petId: String): Pet? {
        return pets.value.find { it.id == petId }
    }
}