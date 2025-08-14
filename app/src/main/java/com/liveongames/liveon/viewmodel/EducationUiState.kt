package com.liveongames.liveon.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.domain.repository.EducationRepository
import com.liveongames.data.assets.education.EducationAssetLoader
import com.liveongames.data.model.education.EducationActionDef
import com.liveongames.domain.model.EducationProgram
import com.liveongames.domain.model.CompletedInstitution
import com.liveongames.domain.model.AcademicHonor
import com.liveongames.domain.model.Certification
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
import kotlin.math.roundToInt

data class EducationUiState(
    val loading: Boolean = true,
    val programs: List<EducationProgram> = emptyList(),
    val actions: List<EducationActionDef> = emptyList(), // Keep the original list for other potential uses
    val categorizedActions: Map<String, List<EducationActionDef>> = emptyMap(), // New field for categorized actions
    val enrollment: Enrollment? = null,
    val grade: Int = 0,                   // visible 0–100 grade (replaces terms/semesters UI)
    val showGpaInfo: Boolean = false,
    val showFailOrRetake: Boolean = false,
    val message: String? = null
)

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

    // -------------------- LOAD --------------------
    private fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val programs: List<EducationProgram> = repo.getPrograms()
            val actions: List<EducationActionDef> = repo.getActions().map { it as EducationActionDef }
            val enrollment = repo.getEnrollment()

            _uiState.update {
                it.copy(
                    loading = false,
                    programs = programs,
                    actions = actions,
                    enrollment = enrollment,
                    grade = if (enrollment != null) 100 else 0, // visible 0–100 grade
                    categorizedActions = categorizeActions(actions) // Categorize actions here
                )
            }
        } catch (e: Exception) {
            Log.e("EducationVM", "Error loading education data", e)
            _uiState.update { it.copy(loading = false, message = "Failed to load education data.") }
        }
    }

    // Dummy categorization logic for now - will need to be properly implemented
    private fun categorizeActions(actions: List<EducationActionDef>): Map<String, List<EducationActionDef>> {
        val categories = mutableMapOf<String, MutableList<EducationActionDef>>()
        for (action in actions) {
            // This is a placeholder. Real categorization needs logic based on action type or a new field in EducationActionDef
            val category = when {
                action.title.contains("Class", ignoreCase = true) -> "Attend Class"
                action.title.contains("Club", ignoreCase = true) || action.title.contains("Team", ignoreCase = true) || action.title.contains("Government", ignoreCase = true) -> "Group Activities"
                action.title.contains("Study", ignoreCase = true) || action.title.contains("Credit", ignoreCase = true) || action.title.contains("Homework", ignoreCase = true) -> "Self-Study / Extra Credit"
                action.title.contains("Test", ignoreCase = true) || action.title.contains("Exam", ignoreCase = true) || action.title == "SAT" || action.title == "GED" -> "Tests & Exams"
                action.title.contains("Break", ignoreCase = true) || action.title.contains("Fun", ignoreCase = true) || action.title.contains("Skip", ignoreCase = true) || action.title.contains("Prank", ignoreCase = true) || action.title.contains("Game", ignoreCase = true) -> "Break / Fun"
                else -> "Other"
            }
            categories.getOrPut(category) { mutableListOf() }.add(action)
        }
        return categories
    }

    // -------------------- EVENTS --------------------

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

    // -------------------- GRADE LOGIC --------------------
    // Map GPA deltas to visible grade points. 1.0 GPA == 25 points.
    private fun gpaToGradePoints(gpa: Double): Int = (gpa * 25.0).roundToInt()

    // Passive decay as progress increases. ~0.20 point per 1% progress.
    private fun decayForProgressDelta(progressDelta: Int): Int = (progressDelta * 0.20).roundToInt()

    private fun recomputeVisibleGrade(
        previous: Enrollment?,
        updated: Enrollment,
        currentGrade: Int
    ): Int {
        val gpaDelta = (updated.gpa - (previous?.gpa ?: updated.gpa))
        val progressDelta = (updated.progressPct - (previous?.progressPct ?: updated.progressPct)).coerceAtLeast(0)
        val next = currentGrade + gpaToGradePoints(gpaDelta) - decayForProgressDelta(progressDelta)
        return next.coerceIn(0, 100)
    }

    // -------------------- ACTIONS --------------------

    private fun handleFailOrRetake(retake: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val currentProgramId = _uiState.value.enrollment?.programId ?: return@launch
        try {
            if (retake) {
                val resetEnrollment = repo.enroll(currentProgramId)
                _uiState.update {
                    it.copy(
                        enrollment = resetEnrollment,
                        grade = 100,
                        showFailOrRetake = false,
                        message = "Program restarted. Progress reset."
                    )
                }
            } else {
                repo.resetEducation()
                _uiState.update {
                    it.copy(
                        enrollment = null,
                        grade = 0,
                        showFailOrRetake = false,
                        message = "You have failed the program and dropped out."
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(message = "Operation failed: ${e.message}", showFailOrRetake = false) }
        }
    }

    private fun enroll(programId: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val newEnrollment = repo.enroll(programId)
            _uiState.update {
                it.copy(
                    enrollment = newEnrollment,
                    grade = 100,
                    message = "Successfully enrolled in the program!"
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(message = "Enrollment failed: ${e.message}") }
        }
    }

    private fun performAction(actionId: String, choiceId: String, multiplier: Double) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentEnrollment = _uiState.value.enrollment
                    ?: throw IllegalStateException("No active enrollment")

                val actionResult: EducationActionResult = repo.applyAction(actionId, choiceId, multiplier)
                val updatedEnrollment = actionResult.enrollment

                val newGrade = recomputeVisibleGrade(
                    previous = currentEnrollment,
                    updated = updatedEnrollment,
                    currentGrade = _uiState.value.grade
                )

                val course = _uiState.value.programs.firstOrNull { it.id == updatedEnrollment.programId }
                if (course != null) {
                    if (updatedEnrollment.progressPct >= 100) {
                        if (updatedEnrollment.gpa >= course.minGpa) {
                            repo.resetEducation()
                            _uiState.update {
                                it.copy(
                                    enrollment = null,
                                    grade = 0,
                                    message = "Congratulations! You graduated from ${course.title} with a GPA of ${String.format("%.2f", updatedEnrollment.gpa)}!"
                                )
                            }
                        } else {
                            if (course.tier >= EduTier.HIGH) {
                                _uiState.update {
                                    it.copy(
                                        showFailOrRetake = true,
                                        enrollment = updatedEnrollment,
                                        grade = newGrade,
                                        message = "Program complete, but GPA is too low for graduation."
                                    )
                                }
                            } else {
                                repo.resetEducation()
                                _uiState.update {
                                    it.copy(enrollment = null, grade = 0, message = "Program ended. Required GPA not met. You must repeat.")
                                }
                            }
                        } else {
                            _uiState.update { it.copy(enrollment = updatedEnrollment, grade = newGrade) }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("EducationVM", "Error performing action $actionId", e)
                    _uiState.update { it.copy(message = "Action failed: ${e.message}") }
                }
            }

            // -------------------- HELPERS FOR UI --------------------

            fun isActionEligible(action: EducationActionDef, enrollment: Enrollment?): Boolean {
                val e = enrollment ?: return false

                val isTierEligible = action.tiers.any { it.name == e.tier.name }
                if (!isTierEligible) return false

                val minGpaMet = action.minGpa?.let { e.gpa >= it } ?: true
                val maxGpaMet = action.maxGpa?.let { e.gpa <= it } ?: true
                val isGpaEligible = minGpaMet && maxGpaMet
                if (!isGpaEligible) return false

                return isActionOffCooldown(action, e)
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
                    _uiState.update { it.copy(enrollment = updatedEnrollment) }
                    _uiState.update { it.copy(message = "Happy Birthday! Action limits have been reset for the year.") }
                } catch (e: Exception) {
                    _uiState.update { it.copy(message = "Age-up logic failed: ${e.message}") }
                }
            }

            fun forceRefresh() {
                loadData()
            }
        }

// -------------------- STATE & EVENTS --------------------

    data class EducationUiState(
        val loading: Boolean = true,
        val programs: List<EducationProgram> = emptyList(),
        val actions: List<EducationActionDef> = emptyList(), // Keep the original list for other potential uses
        val categorizedActions: Map<String, List<EducationActionDef>> = emptyMap(), // New field for categorized actions
        val enrollment: Enrollment? = null,
        val grade: Int = 0,                   // visible 0–100 grade (replaces terms/semesters UI)
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
