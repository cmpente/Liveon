package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Pet

interface PetRepository {
    fun getRandomPet(): Pet
    fun getPetByType(petType: String): Pet?
    fun getAllPets(): List<Pet>
    fun getPetById(id: Long): Pet?
    fun getPets(): List<Pet>
    fun removePet(petId: Long): Boolean  // Add this method
}