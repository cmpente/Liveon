// app/src/main/java/com/liveongames/liveon/viewmodel/EducationViewModel.kt
package com.liveongames.liveon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.domain.model.Education
import com.liveongames.domain.repository.EducationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EducationViewModel @Inject constructor(
    private val educationRepository: EducationRepository
) : ViewModel() {

    private val _availableEducation = MutableStateFlow<List<Education>>(emptyList())
    val availableEducation: StateFlow<List<Education>> = _availableEducation.asStateFlow()

    private val _playerGPA = MutableStateFlow(2.5)
    val playerGPA: StateFlow<Double> = _playerGPA.asStateFlow()

    private val _currentEducation = MutableStateFlow("high_school")
    val currentEducation: StateFlow<String> = _currentEducation.asStateFlow()

    init {
        loadAvailableEducation()
    }

    private fun loadAvailableEducation() {
        viewModelScope.launch {
            educationRepository.getAvailableEducation()
                .catch { e ->
                    // Handle error
                }
                .collect { educations ->
                    _availableEducation.value = educations
                }
        }
    }

    fun enrollInEducation(educationId: String) {
        viewModelScope.launch {
            try {
                val education = educationRepository.getEducationById(educationId)
                if (education != null) {
                    educationRepository.enrollInEducation(education)
                    // Update current education
                    _currentEducation.value = education.id
                    // Deduct cost if applicable
                    if (education.cost > 0) {
                        // Handle cost deduction through player repository
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun completeEducation(educationId: String) {
        viewModelScope.launch {
            try {
                educationRepository.completeEducation(educationId)
                // Update GPA based on completion
                updateGPAAfterCompletion()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun updateGPAAfterCompletion() {
        viewModelScope.launch {
            // Calculate new GPA based on education completed
            // This would typically involve more complex logic
            _playerGPA.value = (_playerGPA.value + 0.2).coerceAtMost(4.0)
        }
    }
}