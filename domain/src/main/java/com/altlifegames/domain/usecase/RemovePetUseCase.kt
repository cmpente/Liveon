package com.altlifegames.domain.usecase

import com.altlifegames.domain.repository.PetRepository
import javax.inject.Inject

class RemovePetUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(petId: String) {
        petRepository.removePet(petId)
    }
}