// app/src/main/java/com/liveongames/data/repository/PetRepositoryImpl.kt
package com.liveongames.data.repository

import com.liveongames.data.db.dao.PetDao
import com.liveongames.domain.model.Pet
import com.liveongames.domain.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val petDao: PetDao
) : PetRepository {

    override fun getPets(): Flow<List<Pet>> {
        return petDao.getPetsForCharacter("default_character").map { petEntities ->
            petEntities.map { petEntity ->
                Pet(
                    id = petEntity.id,
                    name = petEntity.name,
                    type = petEntity.type,
                    happiness = petEntity.happiness,
                    cost = petEntity.cost
                )
            }
        }
    }

    override suspend fun addPet(pet: Pet) {
        val petEntity = com.liveongames.data.db.entity.PetEntity(
            id = pet.id,
            characterId = "default_character",
            name = pet.name,
            type = pet.type,
            happiness = pet.happiness,
            cost = pet.cost
        )
        petDao.insertPet(petEntity)
    }

    override suspend fun removePet(petId: String) {
        petDao.removePet(petId)
    }

    override suspend fun updatePet(pet: Pet) {
        val petEntity = com.liveongames.data.db.entity.PetEntity(
            id = pet.id,
            characterId = "default_character",
            name = pet.name,
            type = pet.type,
            happiness = pet.happiness,
            cost = pet.cost
        )
        petDao.insertPet(petEntity)
    }

    override suspend fun getPetById(petId: String): Pet? {
        // You'll need to add this method to your PetDao
        return null
    }
}