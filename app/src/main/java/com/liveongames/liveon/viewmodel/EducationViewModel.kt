package com.liveongames.liveon.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.domain.repository.EducationRepository
import com.liveongames.domain.model.CompletedInstitution
import com.liveongames.domain.model.AcademicHonor
import com.liveongames.domain.model.Certification
import com.liveongames.data.assets.education.EducationAssetLoader
import com.liveongames.data.model.education.EducationActionDef
import com.liveongames.domain.model.EducationProgram
import com.liveongames.domain.model.Enrollment
import com.liveongames.domain.repository.PlayerRepository
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

@HiltViewModel
class EducationViewModel @Inject constructor(
    app: Application,
    private val repo: EducationRepository,
    private val playerRepository: PlayerRepository, // Inject PlayerRepository
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

            val categorizedActions = actions.groupBy {
                when {
                    it.title.contains("class", ignoreCase = true) || it.title.contains("lecture", ignoreCase = true) || it.title.contains("attend", ignoreCase = true) -> "Attend Class"
                    it.title.contains("club", ignoreCase = true) || it.title.contains("team", ignoreCase = true) || it.title.contains("group", ignoreCase = true) || it.title.contains("student government", ignoreCase = true) -> "Group Activities"
                    it.title.contains("study", ignoreCase = true) || it.title.contains("homework", ignoreCase = true) || it.title.contains("tutor", ignoreCase = true) || it.title.contains("read", ignoreCase = true) || it.title.contains("extra credit", ignoreCase = true) -> "Self-Study / Extra Credit"
                    it.title.contains("test", ignoreCase = true) || it.title.contains("exam", ignoreCase = true) || it.title.contains("SAT", ignoreCase = true) || it.title.contains("GED", ignoreCase = true) || it.title.contains("midterm", ignoreCase = true) || it.title.contains("final", ignoreCase = true) -> "Tests & Exams"
                    it.title.contains("break", ignoreCase = true) || it.title.contains("fun", ignoreCase = true) || it.title.contains("skip", ignoreCase = true) || it.title.contains("prank", ignoreCase = true) || it.title.contains("game", ignoreCase = true) -> "Break / Fun"
                    else -> "Other Activities"
                }
            }

            val enrollment = repo.getEnrollment()
            val playerRequirements = playerRepository.getPlayerRequirements() // Fetch player requirements
 val completedInstitutions = repo.getCompletedInstitutions()
 val academicHonors = repo.getAcademicHonors()
 val certifications = repo.getCertifications()

            _uiState.update {
                it.copy(
                    loading = false,
                    categorizedActions = categorizedActions,
                    programs = programs,
                    actions = actions,
                    enrollment = enrollment, //
                    playerRequirements = playerRequirements, // Update playerRequirements in state
                    grade = if (enrollment != null) 100 else 0, // visible 0–100 grade
 completedInstitutions = completedInstitutions, // Populate completed institutions
 academicHonors = academicHonors, // Populate academic honors
 certifications = certifications // Populate certifications
                )
            }
        } catch (e: Exception) {
            Log.e("EducationVM", "Error loading education data", e)
            _uiState.update { it.copy(loading = false, message = "Failed to load education data.") }
        }
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
            val programToEnroll = _uiState.value.programs.firstOrNull { it.id == programId }
            if (programToEnroll != null) {
                val playerHasRequirements = _uiState.value.playerRequirements.containsAll(programToEnroll.requirements) // Use playerRequirements from state
                if (!playerHasRequirements) {
                    _uiState.update { it.copy(message = "Enrollment failed: Missing requirements.") }
                    return@launch
                }

                // Assuming minimum GPA check is handled in the UI before calling enroll
                // If not, add a check here: _uiState.value.enrollment.gpa >= programToEnroll.minGpa

            }
            val newEnrollment = repo.enroll(programId)
            _uiState.update { it.copy(enrollment = newEnrollment, grade = 100, message = "Successfully enrolled in ${newEnrollment.title}!"
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
                                    message = "Congratulations! You graduated from ${course.title} with a GPA of ${"%.2f".format(updatedEnrollment.gpa)}!"
                                )
                            }
                        } else {
                            if (course.tier >= EduTier.HIGH) {
                                _uiState.update {
                                    it.copy(showFailOrRetake = true, enrollment = updatedEnrollment, grade = newGrade, message = "Program complete, but GPA is too low for graduation.")
                                }
                            }
 else {
                                // For tiers below HIGH, failing means repeating.
                                // We reset education but keep the player at the same age
                                // to simulate repeating a grade/program.
                                // A more sophisticated system could track failed attempts.
                                repo.resetEducation() // This effectively means repeating the current level
                                _uiState.update {
                                    it.copy(enrollment = null, grade = 0, message = "Program ended. Required GPA not met. You must repeat.")
                                }
                            }
                        }
                    } else { // Add missing closing brace for the outer if (updatedEnrollment.progressPct >= 100)
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

 if (!action.tiers.contains(e.tier)) return false

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
    val actions: List<EducationActionDef> = emptyList(),
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
