// app/src/main/java/com/liveongames/liveon/viewmodel/EducationViewModel.kt
package com.liveongames.liveon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.domain.model.Education
import com.liveongames.domain.model.EducationLevel
import com.liveongames.domain.repository.EducationRepository
import com.liveongames.domain.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class EducationViewModel @Inject constructor(
    private val educationRepository: EducationRepository,
    private val characterRepository: CharacterRepository
) : ViewModel() {

    val educations = educationRepository.getEducations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun enrollInEducation(education: Education) {
        viewModelScope.launch {
            try {
                characterRepository.getCharacter("player_character")
                    .firstOrNull()
                    ?.let { character ->
                        // Convert education int to GPA value (assuming it's stored as an integer)
                        val playerGPAValue = if (character.education > 0) {
                            (character.education / 100.0) // Convert int like 320 to GPA 3.20
                        } else {
                            0.0
                        }

                        // Check if player meets GPA requirements
                        if (playerGPAValue >= education.requiredGPA) {
                            // Check if player is already enrolled in a non-certification program
                            val activeNonCertEducation = educations.value.find {
                                it.isActive && it.level != EducationLevel.CERTIFICATION
                            }

                            if (activeNonCertEducation == null || education.level == EducationLevel.CERTIFICATION) {
                                // Deduct cost from character's money
                                if (character.money >= education.cost) {
                                    val updatedCharacter = character.copy(
                                        money = character.money - education.cost
                                    )
                                    characterRepository.updateCharacter(updatedCharacter)

                                    // Add education to player's active programs
                                    val newEducation = education.copy(
                                        isActive = true,
                                        timestamp = System.currentTimeMillis(),
                                        currentGPA = playerGPAValue // Initialize with player's current GPA
                                    )
                                    educationRepository.addEducation(newEducation)
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                // In a real app, you'd want to handle this with proper error reporting
                e.printStackTrace()
            }
        }
    }

    fun completeEducation(education: Education) {
        viewModelScope.launch {
            try {
                // Mark education as completed
                val completedEducation = education.copy(
                    completionDate = System.currentTimeMillis(),
                    isActive = false
                )
                educationRepository.updateEducation(completedEducation)

                // Update character's overall education level/GPA
                updateCharacterEducation(education)
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    private suspend fun updateCharacterEducation(education: Education) {
        try {
            characterRepository.getCharacter("player_character")
                .firstOrNull()
                ?.let { character ->
                    // Convert GPA back to integer representation (e.g., 3.25 -> 325)
                    val educationValue = (education.currentGPA * 100).toInt()

                    // Update character's education stats
                    val updatedCharacter = character.copy(
                        education = educationValue
                    )
                    characterRepository.updateCharacter(updatedCharacter)
                }
        } catch (e: Exception) {
            // Handle error
            e.printStackTrace()
        }
    }

    // Education activities with simple decreasing returns based on current GPA
    fun attendClass(educationId: String) {
        viewModelScope.launch {
            try {
                educationRepository.getEducationById(educationId)?.let { education ->
                    if (education.isActive) {
                        // Decreasing returns based on current GPA (higher GPA = smaller gains)
                        val baseGain = 0.1
                        val reductionFactor = education.currentGPA / 10.0 // Reduce gains as GPA increases
                        val gpaIncrease = max(0.01, baseGain * (1 - reductionFactor))
                        val improvedGPA = (education.currentGPA + gpaIncrease).coerceAtMost(4.0)

                        val updatedEducation = education.copy(
                            currentGPA = improvedGPA
                        )
                        educationRepository.updateEducation(updatedEducation)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun doHomework(educationId: String) {
        viewModelScope.launch {
            try {
                educationRepository.getEducationById(educationId)?.let { education ->
                    if (education.isActive) {
                        // Decreasing returns based on current GPA
                        val baseGain = 0.08
                        val reductionFactor = education.currentGPA / 10.0
                        val gpaIncrease = max(0.01, baseGain * (1 - reductionFactor))
                        val improvedGPA = (education.currentGPA + gpaIncrease).coerceAtMost(4.0)

                        val updatedEducation = education.copy(
                            currentGPA = improvedGPA
                        )
                        educationRepository.updateEducation(updatedEducation)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun study(educationId: String) {
        viewModelScope.launch {
            try {
                educationRepository.getEducationById(educationId)?.let { education ->
                    if (education.isActive) {
                        // Decreasing returns based on current GPA
                        val baseGain = 0.06
                        val reductionFactor = education.currentGPA / 10.0
                        val gpaIncrease = max(0.01, baseGain * (1 - reductionFactor))
                        val improvedGPA = (education.currentGPA + gpaIncrease).coerceAtMost(4.0)

                        val updatedEducation = education.copy(
                            currentGPA = improvedGPA
                        )
                        educationRepository.updateEducation(updatedEducation)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}