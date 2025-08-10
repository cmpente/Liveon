package com.liveongames.liveon.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.data.db.entity.EducationActionStateEntity
import com.liveongames.data.db.entity.TermStateEntity
import com.liveongames.data.repository.EducationRepositoryImpl
import com.liveongames.domain.model.Education
import com.liveongames.domain.repository.EducationRepository
import com.liveongames.liveon.assets.education.EducationAssetLoader
import com.liveongames.liveon.model.EducationActionDef
import com.liveongames.liveon.model.EducationCourse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class EducationViewModel @Inject constructor(
    app: Application,
    private val repo: EducationRepository,
    private val repoImpl: EducationRepositoryImpl,
    private val eduAssets: EducationAssetLoader
) : AndroidViewModel(app) {

    companion object { private const val PLAYER = "player_character" }

    private val _catalog = MutableStateFlow<List<EducationCourse>>(emptyList())
    val catalog: StateFlow<List<EducationCourse>> = _catalog.asStateFlow()

    private val _actions = MutableStateFlow<List<EducationActionDef>>(emptyList())
    val actions: StateFlow<List<EducationActionDef>> = _actions.asStateFlow()

    val educations: StateFlow<List<Education>> =
        repo.getEducations().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeEducation: StateFlow<Education?> =
        educations.map { list -> list.firstOrNull { it.isActive } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val overallGpa: StateFlow<Double> = educations.map { list ->
        if (list.isEmpty()) 0.0 else list.map { it.currentGPA }.average().coerceIn(0.0, 4.0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val termState: StateFlow<TermStateEntity?> =
        repoImpl.observeTermState().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _cooldownTicker = MutableStateFlow(0L)

    init {
        viewModelScope.launch {
            _catalog.value = eduAssets.loadCourses()
            _actions.value = eduAssets.loadActions()
        }
        viewModelScope.launch { while (true) { delay(1000); _cooldownTicker.value = System.currentTimeMillis() } }
        viewModelScope.launch { if (repoImpl.getTermState() == null) repoImpl.upsertTermState(TermStateEntity(characterId = PLAYER)) }
    }

    data class ActionLock(val locked: Boolean, val reason: String? = null)

    fun courseLockInfo(course: EducationCourse): ActionLock {
        val gpaOk = overallGpa.value >= course.requiredGpa
        if (!gpaOk) return ActionLock(true, "Requires GPA ${"%.2f".format(course.requiredGpa)}")
        val completedIds = educations.value.filter { it.completionDate != null }.map { it.id }.toSet()
        val missing = course.prerequisites.filterNot { it in completedIds }
        if (missing.isNotEmpty()) return ActionLock(true, "Prerequisites: ${missing.joinToString()}")
        return ActionLock(false)
    }

    fun isActionLocked(course: EducationCourse, def: EducationActionDef): ActionLock {
        val activeGpa = activeEducation.value?.currentGPA ?: overallGpa.value
        if (activeGpa < def.minGpa) return ActionLock(true, "Needs GPA ${"%.2f".format(def.minGpa)}")
        if (def.allowedLevels.isNotEmpty() && course.level !in def.allowedLevels) return ActionLock(true, "Unavailable at ${course.level.displayName}")
        val phase = termState.value?.coursePhase ?: "EARLY"
        val wanted = def.availability?.phase?.name
        if (wanted != null && wanted != phase) return ActionLock(true, "Available in ${wanted}")
        if (def.requiresMilestonesAny.isNotEmpty() && !course.milestones.any { it in def.requiresMilestonesAny }) return ActionLock(true, "Requires milestone: ${def.requiresMilestonesAny.joinToString()}")
        return ActionLock(false)
    }

    fun enroll(course: EducationCourse) = viewModelScope.launch {
        val lock = courseLockInfo(course); if (lock.locked) return@launch
        repoImpl.deactivateAll()
        val edu = Education(
            id = course.id, name = course.name, description = course.flavorText,
            level = course.level, cost = course.cost, duration = course.durationMonths,
            requiredGPA = course.requiredGpa, currentGPA = 0.0, isActive = true,
            timestamp = System.currentTimeMillis(), completionDate = null,
            attendClassCount = 0, doHomeworkCount = 0, studyCount = 0
        )
        repo.addEducation(edu)
        repoImpl.resetActionCapsForAge(edu.id)
    }

    fun completeActiveIfAny() = viewModelScope.launch {
        val active = activeEducation.value ?: return@launch
        repoImpl.updateStatus(active.id, false, System.currentTimeMillis())
    }

    data class ActionUseResult(val appliedDelta: Double, val newGpa: Double, val usedThisAge: Int, val capPerAge: Int)

    suspend fun actionState(educationId: String, actionId: String) =
        repoImpl.getActionState(educationId, actionId) ?: EducationActionStateEntity(PLAYER, educationId, actionId, 0L, 0)

    fun isOnCooldown(educationId: String, def: EducationActionDef): Boolean {
        _cooldownTicker.value
        val state = runBlockingNoThrow { actionState(educationId, def.id) }
        val cdMs = def.cooldownSeconds * 1000L
        return System.currentTimeMillis() < (state.lastUsedAt + cdMs)
    }

    fun cooldownMillisRemaining(educationId: String, def: EducationActionDef): Long {
        _cooldownTicker.value
        val state = runBlockingNoThrow { actionState(educationId, def.id) }
        val cdMs = def.cooldownSeconds * 1000L
        val end = state.lastUsedAt + cdMs
        return (end - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    fun cooldownProgress(educationId: String, def: EducationActionDef): Float {
        val rem = cooldownMillisRemaining(educationId, def)
        val dur = (def.cooldownSeconds * 1000L).coerceAtLeast(1L)
        return 1f - (rem.toFloat() / dur.toFloat())
    }

    fun performAction(def: EducationActionDef, tierMultiplier: Double = 1.0, critOverride: Boolean? = null, onApplied: (ActionUseResult) -> Unit = {}) {
        val edu = activeEducation.value ?: return
        viewModelScope.launch {
            val course = _catalog.value.firstOrNull { it.id == edu.id } ?: return@launch
            val lock = isActionLocked(course, def); if (lock.locked) return@launch
            if (isOnCooldown(edu.id, def)) return@launch

            val state = actionState(edu.id, def.id)
            val withinCap = state.usedThisAge < def.capPerAge
            var delta = if (withinCap) def.baseDelta else 0.0
            delta *= tierMultiplier
            delta *= course.difficultyMod

            val focus = termState.value?.focus ?: 100
            val focusMul = when { focus >= 90 -> 1.10; focus >= 70 -> 1.05; focus >= 50 -> 1.00; focus >= 30 -> 0.95; else -> 0.90 }
            delta *= focusMul

            val crit = critOverride ?: (def.critChance > 0.0 && kotlin.random.Random.nextDouble() < def.critChance)
            if (crit) delta *= max(1.0, def.critMultiplier)

            val fresh = repo.getEducationById(edu.id) ?: return@launch
            val newGpa = (fresh.currentGPA + delta).coerceIn(0.0, 4.0)
            repoImpl.setGpa(edu.id, newGpa)

            val newUses = state.usedThisAge + 1
            repoImpl.upsertActionState(state.copy(lastUsedAt = System.currentTimeMillis(), usedThisAge = newUses))
            onApplied(ActionUseResult(delta, newGpa, newUses, def.capPerAge))
        }
    }

    fun onAgeUp() = viewModelScope.launch {
        val edu = activeEducation.value ?: return@launch
        repoImpl.resetActionCapsForAge(edu.id)
        val ts = repoImpl.getTermState() ?: TermStateEntity(characterId = PLAYER)
        val nextPhase = when (ts.coursePhase) { "EARLY" -> "MID"; "MID" -> "LATE"; else -> "EARLY" }
        repoImpl.updateTermState(min(100, ts.focus + 10), 0, ts.professorRelationship, ts.weekIndex + 4, nextPhase)
    }

    private fun <T> runBlockingNoThrow(block: suspend () -> T): T {
        var out: T? = null
        val job = viewModelScope.launch { try { out = block() } catch (_: Throwable) {} }
        while (job.isActive) { /* spin */ }
        @Suppress("UNCHECKED_CAST") return out as T
    }
}
