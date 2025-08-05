package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.Pet
import com.altlifegames.domain.repository.PetRepository
import javax.inject.Inject

class AdoptPetUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(pet: Pet) {
        petRepository.addPet(pet)
    }
}