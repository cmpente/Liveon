package com.liveongames.liveon.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.data.assets.crime.CrimeAssetLoader
import com.liveongames.data.assets.crime.CrimeAssetLoader.OutcomeType
import com.liveongames.domain.model.RiskTier
import com.liveongames.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.max
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
    private val crimeAssets: CrimeAssetLoader,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    companion object {
        private const val CHARACTER_ID = "player_character"

        // Fallback durations by tier (ms)
        private const val LOW_MS = 20_000L
        private const val MED_MS = 60_000L
        private const val HIGH_MS = 180_000L
        private const val EXTREME_MS = 300_000L

        // VM tick; UI renders smoothly on its own
        private const val TICK_MS = 100L

        // Post-fail/caught soft lock
        private const val FAILURE_LOCKOUT_MS = 6_000L
    }

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

    enum class Phase { SETUP, EXECUTION, CLIMAX }

    data class CrimeRunState(
        val type: CrimeType,
        val startedAtMs: Long,
        val durationMs: Long,
        val progress: Float,          // 0f..1f across the whole run
        val phase: Phase,
        val phaseStartMs: Long,
        val phaseEndMs: Long,
        val phaseLines: List<String>, // primary lines for the current phase
        val ambientLines: List<String>, // optional mood/ambient lines
        val currentMessage: String
    )

    data class OutcomeEvent(
        val type: CrimeType,
        val success: Boolean,
        val wasCaught: Boolean,
        val moneyGained: Int,
        val jailDays: Int,
        val notorietyDelta: Int,
        val climaxLine: String
    )

    private val _runState = MutableStateFlow<CrimeRunState?>(null)
    val runState: StateFlow<CrimeRunState?> = _runState.asStateFlow()

    private val _lastOutcome = MutableStateFlow<OutcomeEvent?>(null)
    val lastOutcome: StateFlow<OutcomeEvent?> = _lastOutcome.asStateFlow()
    fun consumeOutcome() { _lastOutcome.value = null }

    private val _cooldownUntil = MutableStateFlow<Long?>(null)
    val cooldownUntil: StateFlow<Long?> = _cooldownUntil.asStateFlow()

    private val _playerNotoriety = MutableStateFlow(0)
    val playerNotoriety: StateFlow<Int> = _playerNotoriety.asStateFlow()

    private var runJob: Job? = null

    // Load-once cache of the merged bank
    private val bank by lazy { crimeAssets.loadBank() }

    init {
        // Best-effort read of notoriety from Player repo
        viewModelScope.launch {
            runCatching { playerRepository.getCharacter(CHARACTER_ID).first() }
                .getOrNull()
                ?.let { c ->
                    runCatching {
                        val f = c::class.java.getDeclaredField("notoriety")
                        f.isAccessible = true
                        _playerNotoriety.value = (f.get(c) as? Int) ?: 0
                    }
                }
        }
    }

    /** Start a crime run. */
    fun beginCrime(type: CrimeType) {
        if (_runState.value != null) return // already running

        val asset = bank.byKey[type.name] ?: run {
            beginFallbackRun(type); return
        }
        val path = crimeAssets.pickPath(asset, Random.Default)

        val durationSec = asset.durationSeconds ?: (durationForTier(riskFor(type)) / 1000L).toInt()
        val durationMs = durationSec * 1000L

        val start = System.currentTimeMillis()
        val end = start + durationMs
        _cooldownUntil.value = end // lock during the run

        // 25/55/20 default split (kept stable for UI); can be extended to use per-pack defaults if desired
        val setupUntil = start + (durationMs * 0.25f).toLong()
        val execUntil  = start + (durationMs * 0.80f).toLong()

        fun linesFor(phase: Phase): List<String> = when (phase) {
            Phase.SETUP     -> path.setup
            Phase.EXECUTION -> path.execution
            Phase.CLIMAX    -> if (path.execution.isNotEmpty()) path.execution else path.setup
        }
        fun ambientFor(@Suppress("UNUSED_PARAMETER") phase: Phase): List<String> = path.ambient

        var lastPhase: Phase? = null

        runJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val elapsedMs = (now - start).coerceAtLeast(0L)
                val frac = (elapsedMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

                val uiPhase = when {
                    now < setupUntil -> Phase.SETUP
                    now < execUntil  -> Phase.EXECUTION
                    else             -> Phase.CLIMAX
                }

                val pStart = when (uiPhase) {
                    Phase.SETUP     -> start
                    Phase.EXECUTION -> setupUntil
                    Phase.CLIMAX    -> execUntil
                }
                val pEnd = when (uiPhase) {
                    Phase.SETUP     -> setupUntil
                    Phase.EXECUTION -> execUntil
                    Phase.CLIMAX    -> end
                }
                val lines = linesFor(uiPhase)
                val amb = ambientFor(uiPhase)

                if (lastPhase != uiPhase || _runState.value == null ||
                    _runState.value?.progress != frac ||
                    _runState.value?.currentMessage != (lines.firstOrNull() ?: _runState.value?.currentMessage ?: "")
                ) {
                    _runState.value = CrimeRunState(
                        type = type,
                        startedAtMs = start,
                        durationMs = durationMs,
                        progress = frac,
                        phase = uiPhase,
                        phaseStartMs = pStart,
                        phaseEndMs = pEnd,
                        phaseLines = lines,
                        ambientLines = amb,
                        currentMessage = lines.firstOrNull()
                            ?: path.execution.firstOrNull()
                            ?: path.setup.firstOrNull()
                            ?: previewScenario(type)
                    )
                    lastPhase = uiPhase
                }

                if (now >= end) break
                delay(TICK_MS)
            }

            // Resolve at completion
            val outcome = crimeAssets.pickOutcome(path)
            val base = createBase(type)

            val (isSuccess, isCaught) = when (outcome) {
                OutcomeType.SUCCESS -> true to false
                OutcomeType.PARTIAL -> true to false
                OutcomeType.FAIL    -> false to false
                OutcomeType.CAUGHT  -> false to true
            }

            val money = when (outcome) {
                OutcomeType.SUCCESS -> Random.nextInt(base.payoutMin, base.payoutMax + 1)
                OutcomeType.PARTIAL -> Random.nextInt(max(0, base.payoutMin / 3), max(1, base.payoutMax / 2) + 1)
                else -> 0
            }
            val jail = if (isCaught) Random.nextInt(base.jailMin, base.jailMax + 1) else 0
            val notorietyDelta = when (outcome) {
                OutcomeType.SUCCESS -> base.notorietyGain
                OutcomeType.PARTIAL -> max(1, base.notorietyGain / 2)
                else -> base.notorietyLoss
            }

            runCatching {
                if (money > 0) playerRepository.updateMoney(CHARACTER_ID, money)
                if (notorietyDelta != 0) {
                    playerRepository.updateNotoriety(CHARACTER_ID, notorietyDelta)
                    _playerNotoriety.value = _playerNotoriety.value + notorietyDelta
                }
                if (jail > 0) playerRepository.updateJailTime(CHARACTER_ID, jail)
            }

            _lastOutcome.value = OutcomeEvent(
                type = type,
                success = isSuccess,
                wasCaught = isCaught,
                moneyGained = money,
                jailDays = jail,
                notorietyDelta = notorietyDelta,
                climaxLine = crimeAssets.finalLine(path, outcome).orEmpty()
            )

            _cooldownUntil.value = if (!isSuccess) System.currentTimeMillis() + FAILURE_LOCKOUT_MS else null
            _runState.value = null
            runJob = null
        }
    }

    /** Public cancel used by UI. */
    fun cancelCrime() {
        runJob?.cancel()
        runJob = null
        _runState.value = null
        _cooldownUntil.value = null
    }

    // ---- Fallback minimal run if assets are missing (keeps UI functional) ----
    private fun beginFallbackRun(type: CrimeType) {
        val durationMs = durationForTier(riskFor(type))
        val start = System.currentTimeMillis()
        val end = start + durationMs
        _cooldownUntil.value = end

        val setupUntil = start + (durationMs * 0.25f).toLong()
        val execUntil  = start + (durationMs * 0.80f).toLong()

        var lastPhase: Phase? = null

        runJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val frac = ((now - start).coerceAtLeast(0).toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                val uiPhase = when {
                    now < setupUntil -> Phase.SETUP
                    now < execUntil  -> Phase.EXECUTION
                    else             -> Phase.CLIMAX
                }

                val pStart = when (uiPhase) {
                    Phase.SETUP     -> start
                    Phase.EXECUTION -> setupUntil
                    Phase.CLIMAX    -> execUntil
                }
                val pEnd = when (uiPhase) {
                    Phase.SETUP     -> setupUntil
                    Phase.EXECUTION -> execUntil
                    Phase.CLIMAX    -> end
                }
                val lines = listOf(previewScenario(type))

                if (lastPhase != uiPhase || _runState.value == null || _runState.value?.progress != frac) {
                    _runState.value = CrimeRunState(
                        type = type,
                        startedAtMs = start,
                        durationMs = durationMs,
                        progress = frac,
                        phase = uiPhase,
                        phaseStartMs = pStart,
                        phaseEndMs = pEnd,
                        phaseLines = lines,
                        ambientLines = emptyList(),
                        currentMessage = lines.first()
                    )
                    lastPhase = uiPhase
                }

                if (now >= end) break
                delay(TICK_MS)
            }

            val base = createBase(type)
            _lastOutcome.value = OutcomeEvent(
                type = type, success = false, wasCaught = false,
                moneyGained = 0, jailDays = 0, notorietyDelta = base.notorietyLoss,
                climaxLine = ""
            )
            _cooldownUntil.value = System.currentTimeMillis() + FAILURE_LOCKOUT_MS
            _runState.value = null
            runJob = null
        }
    }

    // ---- Durations / baselines ----
    private fun durationForTier(t: RiskTier) = when (t) {
        RiskTier.LOW_RISK     -> LOW_MS
        RiskTier.MEDIUM_RISK  -> MED_MS
        RiskTier.HIGH_RISK    -> HIGH_MS
        RiskTier.EXTREME_RISK -> EXTREME_MS
    }

    fun riskFor(type: CrimeType): RiskTier = when (type) {
        CrimeType.PICKPOCKETING, CrimeType.SHOPLIFTING, CrimeType.VANDALISM, CrimeType.PETTY_SCAM -> RiskTier.LOW_RISK
        CrimeType.MUGGING, CrimeType.BREAKING_AND_ENTERING, CrimeType.DRUG_DEALING, CrimeType.COUNTERFEIT_GOODS -> RiskTier.MEDIUM_RISK
        CrimeType.BURGLARY, CrimeType.FRAUD, CrimeType.ARMS_SMUGGLING, CrimeType.DRUG_TRAFFICKING -> RiskTier.HIGH_RISK
        else -> RiskTier.EXTREME_RISK
    }

    private data class CrimeBase(
        val riskTier: RiskTier,
        val payoutMin: Int,
        val payoutMax: Int,
        val jailMin: Int,
        val jailMax: Int,
        val notorietyGain: Int,
        val notorietyLoss: Int
    )

    private fun createBase(type: CrimeType): CrimeBase {
        fun base(tier: RiskTier, a: Int, b: Int, jm: Int, jx: Int, ng: Int, nl: Int) =
            CrimeBase(tier, a, b, jm, jx, ng, nl)

        return when (type) {
            // LOW
            CrimeType.PICKPOCKETING      -> base(RiskTier.LOW_RISK, 20, 180, 1, 3, 1, -1)
            CrimeType.SHOPLIFTING        -> base(RiskTier.LOW_RISK, 30, 220, 1, 3, 1, -1)
            CrimeType.VANDALISM          -> base(RiskTier.LOW_RISK, 0, 80, 1, 2, 1, -1)
            CrimeType.PETTY_SCAM         -> base(RiskTier.LOW_RISK, 60, 320, 1, 3, 1, -1)
            // MED
            CrimeType.MUGGING            -> base(RiskTier.MEDIUM_RISK, 120, 600, 3, 14, 2, -2)
            CrimeType.BREAKING_AND_ENTERING -> base(RiskTier.MEDIUM_RISK, 80, 400, 3, 21, 2, -2)
            CrimeType.DRUG_DEALING       -> base(RiskTier.MEDIUM_RISK, 200, 1600, 3, 21, 2, -2)
            CrimeType.COUNTERFEIT_GOODS  -> base(RiskTier.MEDIUM_RISK, 200, 900, 3, 21, 2, -2)
            // HIGH
            CrimeType.BURGLARY           -> base(RiskTier.HIGH_RISK, 600, 3600, 30, 180, 4, -2)
            CrimeType.FRAUD              -> base(RiskTier.HIGH_RISK, 900, 4800, 60, 365, 4, -2)
            CrimeType.ARMS_SMUGGLING     -> base(RiskTier.HIGH_RISK, 2000, 12000, 365, 1095, 4, -2)
            CrimeType.DRUG_TRAFFICKING   -> base(RiskTier.HIGH_RISK, 5000, 45000, 365, 1095, 5, -2)
            // EXTREME
            CrimeType.ARMED_ROBBERY      -> base(RiskTier.EXTREME_RISK, 20000, 120000, 1460, 4380, 10, -3)
            CrimeType.EXTORTION          -> base(RiskTier.EXTREME_RISK, 5000, 40000, 730, 2190, 10, -3)
            CrimeType.KIDNAPPING_FOR_RANSOM -> base(RiskTier.EXTREME_RISK, 50000, 400000, 2190, 5475, 10, -3)
            CrimeType.PONZI_SCHEME       -> base(RiskTier.EXTREME_RISK, 150000, 900000, 1460, 4380, 10, -3)
            CrimeType.CONTRACT_KILLING   -> base(RiskTier.EXTREME_RISK, 100000, 450000, 2190, 5475, 10, -3)
            CrimeType.DARK_WEB_SALES     -> base(RiskTier.EXTREME_RISK, 15000, 220000, 365, 1460, 8, -3)
            CrimeType.ART_THEFT          -> base(RiskTier.EXTREME_RISK, 250000, 4_800_000, 2920, 7300, 10, -3)
            CrimeType.DIAMOND_HEIST      -> base(RiskTier.EXTREME_RISK, 1_500_000, 9_500_000, 2920, 7300, 10, -3)
        }
    }

    // ---- Short preview lines for list rows ----
    fun previewScenario(type: CrimeType): String = when (type) {
        CrimeType.PICKPOCKETING -> listOf(
            "You brush past a tourist, fingers light as air.",
            "You time your move as train doors chime."
        ).random()
        CrimeType.SHOPLIFTING -> listOf(
            "A camera blind spot opens for a heartbeat.",
            "The fitting rooms are unattended."
        ).random()
        CrimeType.VANDALISM -> listOf(
            "Fresh paint hisses as your tag blooms.",
            "You stencil a quick message and vanish."
        ).random()
        CrimeType.PETTY_SCAM -> listOf(
            "You work the shell game with practiced ease.",
            "A forged receipt buys you a door."
        ).random()
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
}
