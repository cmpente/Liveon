// app/src/main/java/com/liveongames/liveon/viewmodel/EducationViewModel.kt
package com.liveongames.liveon.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.domain.repository.EducationRepository
import com.liveongames.data.assets.education.EducationAssetLoader
import com.liveongames.data.model.education.EducationActionDef
import com.liveongames.domain.model.EducationProgram
import com.liveongames.domain.model.Enrollment
import com.liveongames.domain.model.EducationActionResult
import com.liveongames.domain.model.EduTier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EducationViewModel @Inject constructor(
    app: Application,
    private val repo: EducationRepository,
    private val eduAssets: EducationAssetLoader
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(EducationUiState())
    val uiState: StateFlow<EducationUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            // Explicitly cast or map the result to the correct type
            val programs: List<EducationProgram> = repo.getPrograms()
            val actions: List<EducationActionDef> = repo.getActions().map { it as EducationActionDef }
            val enrollment = repo.getEnrollment()

            _uiState.update {
                it.copy(
                    loading = false,
                    programs = programs,
                    actions = actions,
                    enrollment = enrollment
                )
            }
        } catch (e: Exception) {
            Log.e("EducationVM", "Error loading education data", e)
            _uiState.update {
                it.copy(loading = false, message = "Failed to load education data.")
            }
        }
    }

    fun handleEvent(event: EducationEvent) {
        when (event) {
            is EducationEvent.Enroll -> enroll(event.programId)
            is EducationEvent.DoAction -> performAction(event.actionId, event.choiceId, event.multiplier)
            EducationEvent.DismissMessage -> _uiState.update { it.copy(message = null) }
            EducationEvent.ShowGpaInfo -> _uiState.update { it.copy(showGpaInfo = true) }
            EducationEvent.HideGpaInfo -> _uiState.update { it.copy(showGpaInfo = false) }
            is EducationEvent.ChooseFailOrRetake -> handleFailOrRetake(event.retake)
        }
    }

    private fun handleFailOrRetake(retake: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val currentProgramId = _uiState.value.enrollment?.programId ?: return@launch

        try {
            if (retake) {
                val resetEnrollment = repo.enroll(currentProgramId)
                _uiState.update {
                    it.copy(enrollment = resetEnrollment, showFailOrRetake = false, message = "Program restarted. Progress reset.")
                }
            } else {
                repo.resetEducation()
                _uiState.update {
                    it.copy(enrollment = null, showFailOrRetake = false, message = "You have failed the program and dropped out.")
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(message = "Operation failed: ${e.message}", showFailOrRetake = false)
            }
        }
    }

    private fun enroll(programId: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val newEnrollment = repo.enroll(programId)
            _uiState.update {
                it.copy(enrollment = newEnrollment, message = "Successfully enrolled in the program!")
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(message = "Enrollment failed: ${e.message}")
            }
        }
    }

    private fun performAction(actionId: String, choiceId: String, multiplier: Double) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val currentEnrollment = _uiState.value.enrollment
                ?: throw IllegalStateException("No active enrollment")

            val actionResult = repo.applyAction(actionId, choiceId, multiplier)
            val updatedEnrollment = actionResult.enrollment

            val course = _uiState.value.programs.firstOrNull { it.id == updatedEnrollment.programId }

            if (course != null) {
                if (updatedEnrollment.progressPct >= 100) {
                    if (updatedEnrollment.gpa >= course.minGpa) {
                        repo.resetEducation()
                        _uiState.update {
                            it.copy(
                                enrollment = null,
                                message = "Congratulations! You graduated from ${course.title} with a GPA of ${"%.2f".format(updatedEnrollment.gpa)}!"
                            )
                        }
                    } else {
                        if (course.tier >= EduTier.HIGH) {
                            _uiState.update {
                                it.copy(showFailOrRetake = true, enrollment = updatedEnrollment, message = "Program complete, but GPA is too low for graduation.")
                            }
                        } else {
                            repo.resetEducation()
                            _uiState.update {
                                it.copy(enrollment = null, message = "Program ended. Required GPA not met. You must repeat.")
                            }
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(enrollment = updatedEnrollment)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("EducationVM", "Error performing action $actionId", e)
            _uiState.update {
                it.copy(message = "Action failed: ${e.message}")
            }
        }
    }

    // --- HELPER FOR UI ---
    fun isActionEligible(action: EducationActionDef, enrollment: Enrollment?): Boolean {
        val e = enrollment ?: return false

        // Explicit string match avoids type inference problems on `contains`
        val isTierEligible = action.tiers.any { it.name == e.tier.name }
        if (!isTierEligible) return false

        // Smart cast problem solved by explicitly checking for nullability
        val minGpaMet = action.minGpa?.let { e.gpa >= it } ?: true
        val maxGpaMet = action.maxGpa?.let { e.gpa <= it } ?: true
        val isGpaEligible = minGpaMet && maxGpaMet

        if (!isGpaEligible) return false

        val isOffCooldown = isActionOffCooldown(action, e)

        return isOffCooldown
    }

    private fun isActionOffCooldown(action: EducationActionDef, enrollment: Enrollment): Boolean {
        val lastActionTime = enrollment.lastActionAt ?: return true
        val cooldownMillis = action.cooldownMinutes * 60 * 1000L
        val timeSinceLastAction = System.currentTimeMillis() - lastActionTime
        return timeSinceLastAction >= cooldownMillis
    }

    fun onAgeUp() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val updatedEnrollment = repo.onAgeUp()
            _uiState.update {
                it.copy(enrollment = updatedEnrollment)
            }
            _uiState.update {
                it.copy(message = "Happy Birthday! Action limits have been reset for the year.")
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(message = "Age-up logic failed: ${e.message}")
            }
        }
    }

    fun forceRefresh() {
        loadData()
    }
}

data class EducationUiState(
    val loading: Boolean = true,
    val programs: List<EducationProgram> = emptyList(),
    val actions: List<EducationActionDef> = emptyList(),
    val enrollment: Enrollment? = null,
    val showGpaInfo: Boolean = false,
    val showFailOrRetake: Boolean = false,
    val message: String? = null
)

sealed interface EducationEvent {
    data class Enroll(val programId: String) : EducationEvent
    data class DoAction(val actionId: String, val choiceId: String, val multiplier: Double = 1.0) : EducationEvent
    data object DismissMessage : EducationEvent
    data object ShowGpaInfo : EducationEvent
    data object HideGpaInfo : EducationEvent
    data class ChooseFailOrRetake(val retake: Boolean) : EducationEvent
}