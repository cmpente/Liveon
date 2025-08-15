// app/src/main/java/com/liveongames/liveon/viewmodel/CrimeViewModel.kt
package com.liveongames.liveon.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.liveongames.domain.model.RiskTier
import com.liveongames.domain.repository.PlayerRepository
import com.liveongames.liveon.util.JsonAssetLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class CrimeViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    companion object {
        private const val TAG = "CrimeViewModel"
        private const val CHARACTER_ID = "player_character"

        // Progress durations (ms)
        private const val LOW_MS = 20_000L
        private const val MED_MS = 60_000L
        private const val HIGH_MS = 180_000L
        private const val EXTREME_MS = 300_000L

        // Phase thresholds (fraction of total duration)
        private const val SETUP_END = 0.25f
        private const val EXEC_END = 0.85f

        // UI refresh
        private const val TICK_MS = 100L
        private const val MSG_SWAP_MS = 3000L

        // Optional short lockout after a failed outcome (adds a little sting)
        private const val FAILURE_LOCKOUT_MS = 6_000L
    }

    /* ============================== Public UI State ============================== */

    /** Crime types (leave as-is if you already expose this enum) */
    enum class CrimeType {
        // LOW
        PICKPOCKETING, SHOPLIFTING, VANDALISM, PETTY_SCAM,
        // MED
        MUGGING, BREAKING_AND_ENTERING, DRUG_DEALING, COUNTERFEIT_GOODS,
        // HIGH
        BURGLARY, FRAUD, ARMS_SMUGGLING, DRUG_TRAFFICKING,
        // EXTREME
        ARMED_ROBBERY, EXTORTION, KIDNAPPING_FOR_RANSOM, PONZI_SCHEME,
        CONTRACT_KILLING, DARK_WEB_SALES, ART_THEFT, DIAMOND_HEIST
    }

    data class OutcomeEvent(
        val type: CrimeType,
        val success: Boolean,
        val caught: Boolean,
        val moneyGained: Int,
        val jailDays: Int,
        val notorietyDelta: Int,
        /** The final “climax” narration matching the revealed outcome */
        val climaxLine: String
    )

    private val _lastOutcome = MutableStateFlow<OutcomeEvent?>(null)
    val lastOutcome: StateFlow<OutcomeEvent?> = _lastOutcome.asStateFlow()
    fun consumeOutcome() { _lastOutcome.value = null }

    /** Global lockout (also used while a run is in progress) */
    private val _cooldownUntil = MutableStateFlow<Long?>(null)
    val cooldownUntil: StateFlow<Long?> = _cooldownUntil.asStateFlow()

    /** Notoriety (mirrors repo when available) */
    private val _playerNotoriety = MutableStateFlow(0)
    val playerNotoriety: StateFlow<Int> = _playerNotoriety.asStateFlow()

    enum class Phase { SETUP, EXECUTION, CLIMAX }

    data class CrimeRunState(
        val type: CrimeType,
        val startedAtMs: Long,
        val durationMs: Long,
        val progress: Float,
        val phase: Phase,
        val currentMessage: String
    )

    private val _runState = MutableStateFlow<CrimeRunState?>(null)
    val runState: StateFlow<CrimeRunState?> = _runState.asStateFlow()

    /* ============================== Narrative assets ============================== */

    private data class OutcomesWeight(@SerializedName("type") val type: String, val weight: Int)
    private data class ClimaxText(val success: String, val fail: String, val caught: String)
    private data class CrimePath(
        val setup: List<String>,
        val execution: List<String>,
        val climax: ClimaxText,
        val outcomes: List<OutcomesWeight>
    )
    private data class CrimeDef(val type: String, val paths: List<CrimePath>)
    private data class CrimesPack(val crimes: List<CrimeDef>)

    private val narrativeMap = mutableMapOf<CrimeType, List<CrimePath>>()
    private var narrativeLoaded = false
    private var runJob: Job? = null

    private suspend fun ensureNarrativesLoaded() {
        if (narrativeLoaded) return
        val loader = JsonAssetLoader(appContext)
        val gson = Gson()
        val files = listOf(
            "street_crimes.json",
            "robbery_crimes.json",
            "heists_and_smuggling_crimes.json",
            "mastermind_crimes.json"
        )
        val type = object : TypeToken<CrimesPack>() {}.type
        for (file in files) {
            runCatching {
                val json = loader.readRaw(file)
                val pack: CrimesPack = gson.fromJson(json, type)
                pack.crimes.forEach { cd ->
                    runCatching {
                        val ct = CrimeType.valueOf(cd.type)
                        narrativeMap[ct] = cd.paths
                    }
                }
            }
        }
        narrativeLoaded = true
    }

    /* ============================== Public API ============================== */

    fun beginCrime(type: CrimeType) {
        if (_runState.value != null) return // already running
        viewModelScope.launch {
            ensureNarrativesLoaded()
            val base = createBase(type) // payout/jail/notoriety baselines
            val duration = durationForTier(base.riskTier)

            val path = narrativeMap[type]?.randomOrNull()
            val firstLine = path?.setup?.randomOrNull()
                ?: previewScenario(type) // fallback seed line

            val start = System.currentTimeMillis()
            val end = start + duration
            _cooldownUntil.value = end // lock during run

            var lastMsgAt = start
            var currentMsg = firstLine
            var lastPhase: Phase? = null

            runJob = viewModelScope.launch {
                while (true) {
                    val now = System.currentTimeMillis()
                    val frac = ((now - start).coerceAtLeast(0).toFloat() / duration.toFloat())
                        .coerceIn(0f, 1f)

                    val phase = when {
                        frac < SETUP_END -> Phase.SETUP
                        frac < EXEC_END -> Phase.EXECUTION
                        else -> Phase.CLIMAX
                    }

                    // Rotate narrative line every MSG_SWAP_MS within phase (pre-reveal only)
                    if (now - lastMsgAt >= MSG_SWAP_MS) {
                        val list = when (phase) {
                            Phase.SETUP -> path?.setup
                            Phase.EXECUTION, Phase.CLIMAX -> path?.execution
                        } ?: emptyList()
                        if (list.isNotEmpty()) {
                            val next = list.random()
                            if (next != currentMsg) currentMsg = next
                            lastMsgAt = now
                        }
                    }

                    if (lastPhase != phase || _runState.value == null || _runState.value?.currentMessage != currentMsg) {
                        _runState.value = CrimeRunState(
                            type = type,
                            startedAtMs = start,
                            durationMs = duration,
                            progress = frac,
                            phase = phase,
                            currentMessage = currentMsg ?: ""
                        )
                        lastPhase = phase
                    }

                    if (now >= end) break
                    delay(TICK_MS)
                }

                // Timer finished: decide the outcome now (no mid-phase reveals).
                val outcome = chooseOutcome(path)
                val isSuccess = outcome == "SUCCESS"
                val isCaught = outcome == "CAUGHT"

                val money = if (isSuccess) Random.nextInt(base.payoutMin, base.payoutMax + 1) else 0
                val jail = if (isCaught) Random.nextInt(base.jailMin, base.jailMax + 1) else 0
                val notoDelta = if (isSuccess) base.notorietyGain else base.notorietyLoss

                // Apply repo effects (best-effort; safe if repo absent)
                runCatching {
                    if (money > 0) playerRepository.updateMoney(CHARACTER_ID, money)
                    if (notoDelta != 0) {
                        playerRepository.updateNotoriety(CHARACTER_ID, notoDelta)
                        _playerNotoriety.value = _playerNotoriety.value + notoDelta
                    }
                    if (jail > 0) playerRepository.updateJailTime(CHARACTER_ID, jail)
                }

                val climaxLine = when {
                    isSuccess -> path?.climax?.success
                    isCaught -> path?.climax?.caught
                    else -> path?.climax?.fail
                } ?: ""

                _lastOutcome.value = OutcomeEvent(
                    type = type,
                    success = isSuccess,
                    caught = isCaught,
                    moneyGained = money,
                    jailDays = jail,
                    notorietyDelta = notoDelta,
                    climaxLine = climaxLine
                )

                // Post-result short lockout if failed (adds drama + fits police flash)
                _cooldownUntil.value = if (!isSuccess) System.currentTimeMillis() + FAILURE_LOCKOUT_MS else null
                _runState.value = null
                runJob = null
            }
        }
    }

    /** Cancel current crime — no rewards or consequences, crimes unlock immediately. */
    fun cancelCrime() {
        runJob?.cancel()
        runJob = null
        _runState.value = null
        _cooldownUntil.value = null
    }

    /* ============================== Helpers ============================== */

    private fun durationForTier(t: RiskTier) = when (t) {
        RiskTier.LOW_RISK -> LOW_MS
        RiskTier.MEDIUM_RISK -> MED_MS
        RiskTier.HIGH_RISK -> HIGH_MS
        RiskTier.EXTREME_RISK -> EXTREME_MS
    }

    /** Weighted outcome chooser with sensible fallback to base chances. */
    private fun chooseOutcome(path: CrimePath?): String {
        if (path != null && path.outcomes.isNotEmpty()) {
            val total = path.outcomes.sumOf { it.weight }.coerceAtLeast(1)
            var roll = Random.nextInt(total)
            for (o in path.outcomes) {
                if (roll < o.weight) return o.type.uppercase()
                roll -= o.weight
            }
        }
        // Fallback: modest success; detection scales by risk
        return listOf("SUCCESS", "FAIL", "CAUGHT").random()
    }

    /** Short one-liner used before start or as fallback if assets missing. */
    fun previewScenario(type: CrimeType): String = when (type) {
        // LOW
        CrimeType.PICKPOCKETING -> listOf(
            "You brush past a tourist, fingers light as air.",
            "You time your move as train doors chime.",
            "You bump shoulders; the pocket gives."
        ).random()
        CrimeType.SHOPLIFTING -> listOf(
            "A camera blind spot opens for a heartbeat.",
            "The fitting rooms are unattended.",
            "Security yawns as you slip the tag."
        ).random()
        CrimeType.VANDALISM -> listOf(
            "Fresh paint hisses as your tag blooms.",
            "You stencil a quick message and vanish."
        ).random()
        CrimeType.PETTY_SCAM -> listOf(
            "You work the shell game with practiced ease.",
            "A forged receipt buys you a door."
        ).random()
        // MED
        CrimeType.MUGGING -> listOf(
            "An alley throat clears. Footsteps quicken.",
            "You pick a mark near a broken light."
        ).random()
        CrimeType.BREAKING_AND_ENTERING -> listOf(
            "A latch gives under a practiced twist.",
            "Window. Crowbar. Quiet."
        ).random()
        CrimeType.DRUG_DEALING -> listOf(
            "A handshake lasts a second too long.",
            "The meetup ping hits your burner."
        ).random()
        CrimeType.COUNTERFEIT_GOODS -> listOf(
            "Designer fakes spill from a trunk.",
            "Stamps, seals, and a nervous smile."
        ).random()
        // HIGH
        CrimeType.BURGLARY -> listOf(
            "You listen to the house breathe, then move.",
            "Glass whispers. Gloves glide."
        ).random()
        CrimeType.FRAUD -> listOf(
            "An inbox quivers with too-good promises.",
            "Numbers dance; the balance tips."
        ).random()
        CrimeType.ARMS_SMUGGLING -> listOf(
            "Crates vanish into a midnight van.",
            "A handshake at a lonely checkpoint."
        ).random()
        CrimeType.DRUG_TRAFFICKING -> listOf(
            "A fishing boat rides low in the water.",
            "Produce crates hide more than citrus."
        ).random()
        // EXTREME
        CrimeType.ARMED_ROBBERY -> listOf(
            "Masks up. Heart drums. Doors in.",
            "You count steps, not seconds."
        ).random()
        CrimeType.EXTORTION -> listOf(
            "One message. Ten meanings. All sharp.",
            "The club’s back office goes quiet."
        ).random()
        CrimeType.KIDNAPPING_FOR_RANSOM -> listOf(
            "A van idles like a held breath.",
            "The route maps glow on your screen."
        ).random()
        CrimeType.PONZI_SCHEME -> listOf(
            "Charts ascend; truth descends.",
            "Meetings stack; promises echo."
        ).random()
        CrimeType.CONTRACT_KILLING -> listOf(
            "A silencer coughs once; the night holds its breath.",
            "A window opens, a future closes."
        ).random()
        CrimeType.DARK_WEB_SALES -> listOf(
            "A wallet drains behind seven proxies.",
            "A new identity clears across oceans."
        ).random()
        CrimeType.ART_THEFT -> listOf(
            "A replica smiles at a laser grid.",
            "A curator’s keycard sings."
        ).random()
        CrimeType.DIAMOND_HEIST -> listOf(
            "A vault listens; you answer in beeps.",
            "A transport slows at just the wrong light."
        ).random()
    }

    /* ============================== Outcome Baselines ============================== */
    private data class CrimeBase(
        val riskTier: RiskTier,
        val payoutMin: Int,
        val payoutMax: Int,
        val jailMin: Int,
        val jailMax: Int,
        val notorietyGain: Int,
        val notorietyLoss: Int
    )

    // Keep simple baselines (unchanged from earlier versions)
    private fun createBase(type: CrimeType): CrimeBase {
        fun base(
            tier: RiskTier, payMin: Int, payMax: Int,
            jailMin: Int, jailMax: Int, notoGain: Int, notoLoss: Int
        ) = CrimeBase(tier, payMin, payMax, jailMin, jailMax, notoGain, notoLoss)

        return when (type) {
            // LOW
            CrimeType.PICKPOCKETING -> base(RiskTier.LOW_RISK, 20, 180, 1, 3, 1, -1)
            CrimeType.SHOPLIFTING -> base(RiskTier.LOW_RISK, 30, 220, 1, 3, 1, -1)
            CrimeType.VANDALISM -> base(RiskTier.LOW_RISK, 0, 80, 1, 2, 1, -1)
            CrimeType.PETTY_SCAM -> base(RiskTier.LOW_RISK, 60, 320, 1, 3, 1, -1)

            // MED
            CrimeType.MUGGING -> base(RiskTier.MEDIUM_RISK, 120, 600, 3, 14, 2, -2)
            CrimeType.BREAKING_AND_ENTERING -> base(RiskTier.MEDIUM_RISK, 80, 400, 3, 21, 2, -2)
            CrimeType.DRUG_DEALING -> base(RiskTier.MEDIUM_RISK, 200, 1600, 3, 21, 2, -2)
            CrimeType.COUNTERFEIT_GOODS -> base(RiskTier.MEDIUM_RISK, 200, 900, 3, 21, 2, -2)

            // HIGH
            CrimeType.BURGLARY -> base(RiskTier.HIGH_RISK, 600, 3600, 30, 180, 4, -2)
            CrimeType.FRAUD -> base(RiskTier.HIGH_RISK, 900, 4800, 60, 365, 4, -2)
            CrimeType.ARMS_SMUGGLING -> base(RiskTier.HIGH_RISK, 2000, 12000, 365, 1095, 4, -2)
            CrimeType.DRUG_TRAFFICKING -> base(RiskTier.HIGH_RISK, 5000, 45000, 365, 1095, 5, -2)

            // EXTREME
            CrimeType.ARMED_ROBBERY -> base(RiskTier.EXTREME_RISK, 20000, 120000, 1460, 4380, 10, -3)
            CrimeType.EXTORTION -> base(RiskTier.EXTREME_RISK, 5000, 40000, 730, 2190, 10, -3)
            CrimeType.KIDNAPPING_FOR_RANSOM -> base(RiskTier.EXTREME_RISK, 50000, 400000, 2190, 5475, 10, -3)
            CrimeType.PONZI_SCHEME -> base(RiskTier.EXTREME_RISK, 150000, 900000, 1460, 4380, 10, -3)
            CrimeType.CONTRACT_KILLING -> base(RiskTier.EXTREME_RISK, 100000, 450000, 2190, 5475, 10, -3)
            CrimeType.DARK_WEB_SALES -> base(RiskTier.EXTREME_RISK, 15000, 220000, 365, 1460, 8, -3)
            CrimeType.ART_THEFT -> base(RiskTier.EXTREME_RISK, 250000, 4_800_000, 2920, 7300, 10, -3)
            CrimeType.DIAMOND_HEIST -> base(RiskTier.EXTREME_RISK, 1_500_000, 9_500_000, 2920, 7300, 10, -3)
        }
    }

    init {
        // Try to mirror notoriety from repository (best effort)
        viewModelScope.launch {
            runCatching { playerRepository.getCharacter(CHARACTER_ID).first() }
                .getOrNull()
                ?.let { c ->
                    // If your Character model has notoriety, use it
                    runCatching {
                        val f = c::class.java.getDeclaredField("notoriety")
                        f.isAccessible = true
                        _playerNotoriety.value = (f.get(c) as? Int) ?: 0
                    }
                }
        }
    }
}
