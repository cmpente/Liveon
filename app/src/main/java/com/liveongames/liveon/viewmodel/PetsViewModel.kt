// app/src/main/java/com/liveongames/liveon/viewmodel/PetsViewModel.kt
package com.liveongames.liveon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.domain.model.Pet
import com.liveongames.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetsViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()

    init {
        // Monitor pets
        petRepository.getPets().onEach { petList ->
            _pets.value = petList
        }.launchIn(viewModelScope)
    }

    fun adoptPet(name: String) {
        viewModelScope.launch {
            // Create a pet with the exact parameters your model requires
            val pet = Pet(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                type = "Dog",           // Required parameter
                happiness = 50,         // Required parameter
                cost = 100              // Required parameter
            )
            petRepository.addPet(pet)
        }
    }

    fun removePet(petId: String) {
        viewModelScope.launch {
            petRepository.removePet(petId)
        }
    }
}