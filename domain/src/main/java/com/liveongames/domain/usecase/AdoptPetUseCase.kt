package com.liveongames.domain.usecase

import com.liveongames.domain.model.Pet
import com.liveongames.domain.repository.PetRepository
import javax.inject.Inject

class AdoptPetUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(pet: Pet) {
        petRepository.addPet(pet)
    }
}