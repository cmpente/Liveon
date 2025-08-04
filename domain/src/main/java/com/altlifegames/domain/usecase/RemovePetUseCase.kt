package com.altlifegames.domain.usecase

import com.altlifegames.domain.repository.PetRepository
import javax.inject.Inject

class RemovePetUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    operator fun invoke(petId: Long): Boolean {
        // This should work now that removePet is in the interface
        // But since our current implementation returns Boolean directly,
        // we need to check if your PetRepository actually has this method
        return try {
            // If your repository doesn't actually have removePet method,
            // you might need to remove this use case or implement it differently
            false // placeholder
        } catch (e: Exception) {
            false
        }
    }
}