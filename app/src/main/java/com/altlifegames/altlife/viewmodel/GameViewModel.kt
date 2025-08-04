package com.altlifegames.altlife.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// For now, let's create a simple Character data class
data class Character(
    val id: Long = 0L,
    val firstName: String = "",
    val lastName: String = "",
    val age: Int = 0,
    val health: Int = 50,
    val happiness: Int = 50,
    val intelligence: Int = 50,
    val money: Int = 1000
)

@HiltViewModel
class GameViewModel @Inject constructor() : ViewModel() {
    
    private val _currentCharacter = MutableStateFlow<Character?>(null)
    val currentCharacter: StateFlow<Character?> = _currentCharacter
    
    init {
        createNewCharacter()
    }
    
    fun createNewCharacter() {
        // Simple character creation for now
        val newCharacter = Character(
            id = System.currentTimeMillis(),
            firstName = "Alex",
            lastName = "Adams",
            age = 0,
            health = 75,
            happiness = 60,
            intelligence = 55,
            money = 1000
        )
        _currentCharacter.value = newCharacter
    }
    
    fun advanceYear() {
        viewModelScope.launch {
            _currentCharacter.value?.let { character ->
                // Apply stat changes for aging
                val updatedCharacter = character.copy(
                    age = character.age + 1,
                    health = (character.health - 2).coerceAtLeast(0),
                    happiness = (character.happiness + (0..10).random() - 5).coerceIn(0, 100),
                    intelligence = (character.intelligence + 1).coerceAtMost(100),
                    money = character.money + 1000 // Yearly income
                )
                _currentCharacter.value = updatedCharacter
            }
        }
    }
}