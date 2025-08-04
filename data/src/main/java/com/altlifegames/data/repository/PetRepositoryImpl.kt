package com.altlifegames.data.repository

import com.altlifegames.domain.model.Pet
import com.altlifegames.domain.repository.PetRepository
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor() : PetRepository {
    
    private val pets = listOf(
        Pet(1, "Buddy", "Dog", 1, 
            com.altlifegames.domain.model.Stat(85, 100),
            com.altlifegames.domain.model.Stat(90, 100)),
        Pet(2, "Whiskers", "Cat", 2,
            com.altlifegames.domain.model.Stat(75, 100),
            com.altlifegames.domain.model.Stat(80, 100)),
        Pet(3, "Nemo", "Fish", 1,
            com.altlifegames.domain.model.Stat(95, 100),
            com.altlifegames.domain.model.Stat(70, 100))
    )
    
    override fun getRandomPet(): Pet {
        return pets.random()
    }
    
    override fun getAllPets(): List<Pet> {
        return pets
    }
    
    override fun getPets(): List<Pet> {
        return pets
    }
    
    override fun getPetById(id: Long): Pet? {
        return pets.find { it.id == id }
    }
    
    override fun getPetByType(type: String): Pet? {
        return pets.find { it.petType.equals(type, ignoreCase = true) }
    }
    
    override fun removePet(petId: Long): Boolean {
        // Since we're using an immutable list, we can't actually remove
        // This is a placeholder implementation
        return false
    }
}