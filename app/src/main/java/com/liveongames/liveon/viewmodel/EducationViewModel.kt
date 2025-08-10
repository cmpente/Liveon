// app/src/main/java/com/liveongames/liveon/viewmodel/EducationViewModel.kt
package com.liveongames.liveon.viewmodel

import android.app.Application
import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

    // Keep UI alive even if Room throws — emit empty list instead of crashing compose.
    val educations: StateFlow<List<Education>> =
        repo.getEducations()
            .catch { e ->
                Log.e("EducationVM", "educations stream error", e)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeEducation: StateFlow<Education?> =
        educations.map { list -> list.firstOrNull { it.isActive } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val overallGpa: StateFlow<Double> = educations.map { list ->
        if (list.isEmpty()) 0.0 else list.map { it.currentGPA }.average().coerceIn(0.0, 4.0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val termState: StateFlow<TermStateEntity?> =
        repoImpl.observeTermState()
            .catch { e ->
                Log.e("EducationVM", "term state stream error", e)
                emit(null)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Ticker for cooldown progress
    private val _cooldownTicker = MutableStateFlow(0L)

    // In-memory cache to avoid blocking calls from Composables.
    // key = "$educationId|$actionId"
    private val _actionStates = MutableStateFlow<Map<String, EducationActionStateEntity>>(emptyMap())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _catalog.value = eduAssets.loadCourses()
            _actions.value = eduAssets.loadActions()
        }
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _cooldownTicker.value = System.currentTimeMillis()
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (repoImpl.getTermState() == null) {
                repoImpl.upsertTermState(TermStateEntity(characterId = PLAYER))
            }
        }
    }

    data class ActionLock(val locked: Boolean, val reason: String? = null)

    fun courseLockInfo(course: EducationCourse): ActionLock {
        val gpaOk = overallGpa.value >= course.requiredGpa
        if (!gpaOk) return ActionLock(true, "Requires GPA %s".format("%.2f".format(course.requiredGpa)))
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
        if (wanted != null && wanted != phase) return ActionLock(true, "Available in $wanted")
        if (def.requiresMilestonesAny.isNotEmpty() && !course.milestones.any { it in def.requiresMilestonesAny }) return ActionLock(true, "Requires milestone: ${def.requiresMilestonesAny.joinToString()}")
        return ActionLock(false)
    }

    fun enroll(course: EducationCourse) = viewModelScope.launch(Dispatchers.IO) {
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
        // Prime cache
        _actionStates.update { it - it.keys.filter { k -> k.startsWith("${edu.id}|") }.toSet() }
    }

    fun completeActiveIfAny() = viewModelScope.launch(Dispatchers.IO) {
        val active = activeEducation.value ?: return@launch
        repoImpl.updateStatus(active.id, false, System.currentTimeMillis())
    }

    data class ActionUseResult(val appliedDelta: Double, val newGpa: Double, val usedThisAge: Int, val capPerAge: Int)

    private fun key(educationId: String, actionId: String) = "$educationId|$actionId"

    private fun getCachedOrDefault(educationId: String, actionId: String): EducationActionStateEntity {
        val k = key(educationId, actionId)
        return _actionStates.value[k]
            ?: EducationActionStateEntity(PLAYER, educationId, actionId, 0L, 0)
    }

    private fun ensureCached(educationId: String, actionId: String) {
        val k = key(educationId, actionId)
        if (_actionStates.value.containsKey(k)) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val s = repoImpl.getActionState(educationId, actionId)
                    ?: EducationActionStateEntity(PLAYER, educationId, actionId, 0L, 0)
                _actionStates.update { it + (k to s) }
            } catch (t: Throwable) {
                Log.e("EducationVM", "load action state failed", t)
            }
        }
    }

    fun isOnCooldown(educationId: String, def: EducationActionDef): Boolean {
        _cooldownTicker.value // observe ticker to recompute every second
        ensureCached(educationId, def.id)
        val state = getCachedOrDefault(educationId, def.id)
        val cdMs = def.cooldownSeconds * 1000L
        return System.currentTimeMillis() < (state.lastUsedAt + cdMs)
    }

    fun cooldownMillisRemaining(educationId: String, def: EducationActionDef): Long {
        _cooldownTicker.value
        ensureCached(educationId, def.id)
        val state = getCachedOrDefault(educationId, def.id)
        val cdMs = def.cooldownSeconds * 1000L
        val end = state.lastUsedAt + cdMs
        return (end - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    fun cooldownProgress(educationId: String, def: EducationActionDef): Float {
        val rem = cooldownMillisRemaining(educationId, def)
        val dur = (def.cooldownSeconds * 1000L).coerceAtLeast(1L)
        return 1f - (rem.toFloat() / dur.toFloat())
    }

    fun performAction(
        def: EducationActionDef,
        tierMultiplier: Double = 1.0,
        critOverride: Boolean? = null,
        onApplied: (ActionUseResult) -> Unit = {}
    ) {
        val edu = activeEducation.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val course = _catalog.value.firstOrNull { it.id == edu.id } ?: return@launch
            val lock = isActionLocked(course, def); if (lock.locked) return@launch
            if (isOnCooldown(edu.id, def)) return@launch

            val currentState = getCachedOrDefault(edu.id, def.id)

            val withinCap = currentState.usedThisAge < def.capPerAge
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

            val newState = currentState.copy(
                lastUsedAt = System.currentTimeMillis(),
                usedThisAge = currentState.usedThisAge + 1
            )
            repoImpl.upsertActionState(newState)
            _actionStates.update { it + (key(edu.id, def.id) to newState) }

            onApplied(ActionUseResult(delta, newGpa, newState.usedThisAge, def.capPerAge))
        }
    }

    fun onAgeUp() = viewModelScope.launch(Dispatchers.IO) {
        val edu = activeEducation.value ?: return@launch
        repoImpl.resetActionCapsForAge(edu.id)
        // reset cached caps for current education
        _actionStates.update { map ->
            map.mapValues { (k, v) -> if (k.startsWith("${edu.id}|")) v.copy(usedThisAge = 0) else v }
        }
        val ts = repoImpl.getTermState() ?: TermStateEntity(characterId = PLAYER)
        val nextPhase = when (ts.coursePhase) { "EARLY" -> "MID"; "MID" -> "LATE"; else -> "EARLY" }
        repoImpl.updateTermState(min(100, ts.focus + 10), 0, ts.professorRelationship, ts.weekIndex + 4, nextPhase)
    }
}