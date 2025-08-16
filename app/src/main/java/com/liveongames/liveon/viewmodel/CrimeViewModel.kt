// app/src/main/java/com/liveongames/liveon/viewmodel/CrimeViewModel.kt
package com.liveongames.liveon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.domain.model.CrimeRecordEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class CrimeViewModel(
    private val loader: CrimeContentLoader = InMemoryCrimeContentLoader(),
    private val persistence: CrimePersistencePort = InMemoryCrimePersistence()
) : ViewModel() {

    enum class CrimeType {
        PICKPOCKETING, SHOPLIFTING, VANDALISM, PETTY_SCAM,
        MUGGING, BREAKING_AND_ENTERING, DRUG_DEALING, COUNTERFEIT_GOODS,
        BURGLARY, FRAUD, ARMS_SMUGGLING, DRUG_TRAFFICKING,
        ARMED_ROBBERY, EXTORTION, KIDNAPPING_FOR_RANSOM, PONZI_SCHEME,
        CONTRACT_KILLING, DARK_WEB_SALES, ART_THEFT, DIAMOND_HEIST,
        BANK_HEIST, POLITICAL_ASSASSINATION, CRIME_SYNDICATE
    }

    enum class Phase { SETUP, EXECUTION, CLIMAX }

    data class CrimeRunState(
        val type: CrimeType,
        val startedAtMs: Long,
        val durationMs: Long,
        val progress: Float,
        val phase: Phase,
        val scriptAll: String,
        val ambientLines: List<String>
    )

    data class OutcomeEvent(
        val type: CrimeType,
        val success: Boolean,
        val wasCaught: Boolean,
        val moneyGained: Int,
        val jailDays: Int,
        val climaxLine: String
    )

    private val _playerNotoriety = MutableStateFlow(0)
    val playerNotoriety: StateFlow<Int> = _playerNotoriety.asStateFlow()

    private val _criminalRecords = MutableStateFlow<List<CrimeRecordEntry>>(emptyList())
    val criminalRecords: StateFlow<List<CrimeRecordEntry>> = _criminalRecords.asStateFlow()

    private val _runState = MutableStateFlow<CrimeRunState?>(null)
    val runState: StateFlow<CrimeRunState?> = _runState.asStateFlow()

    private val _lastOutcome = MutableStateFlow<OutcomeEvent?>(null)
    val lastOutcome: StateFlow<OutcomeEvent?> = _lastOutcome.asStateFlow()

    private val _cooldownUntil = MutableStateFlow<Long?>(null)
    val cooldownUntil: StateFlow<Long?> = _cooldownUntil.asStateFlow()

    companion object {
        private const val MAX_NOTORIETY_PER_YEAR = 100
        private const val DEFAULT_COOLDOWN_ON_FAIL_MS = 2_500L
        private const val DEFAULT_COOLDOWN_ON_CAUGHT_MS = 7_000L
    }

    data class Baseline(val notorietyGain: Int, val notorietyLoss: Int)

    fun baselineFor(type: CrimeType): Baseline = when (type) {
        CrimeType.PICKPOCKETING,
        CrimeType.SHOPLIFTING,
        CrimeType.VANDALISM,
        CrimeType.PETTY_SCAM -> Baseline(notorietyGain = 1, notorietyLoss = -1)

        CrimeType.MUGGING,
        CrimeType.BREAKING_AND_ENTERING,
        CrimeType.DRUG_DEALING,
        CrimeType.COUNTERFEIT_GOODS -> Baseline(notorietyGain = 2, notorietyLoss = -2)

        CrimeType.BURGLARY,
        CrimeType.FRAUD,
        CrimeType.ARMS_SMUGGLING,
        CrimeType.DRUG_TRAFFICKING -> Baseline(notorietyGain = 4, notorietyLoss = -2)

        else -> Baseline(notorietyGain = 10, notorietyLoss = -3)
    }

    private var tickerJob: Job? = null

    init {
        viewModelScope.launch {
            val snapshot = persistence.loadSnapshot()
            _criminalRecords.value = snapshot.records
            _playerNotoriety.value = snapshot.notoriety
        }
    }

    fun beginCrime(type: CrimeType) {
        if (_runState.value != null) return
        val content = loader.pickScenario(type) ?: return

        val now = System.currentTimeMillis()
        val durationMs = (content.durationSeconds * 1_000L).coerceAtLeast(4_000L)
        val scriptAll = (content.setup + content.execution).joinToString(" ").trim()
        val ambientOnce = content.ambient.distinct()

        _runState.value = CrimeRunState(
            type = type,
            startedAtMs = now,
            durationMs = durationMs,
            progress = 0f,
            phase = Phase.SETUP,
            scriptAll = scriptAll,
            ambientLines = ambientOnce
        )

        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            val setupEnd = content.setupPortion
            val execEnd = setupEnd + content.execPortion
            var p: Float
            do {
                val t = System.currentTimeMillis()
                p = ((t - now).toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                val newPhase = when {
                    p < setupEnd -> Phase.SETUP
                    p < execEnd -> Phase.EXECUTION
                    else -> Phase.CLIMAX
                }
                _runState.update { it?.copy(progress = p, phase = newPhase) }
                delay(16)
            } while (p < 1f && _runState.value != null)

            if (_runState.value != null) resolveAndPostOutcome(type, content)
        }
    }

    fun cancelCrime() {
        val rs = _runState.value ?: return
        tickerJob?.cancel()
        tickerJob = null
        _runState.value = null
        val outcome = OutcomeEvent(
            type = rs.type, success = false, wasCaught = false,
            moneyGained = 0, jailDays = 0,
            climaxLine = "You back off at the last second."
        )
        persistOutcomeAndCooldown(rs, outcome, cooldownMs = DEFAULT_COOLDOWN_ON_FAIL_MS)
        _lastOutcome.value = outcome
    }

    fun consumeOutcome() { _lastOutcome.value = null }

    fun effectiveNotorietyForRecord(rec: CrimeRecordEntry): Int? {
        val list = _criminalRecords.value.filter { it.year == rec.year }.sortedBy { it.timestamp }
        var earned = 0
        for (r in list) {
            val type = runCatching { CrimeType.valueOf(r.typeKey) }.getOrNull() ?: continue
            val base = baselineFor(type)
            val baseDelta = when {
                r.caught -> base.notorietyLoss
                r.success && r.money > 0 -> base.notorietyGain
                r.success -> max(1, base.notorietyGain / 2)
                else -> base.notorietyLoss
            }
            val applied = if (baseDelta > 0) {
                val remaining = MAX_NOTORIETY_PER_YEAR - earned
                val appliedPos = remaining.coerceAtLeast(0).coerceAtMost(baseDelta)
                earned += appliedPos
                appliedPos
            } else baseDelta
            if (r.id == rec.id) return applied
        }
        return null
    }

    private fun resolveAndPostOutcome(type: CrimeType, content: CrimeScenario) {
        val roll = rollWeighted(content.outcomes)
        val (success, caught) = when (roll.kind) {
            OutcomeKind.SUCCESS -> true to false
            OutcomeKind.PARTIAL -> true to false
            OutcomeKind.FAIL -> false to false
            OutcomeKind.CAUGHT -> false to true
        }

        val money = if (success) content.payout(successFull = (roll.kind == OutcomeKind.SUCCESS)) else 0
        val jail = if (caught) content.jailDays() else 0
        val climaxLine = content.climaxFor(
            success = (roll.kind == OutcomeKind.SUCCESS),
            partial = (roll.kind == OutcomeKind.PARTIAL),
            fail = (roll.kind == OutcomeKind.FAIL),
            caught = (roll.kind == OutcomeKind.CAUGHT)
        )

        val outcome = OutcomeEvent(
            type = type, success = success, wasCaught = caught,
            moneyGained = money, jailDays = jail, climaxLine = climaxLine
        )

        val rs = _runState.value!!
        _runState.value = null
        tickerJob?.cancel(); tickerJob = null

        val cooldown = when {
            caught -> DEFAULT_COOLDOWN_ON_CAUGHT_MS
            success -> 0L
            else -> DEFAULT_COOLDOWN_ON_FAIL_MS
        }
        persistOutcomeAndCooldown(rs, outcome, cooldown)
        _lastOutcome.value = outcome
    }

    private fun prettyTypeLabel(type: CrimeType): String {
        val raw = type.name.replace('_', ' ').lowercase(Locale.getDefault())
        return raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    private fun summaryForRecord(type: CrimeType, outcome: OutcomeEvent): String {
        val disposition = when {
            outcome.wasCaught -> "Caught"
            outcome.success && outcome.moneyGained > 0 -> "Success"
            outcome.success -> "Partial"
            else -> "Fail"
        }
        val parts = buildList {
            add("$disposition — ${prettyTypeLabel(type)}")
            if (outcome.moneyGained > 0) add("$${outcome.moneyGained}")
            if (outcome.jailDays > 0) add("Jail ${outcome.jailDays}d")
        }
        return parts.joinToString(" • ")
    }

    private fun persistOutcomeAndCooldown(rs: CrimeRunState, outcome: OutcomeEvent, cooldownMs: Long) {
        val now = System.currentTimeMillis()
        val year = persistence.currentYear()
        val base = baselineFor(rs.type)
        val baseDelta = when {
            outcome.wasCaught -> base.notorietyLoss
            outcome.success && outcome.moneyGained > 0 -> base.notorietyGain
            outcome.success -> max(1, base.notorietyGain / 2)
            else -> base.notorietyLoss
        }

        val earnedSoFar = persistence.earnedThisYear()
        val applied = if (baseDelta > 0) {
            val remaining = MAX_NOTORIETY_PER_YEAR - earnedSoFar
            min(remaining.coerceAtLeast(0), baseDelta)
        } else baseDelta

        val rec = CrimeRecordEntry(
            id = persistence.newId(),
            timestamp = now,
            typeKey = rs.type.name,
            success = outcome.success,
            caught = outcome.wasCaught,
            money = outcome.moneyGained,
            jailDays = outcome.jailDays,
            year = year,
            summary = summaryForRecord(rs.type, outcome)
        )
        persistence.appendRecord(rec)
        if (applied > 0) persistence.addEarnedThisYear(applied)
        _criminalRecords.update { list -> (list + rec).sortedByDescending { it.timestamp } }
        _playerNotoriety.update { it + applied }

        _cooldownUntil.value = if (cooldownMs > 0) now + cooldownMs else null
    }

    private fun rollWeighted(weights: List<OutcomeWeight>): OutcomeWeight {
        val total = weights.sumOf { it.weight.coerceAtLeast(0) }.coerceAtLeast(1)
        var r = Random.nextInt(total)
        for (w in weights) {
            val slice = w.weight.coerceAtLeast(0)
            if (r < slice) return w
            r -= slice
        }
        return weights.last()
    }

    interface CrimeContentLoader { fun pickScenario(type: CrimeType): CrimeScenario? }

    data class CrimeScenario(
        val durationSeconds: Int,
        val setup: List<String>,
        val execution: List<String>,
        val ambient: List<String>,
        val outcomes: List<OutcomeWeight>,
        val climax: Climax
    ) {
        val setupPortion: Float = 0.18f.coerceIn(0.15f, 0.25f)
        val execPortion: Float = 0.58f.coerceIn(0.50f, 0.70f)

        fun climaxFor(success: Boolean, partial: Boolean, fail: Boolean, caught: Boolean): String =
            when {
                success -> (climax.success ?: climax.generic ?: "It goes your way.")
                partial -> (climax.partial ?: climax.generic ?: "Barely enough to call it a win.")
                fail -> (climax.fail ?: climax.generic ?: "It slips through your fingers.")
                caught -> (climax.caught ?: climax.generic ?: "Hands behind your head.")
                else -> climax.generic ?: "It’s done."
            }

        fun payout(successFull: Boolean): Int = if (successFull) Random.nextInt(120, 500) else 0
        fun jailDays(): Int = Random.nextInt(2, 20)
    }

    data class OutcomeWeight(val kind: OutcomeKind, val weight: Int)
    enum class OutcomeKind { SUCCESS, PARTIAL, FAIL, CAUGHT }

    data class Climax(
        val success: String? = null,
        val partial: String? = null,
        val fail: String? = null,
        val caught: String? = null,
        val generic: String? = null
    )

    interface CrimePersistencePort {
        fun loadSnapshot(): Snapshot
        fun appendRecord(entry: CrimeRecordEntry)
        fun currentYear(): Int
        fun newId(): String
        fun earnedThisYear(): Int
        fun addEarnedThisYear(delta: Int)
        data class Snapshot(val notoriety: Int, val records: List<CrimeRecordEntry>)
    }

    private class InMemoryCrimeContentLoader : CrimeContentLoader {
        override fun pickScenario(type: CrimeType): CrimeScenario {
            val dur = when (type) {
                CrimeType.PICKPOCKETING, CrimeType.SHOPLIFTING -> 22
                CrimeType.DARK_WEB_SALES -> 90
                CrimeType.ART_THEFT -> 220
                else -> 45
            }
            val setup = listOf(
                "You drift into position, eyes scanning for patterns.",
                "The target’s routine plays back in your head."
            )
            val exec = listOf(
                "Hands steady, timing tighter than a snare drum.",
                "One breath, one motion—then you move."
            )
            val ambient = listOf(
                "Distant siren fades under a truck’s downshift.",
                "Neon hum vibrates through the window glass.",
                "Keys jangle; a door latch whispers shut."
            )
            val climax = when (type) {
                CrimeType.ART_THEFT -> Climax(
                    success = "A courier’s engine hum carries you away. The city never knew you were there.",
                    partial = "The frame swaps, but the schedule slips. You ghost out empty-handed.",
                    fail = "A sensor hiccups into a siren. Calm learns to run.",
                    caught = "A docent’s radio says your description aloud."
                )
                else -> Climax(generic = "You fade back into the city.")
            }
            val weights = listOf(
                OutcomeWeight(OutcomeKind.SUCCESS, 55),
                OutcomeWeight(OutcomeKind.PARTIAL, 15),
                OutcomeWeight(OutcomeKind.FAIL, 20),
                OutcomeWeight(OutcomeKind.CAUGHT, 10)
            )
            return CrimeScenario(
                durationSeconds = dur,
                setup = setup,
                execution = exec,
                ambient = ambient,
                outcomes = weights,
                climax = climax
            )
        }
    }

    private class InMemoryCrimePersistence : CrimePersistencePort {
        private var notoriety = 0
        private var earnedThisYear = 0
        private val entries = mutableListOf<CrimeRecordEntry>()

        override fun loadSnapshot(): CrimePersistencePort.Snapshot =
            CrimePersistencePort.Snapshot(notoriety = notoriety, records = entries.toList())

        override fun appendRecord(entry: CrimeRecordEntry) { entries.add(entry) }
        override fun currentYear(): Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        override fun newId(): String = java.util.UUID.randomUUID().toString()
        override fun earnedThisYear(): Int = earnedThisYear
        override fun addEarnedThisYear(delta: Int) {
            notoriety += delta
            earnedThisYear += max(0, delta)
        }
    }
}