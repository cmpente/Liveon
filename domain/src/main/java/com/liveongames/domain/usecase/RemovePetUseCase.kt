package com.liveongames.domain.usecase

import com.liveongames.domain.repository.PetRepository
import javax.inject.Inject

class RemovePetUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(petId: String) {
        petRepository.removePet(petId)
    }
}