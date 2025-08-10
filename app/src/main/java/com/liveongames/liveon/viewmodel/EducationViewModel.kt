package com.liveongames.liveon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.domain.model.Education
import com.liveongames.domain.model.EducationLevel
import com.liveongames.domain.repository.CharacterRepository
import com.liveongames.domain.repository.EducationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

/**
 * EducationViewModel
 *
 * Features:
 * - Enroll / Complete flows (uses your repositories)
 * - GPA updates from “actions” with MINUTE cooldowns and diminishing returns:
 *      • After 3 consecutive taps on the SAME action (per education), further taps = 0 benefit
 *        until the player switches actions (or you can also reset after a full cooldown cycle).
 * - Exposes UI-friendly cooldown and action-state flows for disabling buttons / showing timers.
 * - Helpers to fetch active education and current GPA safely.
 */
@HiltViewModel
class EducationViewModel @Inject constructor(
    private val educationRepository: EducationRepository,
    private val characterRepository: CharacterRepository
) : ViewModel() {

    // ----------------------------
    // Public streams
    // ----------------------------

    /** All education rows for the player (Room flow) */
    val educations: StateFlow<List<Education>> =
        educationRepository.getEducations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Active (non-cert) education, if any */
    val activeEducation: StateFlow<Education?> =
        educations.map { list -> list.find { it.isActive && it.level != EducationLevel.CERTIFICATION } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Current GPA the UI should display (falls back to 2.0 if no active program yet) */
    val currentGpa: StateFlow<Double> =
        activeEducation.map { it?.currentGPA ?: 2.0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 2.0)

    // ----------------------------
    // Action + cooldown system
    // ----------------------------

    /** Supported academic actions */
    enum class EduAction {
        ATTEND_LECTURE,        // 1 min
        HOMEWORK,              // 3 min
        STUDY_SOLO,            // 1 min
        GROUP_STUDY,           // 2 min
        LIBRARY_RESEARCH,      // 5 min
        TUTORING_SESSION,      // 5 min
        EXAM_PREP,             // 5 min
        SCHOOL_EVENT,          // 2 min
        WELLNESS_BREAK         // 0 min (no GPA change but can reduce strain if you add it later)
    }

    /** Base GPA effect per tap (before diminishing) */
    private val baseGpaEffect: Map<EduAction, Double> = mapOf(
        EduAction.ATTEND_LECTURE   to 0.05,
        EduAction.HOMEWORK         to 0.03,
        EduAction.STUDY_SOLO       to 0.02,
        EduAction.GROUP_STUDY      to 0.02,
        EduAction.LIBRARY_RESEARCH to 0.04,
        EduAction.TUTORING_SESSION to 0.03,
        EduAction.EXAM_PREP        to 0.04,
        EduAction.SCHOOL_EVENT     to 0.01,
        EduAction.WELLNESS_BREAK   to 0.00
    )

    /** Cooldowns (minutes) */
    private val cooldownMinutes: Map<EduAction, Int> = mapOf(
        EduAction.ATTEND_LECTURE   to 1,
        EduAction.HOMEWORK         to 3,
        EduAction.STUDY_SOLO       to 1,
        EduAction.GROUP_STUDY      to 2,
        EduAction.LIBRARY_RESEARCH to 5,
        EduAction.TUTORING_SESSION to 5,
        EduAction.EXAM_PREP        to 5,
        EduAction.SCHOOL_EVENT     to 2,
        EduAction.WELLNESS_BREAK   to 0
    )

    /** Max consecutive taps on the same action before returns drop to zero */
    private val maxConsecutive = 3

    /** In-memory cooldown + counters per educationId + action */
    private val _cooldowns = MutableStateFlow<Map<String, Map<EduAction, CooldownState>>>(emptyMap())
    val cooldowns: StateFlow<Map<String, Map<EduAction, CooldownState>>> = _cooldowns.asStateFlow()

    private val _consecutiveCounters =
        MutableStateFlow<Map<String, Map<EduAction, Int>>>(emptyMap())
    val consecutiveCounters: StateFlow<Map<String, Map<EduAction, Int>>> = _consecutiveCounters.asStateFlow()

    /** Track last action per education to decide when to reset the "consecutive" run */
    private val _lastAction = MutableStateFlow<Map<String, EduAction?>>(emptyMap())

    private val tickerJobs = mutableMapOf<String, Job>() // simple per-education tickers

    data class CooldownState(
        val endTimeMillis: Long,  // when the cooldown ends
        val durationMillis: Long  // total cooldown duration to compute progress %
    )

    // ----------------------------
    // Enrollment / completion
    // ----------------------------

    fun enrollInEducation(education: Education) {
        viewModelScope.launch {
            runCatching {
                // Pull character GPA from your int storage (e.g., 320 => 3.20)
                val character = characterRepository.getCharacter("player_character").firstOrNull() ?: return@launch
                val playerGpaValue = if (character.education > 0) character.education / 100.0 else 0.0

                // Pre-check GPA and active non-cert constraint
                if (playerGpaValue < education.requiredGPA) return@launch

                val activeNonCert = educations.value.find { it.isActive && it.level != EducationLevel.CERTIFICATION }
                if (activeNonCert != null && education.level != EducationLevel.CERTIFICATION) return@launch

                // Pay cost
                if (character.money < education.cost) return@launch
                characterRepository.updateCharacter(character.copy(money = character.money - education.cost))

                // Add as active education, initialize with player's current GPA
                val newEdu = education.copy(
                    isActive = true,
                    timestamp = System.currentTimeMillis(),
                    currentGPA = playerGpaValue
                )
                educationRepository.addEducation(newEdu)

                // Initialize per-edu maps
                initActionState(newEdu.id)
            }.onFailure { it.printStackTrace() }
        }
    }

    fun completeEducation(education: Education) {
        viewModelScope.launch {
            runCatching {
                // mark complete
                val completed = education.copy(
                    completionDate = System.currentTimeMillis(),
                    isActive = false
                )
                educationRepository.updateEducation(completed)

                // Update character "education" int value from the final GPA
                updateCharacterEducationGpa(completed.currentGPA)

                // Clear in-memory state for that education id
                clearActionState(education.id)
            }.onFailure { it.printStackTrace() }
        }
    }

    private suspend fun updateCharacterEducationGpa(gpa: Double) {
        val character = characterRepository.getCharacter("player_character").firstOrNull() ?: return
        val educationValue = (gpa * 100).toInt().coerceIn(0, 400) // cap 4.00 → 400
        characterRepository.updateCharacter(character.copy(education = educationValue))
    }

    // ----------------------------
    // Action execution
    // ----------------------------

    /**
     * Perform an academic action for the active education.
     * - Enforces cooldowns
     * - Applies diminishing returns after 3 consecutive taps on the same action
     * - Updates GPA (0.00..4.00)
     */
    fun performAction(action: EduAction) {
        val edu = activeEducation.value ?: return

        viewModelScope.launch {
            val now = System.currentTimeMillis()

            // Check cooldown
            val cdMap = cooldowns.value[edu.id].orEmpty()
            val cd = cdMap[action]
            if (cd != null && now < cd.endTimeMillis) {
                // still cooling down, ignore
                return@launch
            }

            // Diminishing returns: if last action is different -> reset consecutive for the new action
            val last = _lastAction.value[edu.id]
            if (last != action) {
                // Reset the new action counter to 0 (fresh start) and keep others as-is
                setConsecutive(edu.id, action, 0)
            }

            // Compute effective GPA delta
            val base = baseGpaEffect[action] ?: 0.0
            val currentRun = getConsecutive(edu.id, action)
            val effective = if (currentRun >= maxConsecutive) {
                0.0
            } else {
                base
            }

            // Update GPA in DB if active
            val fresh = educationRepository.getEducationById(edu.id) ?: return@launch
            if (!fresh.isActive) return@launch

            val improved = (fresh.currentGPA + effective)
                .let { min(4.0, it) } // hard cap at 4.0

            educationRepository.updateEducation(
                fresh.copy(
                    currentGPA = improved
                )
            )

            // Increase consecutive count if we actually applied the same action
            if (last == action) {
                setConsecutive(edu.id, action, currentRun + 1)
            } else {
                setConsecutive(edu.id, action, 1)
            }
            // Update last action
            setLastAction(edu.id, action)

            // Start cooldown
            val minutes = cooldownMinutes[action] ?: 0
            if (minutes > 0) {
                startCooldown(edu.id, action, minutes)
            } else {
                // Ensure we still notify UI when zero (e.g., refresh counters/last action)
                emitCooldown(edu.id, action, 0L)
            }
        }
    }

    // ----------------------------
    // Cooldown helpers for UI
    // ----------------------------

    /** Remaining millis for this action on a given educationId */
    fun cooldownMillisRemaining(educationId: String, action: EduAction, now: Long = System.currentTimeMillis()): Long {
        val cd = cooldowns.value[educationId]?.get(action) ?: return 0L
        return (cd.endTimeMillis - now).coerceAtLeast(0L)
    }

    /** 0f..1f progress for radial indicators */
    fun cooldownProgress(educationId: String, action: EduAction, now: Long = System.currentTimeMillis()): Float {
        val cd = cooldowns.value[educationId]?.get(action) ?: return 0f
        val remaining = (cd.endTimeMillis - now).coerceAtLeast(0L).toFloat()
        return if (cd.durationMillis <= 0L) 0f else 1f - (remaining / cd.durationMillis)
    }

    /** Whether the action is currently cooling down */
    fun isOnCooldown(educationId: String, action: EduAction, now: Long = System.currentTimeMillis()): Boolean {
        val cd = cooldowns.value[educationId]?.get(action) ?: return false
        return now < cd.endTimeMillis
    }

    /** How many consecutive taps the user has made on this action for this education */
    fun consecutiveCount(educationId: String, action: EduAction): Int {
        return _consecutiveCounters.value[educationId]?.get(action) ?: 0
    }

    // ----------------------------
    // Internal state plumbing
    // ----------------------------

    private fun initActionState(educationId: String) {
        // initialize maps
        if (!cooldowns.value.containsKey(educationId)) {
            _cooldowns.update { it + (educationId to emptyMap()) }
        }
        if (!_consecutiveCounters.value.containsKey(educationId)) {
            _consecutiveCounters.update { it + (educationId to emptyMap()) }
        }
        if (!_lastAction.value.containsKey(educationId)) {
            _lastAction.update { it + (educationId to null) }
        }

        // Start a ticker for UI progress (1s granularity, tiny cost)
        if (tickerJobs[educationId] == null) {
            tickerJobs[educationId] = viewModelScope.launch {
                while (true) {
                    delay(1_000L)
                    // just re-emit cooldown map so observers recompute progress
                    _cooldowns.update { current -> current.toMap() }
                }
            }
        }
    }

    private fun clearActionState(educationId: String) {
        _cooldowns.update { it - educationId }
        _consecutiveCounters.update { it - educationId }
        _lastAction.update { it - educationId }
        tickerJobs.remove(educationId)?.cancel()
    }

    private fun setConsecutive(educationId: String, action: EduAction, count: Int) {
        _consecutiveCounters.update { map ->
            val inner = map[educationId].orEmpty().toMutableMap()
            inner[action] = count
            map + (educationId to inner.toMap())
        }
    }

    private fun getConsecutive(educationId: String, action: EduAction): Int {
        return _consecutiveCounters.value[educationId]?.get(action) ?: 0
    }

    private fun setLastAction(educationId: String, action: EduAction) {
        _lastAction.update { it + (educationId to action) }
    }

    private fun startCooldown(educationId: String, action: EduAction, minutes: Int) {
        val duration = minutes * 60_000L
        val end = System.currentTimeMillis() + duration
        emitCooldown(educationId, action, duration, end)
    }

    private fun emitCooldown(
        educationId: String,
        action: EduAction,
        durationMillis: Long,
        endTimeMillis: Long = System.currentTimeMillis()
    ) {
        _cooldowns.update { map ->
            val inner = map[educationId].orEmpty().toMutableMap()
            inner[action] = CooldownState(endTimeMillis = endTimeMillis, durationMillis = durationMillis)
            map + (educationId to inner.toMap())
        }
    }

    // ----------------------------
    // Legacy helpers (kept for compatibility if other parts still call them)
    // ----------------------------

    /** Legacy: attendClass → maps to ATTEND_LECTURE */
    fun attendClass(educationId: String) {
        if (activeEducation.value?.id == educationId) {
            performAction(EduAction.ATTEND_LECTURE)
        }
    }

    /** Legacy: doHomework → maps to HOMEWORK */
    fun doHomework(educationId: String) {
        if (activeEducation.value?.id == educationId) {
            performAction(EduAction.HOMEWORK)
        }
    }

    /** Legacy: study → maps to STUDY_SOLO */
    fun study(educationId: String) {
        if (activeEducation.value?.id == educationId) {
            performAction(EduAction.STUDY_SOLO)
        }
    }

    // ----------------------------
    // Safety: cap usage if someone else calls GPA mutators directly
    // ----------------------------

    private fun applyCappedGpaIncrease(current: Double, delta: Double): Double {
        val next = current + delta
        return next.coerceAtMost(4.0).coerceAtLeast(0.0)
    }
}
