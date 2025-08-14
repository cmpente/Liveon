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

    // -------------------- LOAD --------------------\n\n    private fun loadData() = viewModelScope.launch(Dispatchers.IO) {\n        try {\n            val programs: List<EducationProgram> = repo.getPrograms()\n            val actions: List<EducationActionDef> = repo.getActions().map { it as EducationActionDef }\n            val enrollment = repo.getEnrollment()\n\n            _uiState.update {\n                it.copy(\n                    loading = false,\n                    programs = programs,\n                    actions = actions,\n                    enrollment = enrollment,\n                    grade = if (enrollment != null) 100 else 0, // visible 0–100 grade\n                    categorizedActions = categorizeActions(actions) // Categorize actions here\n                )\n            }\n        } catch (e: Exception) {\n            Log.e(\"EducationVM\", \"Error loading education data\", e)\n            _uiState.update { it.copy(loading = false, message = \"Failed to load education data.\") }\n        }\n    }

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

    // -------------------- EVENTS --------------------\n\n    fun handleEvent(event: EducationEvent) {\n        when (event) {\n            is EducationEvent.Enroll -> enroll(event.programId)\n            is EducationEvent.DoAction -> performAction(event.actionId, event.choiceId, event.multiplier)\n            EducationEvent.DismissMessage -> _uiState.update { it.copy(message = null) }\n            EducationEvent.ShowGpaInfo -> _uiState.update { it.copy(showGpaInfo = true) }\n            EducationEvent.HideGpaInfo -> _uiState.update { it.copy(showGpaInfo = false) }\n            is EducationEvent.ChooseFailOrRetake -> handleFailOrRetake(event.retake)\n        }\n    }

    // -------------------- GRADE LOGIC --------------------\n    // Map GPA deltas to visible grade points. 1.0 GPA == 25 points.\n    private fun gpaToGradePoints(gpa: Double): Int = (gpa * 25.0).roundToInt()

    // Passive decay as progress increases. ~0.20 point per 1% progress.\n    private fun decayForProgressDelta(progressDelta: Int): Int = (progressDelta * 0.20).roundToInt()

    private fun recomputeVisibleGrade(\n        previous: Enrollment?,\n        updated: Enrollment,\n        currentGrade: Int\n    ): Int {\n        val gpaDelta = (updated.gpa - (previous?.gpa ?: updated.gpa))\n        val progressDelta = (updated.progressPct - (previous?.progressPct ?: updated.progressPct)).coerceAtLeast(0)\n        val next = currentGrade + gpaToGradePoints(gpaDelta) - decayForProgressDelta(progressDelta)\n        return next.coerceIn(0, 100)\n    }

    // -------------------- ACTIONS --------------------\n\n    private fun handleFailOrRetake(retake: Boolean) = viewModelScope.launch(Dispatchers.IO) {\n        val currentProgramId = _uiState.value.enrollment?.programId ?: return@launch\n        try {\n            if (retake) {\n                val resetEnrollment = repo.enroll(currentProgramId)\n                _uiState.update {\n                    it.copy(\n                        enrollment = resetEnrollment,\n                        grade = 100,\n                        showFailOrRetake = false,\n                        message = \"Program restarted. Progress reset.\"\n                    )\n                }\n            } else {\n                repo.resetEducation()\n                _uiState.update {\\n                    it.copy(\n                        enrollment = null,\n                        grade = 0,\n                        showFailOrRetake = false,\n                        message = \"You have failed the program and dropped out.\"\n                    )\n                }\n            }\n        } catch (e: Exception) {\n            _uiState.update { it.copy(message = \"Operation failed: ${e.message}\", showFailOrRetake = false) }\n        }\n    }

    private fun enroll(programId: String) = viewModelScope.launch(Dispatchers.IO) {\n        try {\n            val newEnrollment = repo.enroll(programId)\n            _uiState.update {\n                it.copy(\n                    enrollment = newEnrollment,\n                    grade = 100,\n                    message = \"Successfully enrolled in the program!\"\n                )\n            }\n        } catch (e: Exception) {\n            _uiState.update { it.copy(message = \"Enrollment failed: ${e.message}\") }\n        }\n    }

    private fun performAction(actionId: String, choiceId: String, multiplier: Double) =\n        viewModelScope.launch(Dispatchers.IO) {\n            try {\n                val currentEnrollment = _uiState.value.enrollment\n                    ?: throw IllegalStateException(\"No active enrollment\")

                val actionResult: EducationActionResult = repo.applyAction(actionId, choiceId, multiplier)\n                val updatedEnrollment = actionResult.enrollment

                val newGrade = recomputeVisibleGrade(\n                    previous = currentEnrollment,\n                    updated = updatedEnrollment,\n                    currentGrade = _uiState.value.grade\n                )

                val course = _uiState.value.programs.firstOrNull { it.id == updatedEnrollment.programId }\n                if (course != null) {\n                    if (updatedEnrollment.progressPct >= 100) {\n                        if (updatedEnrollment.gpa >= course.minGpa) {\n                            repo.resetEducation()\n                            _uiState.update {\n                                it.copy(\n                                    enrollment = null,\n                                    grade = 0,\n                                    message = \"Congratulations! You graduated from ${course.title} with a GPA of ${\"%.2f\".format(updatedEnrollment.gpa)}!\"\n                                )\n                            }\n                        } else {\n                            if (course.tier >= EduTier.HIGH) {\n                                _uiState.update {\n                                    it.copy(\n                                        showFailOrRetake = true,\n                                        enrollment = updatedEnrollment,\n                                        grade = newGrade,\n                                        message = \"Program complete, but GPA is too low for graduation.\"\n                                    )\n                                }\n                            } else {\n                                repo.resetEducation()\n                                _uiState.update {\n                                    it.copy(enrollment = null, grade = 0, message = \"Program ended. Required GPA not met. You must repeat.\")\n                                }\n                            }\n                        }\n                    } else {\n                        _uiState.update { it.copy(enrollment = updatedEnrollment, grade = newGrade) }\n                    }\n                }\n            } catch (e: Exception) {\n                Log.e(\"EducationVM\", \"Error performing action $actionId\", e)\n                _uiState.update { it.copy(message = \"Action failed: ${e.message}\") }\n            }\n        }

    // -------------------- HELPERS FOR UI --------------------\n\n    fun isActionEligible(action: EducationActionDef, enrollment: Enrollment?): Boolean {\n        val e = enrollment ?: return false

        val isTierEligible = action.tiers.any { it.name == e.tier.name }\n        if (!isTierEligible) return false

        val minGpaMet = action.minGpa?.let { e.gpa >= it } ?: true\n        val maxGpaMet = action.maxGpa?.let { e.gpa <= it } ?: true\n        val isGpaEligible = minGpaMet && maxGpaMet\n        if (!isGpaEligible) return false

        return isActionOffCooldown(action, e)\n    }

    private fun isActionOffCooldown(action: EducationActionDef, enrollment: Enrollment): Boolean {\n        val lastActionTime = enrollment.lastActionAt ?: return true\n        val cooldownMillis = action.cooldownMinutes * 60 * 1000L\n        val timeSinceLastAction = System.currentTimeMillis() - lastActionTime\n        return timeSinceLastAction >= cooldownMillis\n    }

    fun onAgeUp() = viewModelScope.launch(Dispatchers.IO) {\n        try {\n            val updatedEnrollment = repo.onAgeUp()\n            _uiState.update { it.copy(enrollment = updatedEnrollment) }\n            _uiState.update { it.copy(message = \"Happy Birthday! Action limits have been reset for the year.\") }\n        } catch (e: Exception) {\n            _uiState.update { it.copy(message = \"Age-up logic failed: ${e.message}\") }\n        }\n    }

    fun forceRefresh() {\n        loadData()\n    }\n}

// -------------------- STATE & EVENTS --------------------\n\ndata class EducationUiState(\n    val loading: Boolean = true,\n    val programs: List<EducationProgram> = emptyList(),\n    val actions: List<EducationActionDef> = emptyList(), // Keep the original list for other potential uses\n    val categorizedActions: Map<String, List<EducationActionDef>> = emptyMap(), // New field for categorized actions\n    val enrollment: Enrollment? = null,\n    val grade: Int = 0,                   // visible 0–100 grade (replaces terms/semesters UI)\n    val showGpaInfo: Boolean = false,\n    val showFailOrRetake: Boolean = false,\n    val message: String? = null\n)\n\nsealed interface EducationEvent {\n    data class Enroll(val programId: String) : EducationEvent\n    data class DoAction(val actionId: String, val choiceId: String, val multiplier: Double = 1.0) : EducationEvent\n    data object DismissMessage : EducationEvent\n    data object ShowGpaInfo : EducationEvent\n    data object HideGpaInfo : EducationEvent\n    data class ChooseFailOrRetake(val retake: Boolean) : EducationEvent\n}