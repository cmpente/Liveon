// app/src/main/java/com/liveongames/liveon/viewmodel/CrimeViewModel.kt
package com.liveongames.liveon.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.domain.model.RiskTier
import com.liveongames.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Drop-in, self-contained CrimeViewModel that powers the inline crime flow.
 * No stitching required — all helpers and enums are defined here.
 * It only relies on PlayerRepository for money/notoriety/jail updates.
 */
@HiltViewModel
class CrimeViewModel @Inject constructor(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CrimeViewModel"
        private const val CHARACTER_ID = "player_character"

        /** Cooldown durations (also the in-progress duration) */
        object CrimeCooldownDurationsMs {
            const val LOW = 25_000L
            const val MEDIUM = 35_000L
            const val HIGH = 50_000L
            const val EXTREME = 70_000L
        }

        object CrimeCooldownConfig {
            const val ENABLED = true
            const val PARTIAL_SUCCESS_STARTS_COOLDOWN = false
            const val REDUCED_MOTION_FALLBACK = true
        }

        /** Helper: map risk tier to cooldown/duration */
        fun cooldownForTier(tier: RiskTier): Long = when (tier) {
            RiskTier.LOW_RISK -> CrimeCooldownDurationsMs.LOW
            RiskTier.MEDIUM_RISK -> CrimeCooldownDurationsMs.MEDIUM
            RiskTier.HIGH_RISK -> CrimeCooldownDurationsMs.HIGH
            RiskTier.EXTREME_RISK -> CrimeCooldownDurationsMs.EXTREME
        }
    }

    // ===== Public UI state =====

    /** ms since epoch when crimes unlock; null when unlocked */
    private val _cooldownUntil = MutableStateFlow<Long?>(null)
    val cooldownUntil: StateFlow<Long?> = _cooldownUntil.asStateFlow()

    /** Player notoriety cache (kept in sync on commit) */
    private val _playerNotoriety = MutableStateFlow(0)
    val playerNotoriety: StateFlow<Int> = _playerNotoriety.asStateFlow()

    /** One-shot outcome for the most recent crime */
    data class OutcomeEvent(
        val type: CrimeType,
        val success: Boolean,
        val caught: Boolean,
        val moneyGained: Int,
        val jailDays: Int,
        val notorietyDelta: Int,
        val scenario: String
    )
    private val _lastOutcome = MutableStateFlow<OutcomeEvent?>(null)
    val lastOutcome: StateFlow<OutcomeEvent?> = _lastOutcome.asStateFlow()
    fun consumeOutcome() { _lastOutcome.value = null }

    init {
        // Warm the notoriety value from repo if available
        viewModelScope.launch {
            try {
                playerRepository.getCharacter(CHARACTER_ID).first()?.let { c ->
                    // Assuming your Character model has a notoriety field:
                    val noto = try {
                        val f = c!!::class.java.getDeclaredField("notoriety")
                        f.isAccessible = true
                        (f.get(c) as? Int) ?: 0
                    } catch (_: Exception) { 0 }
                    _playerNotoriety.value = noto
                }
            } catch (_: Exception) {
                // Ignore; we'll keep local state only if repository isn't reachable.
            }
        }
    }

    /** Start a global cooldown and keep crimes disabled until it ends. */
    fun startGlobalCooldown(durationMs: Long) {
        if (!CrimeCooldownConfig.ENABLED) return
        viewModelScope.launch {
            val endTime = System.currentTimeMillis() + durationMs
            _cooldownUntil.value = endTime
            while (System.currentTimeMillis() < endTime) delay(100)
            _cooldownUntil.value = null
        }
    }

    /** Returns a plausible "as it happens" line for the selected crime. */
    fun previewScenario(type: CrimeType): String = when (type) {
        // LOW
        CrimeType.PICKPOCKETING -> listOf(
            "You spot a distracted shopper fumbling for their phone.",
            "A tourist stops to take a photo, bag open on their shoulder.",
            "A gambler counts his winnings in the open."
        ).random()
        CrimeType.SHOPLIFTING -> listOf(
            "You notice a blind spot in the store's cameras.",
            "The fitting rooms are unattended.",
            "Someone else causes a commotion near the registers."
        ).random()
        CrimeType.VANDALISM -> listOf(
            "A rival crew's mural taunts your block.",
            "A politician's poster becomes your canvas.",
            "You tag over a rival's graffiti."
        ).random()
        CrimeType.PETTY_SCAM -> listOf(
            "You 'find' a gold ring and offer to sell it cheap.",
            "You sell fake raffle tickets at a busy market.",
            "You pose as a charity collector."
        ).random()

        // MEDIUM
        CrimeType.MUGGING -> listOf(
            "You corner a lone businessman in a dark alley.",
            "A jogger stops to catch their breath, headphones in.",
            "A tourist wanders into the wrong neighborhood."
        ).random()
        CrimeType.BREAKING_AND_ENTERING -> listOf(
            "You spot a home with lights off and mail piling up.",
            "A back window is left unlocked.",
            "A shopkeeper leaves the rear door ajar."
        ).random()
        CrimeType.DRUG_DEALING -> listOf(
            "A regular asks for a bigger order than usual.",
            "You meet a new buyer at a busy park.",
            "A bar patron discreetly approaches you."
        ).random()
        CrimeType.COUNTERFEIT_GOODS -> listOf(
            "A flea market vendor agrees to move your goods.",
            "Tourists crowd around your street stall.",
            "A promoter buys bulk for giveaways."
        ).random()

        // HIGH
        CrimeType.BURGLARY -> listOf(
            "You disable a small shop's alarm system.",
            "A mansion is left unattended for the weekend.",
            "You find a warehouse with lax security."
        ).random()
        CrimeType.FRAUD -> listOf(
            "You set up a fake donation site.",
            "You skim credit cards at a gas station.",
            "You forge a cashier's check."
        ).random()
        CrimeType.ARMS_SMUGGLING -> listOf(
            "You move a shipment through a border checkpoint.",
            "You sell to a biker gang out of state.",
            "You load crates into a cargo van at night."
        ).random()
        CrimeType.DRUG_TRAFFICKING -> listOf(
            "You drive a van across the state line.",
            "A shipment arrives hidden in produce crates.",
            "You use a fishing boat to transport packages."
        ).random()

        // EXTREME
        CrimeType.ARMED_ROBBERY -> listOf(
            "You storm a jewelry store during peak hours.",
            "You hit an armored truck in transit.",
            "You rob a high-stakes poker game."
        ).random()
        CrimeType.EXTORTION -> listOf(
            "You threaten to leak sensitive photos.",
            "You demand 'protection' money from a nightclub.",
            "You blackmail a corporate executive."
        ).random()
        CrimeType.KIDNAPPING_FOR_RANSOM -> listOf(
            "You grab a wealthy child outside a school.",
            "You abduct a celebrity's assistant.",
            "You take a local politician's spouse."
        ).random()
        CrimeType.PONZI_SCHEME -> listOf(
            "You launch a fake investment firm.",
            "You promise impossible returns to investors.",
            "You use new deposits to pay earlier victims."
        ).random()
        CrimeType.CONTRACT_KILLING -> listOf(
            "You assemble a rifle in a motel bathroom.",
            "A silencer coughs once. The night holds its breath.",
            "You vanish into a crowd before the sirens start."
        ).random()
        CrimeType.DARK_WEB_SALES -> listOf(
            "You sell stolen bank credentials.",
            "You auction off hacking tools.",
            "You ship forged passports overseas."
        ).random()
        CrimeType.ART_THEFT -> listOf(
            "You swap a gallery piece for a perfect replica.",
            "A curator's keycard opens more than doors.",
            "The frame is heavier than it looks."
        ).random()
        CrimeType.DIAMOND_HEIST -> listOf(
            "You rob a diamond exchange vault.",
            "You hit a guarded transport truck.",
            "You infiltrate a mining company's storage."
        ).random()
    }

    /** Commit a crime: compute outcome, update repo, emit OutcomeEvent. */
    fun commitCrime(type: CrimeType) {
        Log.d(TAG, "Attempting to commit crime: $type")
        viewModelScope.launch {
            try {
                val base = createCrime(type)

                val isSuccess = Random.nextDouble() <= base.baseSuccessChance
                val isCaught = Random.nextDouble() < when (base.riskTier) {
                    RiskTier.LOW_RISK -> 0.30
                    RiskTier.MEDIUM_RISK -> 0.50
                    RiskTier.HIGH_RISK -> 0.70
                    RiskTier.EXTREME_RISK -> 0.90
                }

                val money = if (isSuccess) Random.nextInt(base.payoutMin, base.payoutMax + 1) else 0
                val jail = if (isCaught) Random.nextInt(base.jailMin, base.jailMax + 1) else 0
                val notorietyDelta = if (isSuccess) base.notorietyGain else base.notorietyLoss

                if (money > 0) {
                    runCatching { playerRepository.updateMoney(CHARACTER_ID, money) }
                }
                if (notorietyDelta != 0) {
                    runCatching { playerRepository.updateNotoriety(CHARACTER_ID, notorietyDelta) }
                    _playerNotoriety.value = (_playerNotoriety.value + notorietyDelta)
                }
                if (jail > 0) {
                    runCatching { playerRepository.updateJailTime(CHARACTER_ID, jail) }
                }

                _lastOutcome.value = OutcomeEvent(
                    type = type,
                    success = isSuccess,
                    caught = isCaught,
                    moneyGained = money,
                    jailDays = jail,
                    notorietyDelta = notorietyDelta,
                    scenario = base.scenario
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error committing crime", e)
            }
        }
    }

    // ====== Model & factory ======

    /** Local model used for outcome computation and scenario text. */
    private data class CrimeBase(
        val name: String,
        val description: String,
        val riskTier: RiskTier,
        val notorietyRequired: Int,
        val baseSuccessChance: Double,
        val payoutMin: Int,
        val payoutMax: Int,
        val jailMin: Int,
        val jailMax: Int,
        val notorietyGain: Int,
        val notorietyLoss: Int,
        val scenario: String
    )

    @Suppress("LongMethod")
    private fun createCrime(type: CrimeType): CrimeBase {
        fun pick(vararg lines: String) = lines.random()
        return when (type) {
            // ===== LOW RISK =====
            CrimeType.PICKPOCKETING -> CrimeBase(
                name = "Pickpocketing",
                description = "Lift a wallet or phone from an unsuspecting mark.",
                riskTier = RiskTier.LOW_RISK,
                notorietyRequired = 0,
                baseSuccessChance = 0.62,
                payoutMin = 20,
                payoutMax = 180,
                jailMin = 1,
                jailMax = 3,
                notorietyGain = 1,
                notorietyLoss = -1,
                scenario = pick(
                    "You brush past a tourist, fingers light as air.",
                    "You time your move as a train doors chime.",
                    "You bump shoulders; the pocket gives."
                )
            )
            CrimeType.SHOPLIFTING -> CrimeBase(
                name = "Shoplifting",
                description = "Swipe small items from a retail store.",
                riskTier = RiskTier.LOW_RISK,
                notorietyRequired = 0,
                baseSuccessChance = 0.58,
                payoutMin = 30,
                payoutMax = 220,
                jailMin = 1,
                jailMax = 3,
                notorietyGain = 1,
                notorietyLoss = -1,
                scenario = pick(
                    "You glide through the blind spot between cameras.",
                    "Security yawns; you slip the item into your bag.",
                    "You peel off the tag and walk."
                )
            )
            CrimeType.VANDALISM -> CrimeBase(
                name = "Vandalism",
                description = "Deface property to make a statement.",
                riskTier = RiskTier.LOW_RISK,
                notorietyRequired = 0,
                baseSuccessChance = 0.70,
                payoutMin = 0,
                payoutMax = 80,
                jailMin = 1,
                jailMax = 2,
                notorietyGain = 1,
                notorietyLoss = -1,
                scenario = pick(
                    "Fresh paint hisses as your tag blooms.",
                    "You stencil a quick message and vanish.",
                    "You scratch a taunt into polished steel."
                )
            )
            CrimeType.PETTY_SCAM -> CrimeBase(
                name = "Petty Scam",
                description = "Run a small con for quick cash.",
                riskTier = RiskTier.LOW_RISK,
                notorietyRequired = 0,
                baseSuccessChance = 0.60,
                payoutMin = 30,
                payoutMax = 250,
                jailMin = 2,
                jailMax = 4,
                notorietyGain = 1,
                notorietyLoss = -1,
                scenario = pick(
                    "Your 'gold ring' gleams convincingly in the sun.",
                    "You shuffle cups with practiced rhythm.",
                    "You wave a clipboard and smile for donations."
                )
            )

            // ===== MEDIUM RISK =====
            CrimeType.MUGGING -> CrimeBase(
                name = "Mugging",
                description = "Threaten and rob a mark.",
                riskTier = RiskTier.MEDIUM_RISK,
                notorietyRequired = 10,
                baseSuccessChance = 0.45,
                payoutMin = 80,
                payoutMax = 500,
                jailMin = 3,
                jailMax = 7,
                notorietyGain = 2,
                notorietyLoss = -2,
                scenario = pick(
                    "Footsteps quicken; you press the advantage.",
                    "A shadow swallows the alley mouth.",
                    "You whisper: 'Wallet. Now.'"
                )
            )
            CrimeType.BREAKING_AND_ENTERING -> CrimeBase(
                name = "Breaking & Entering",
                description = "Slip into a home or shop.",
                riskTier = RiskTier.MEDIUM_RISK,
                notorietyRequired = 12,
                baseSuccessChance = 0.42,
                payoutMin = 120,
                payoutMax = 800,
                jailMin = 4,
                jailMax = 10,
                notorietyGain = 2,
                notorietyLoss = -2,
                scenario = pick(
                    "A latch yields with a soft click.",
                    "You pry the back window and slide in.",
                    "You kill the fuse and move by phone light."
                )
            )
            CrimeType.DRUG_DEALING -> CrimeBase(
                name = "Drug Dealing",
                description = "Move product to street buyers.",
                riskTier = RiskTier.MEDIUM_RISK,
                notorietyRequired = 15,
                baseSuccessChance = 0.38,
                payoutMin = 150,
                payoutMax = 1200,
                jailMin = 7,
                jailMax = 20,
                notorietyGain = 3,
                notorietyLoss = -3,
                scenario = pick(
                    "The handshake hides a packet.",
                    "You count cash under a streetlight.",
                    "Sirens wail somewhere else. Not here. Not now."
                )
            )
            CrimeType.COUNTERFEIT_GOODS -> CrimeBase(
                name = "Counterfeit Goods",
                description = "Sell knock-offs to eager buyers.",
                riskTier = RiskTier.MEDIUM_RISK,
                notorietyRequired = 18,
                baseSuccessChance = 0.44,
                payoutMin = 120,
                payoutMax = 900,
                jailMin = 5,
                jailMax = 12,
                notorietyGain = 2,
                notorietyLoss = -2,
                scenario = pick(
                    "Your stall opens at dawn with 'designer' merch.",
                    "Bulk order secured; labels look legit.",
                    "You pass handbags off the back of a van."
                )
            )

            // ===== HIGH RISK =====
            CrimeType.BURGLARY -> CrimeBase(
                name = "Burglary",
                description = "Hit higher-value targets with planning.",
                riskTier = RiskTier.HIGH_RISK,
                notorietyRequired = 28,
                baseSuccessChance = 0.32,
                payoutMin = 600,
                payoutMax = 3500,
                jailMin = 12,
                jailMax = 36,
                notorietyGain = 4,
                notorietyLoss = -4,
                scenario = pick(
                    "Glass crunches underfoot in the dark.",
                    "You snake past laser tripwires.",
                    "You crack a small safe, pulse steady."
                )
            )
            CrimeType.FRAUD -> CrimeBase(
                name = "Fraud",
                description = "Forge, skim, and siphon funds.",
                riskTier = RiskTier.HIGH_RISK,
                notorietyRequired = 32,
                baseSuccessChance = 0.30,
                payoutMin = 500,
                payoutMax = 4000,
                jailMin = 10,
                jailMax = 30,
                notorietyGain = 4,
                notorietyLoss = -4,
                scenario = pick(
                    "You deepfake a voice to approve a transfer.",
                    "The card skimmer chirps once — got it.",
                    "You fax a 'verified' cashier's check."
                )
            )
            CrimeType.ARMS_SMUGGLING -> CrimeBase(
                name = "Arms Smuggling",
                description = "Move illegal weapons between buyers.",
                riskTier = RiskTier.HIGH_RISK,
                notorietyRequired = 38,
                baseSuccessChance = 0.26,
                payoutMin = 1000,
                payoutMax = 6000,
                jailMin = 18,
                jailMax = 60,
                notorietyGain = 5,
                notorietyLoss = -5,
                scenario = pick(
                    "Crates clatter in a hidden compartment.",
                    "A border guard waves you through. You don't breathe.",
                    "The buyer checks serial numbers with a nod."
                )
            )
            CrimeType.DRUG_TRAFFICKING -> CrimeBase(
                name = "Drug Trafficking",
                description = "Transport larger shipments for cartels.",
                riskTier = RiskTier.HIGH_RISK,
                notorietyRequired = 40,
                baseSuccessChance = 0.24,
                payoutMin = 1200,
                payoutMax = 7000,
                jailMin = 20,
                jailMax = 72,
                notorietyGain = 5,
                notorietyLoss = -5,
                scenario = pick(
                    "Packages ride under a false floor.",
                    "You switch vehicles under an overpass.",
                    "A fishing boat cuts its engine; hands pass bundles."
                )
            )

            // ===== EXTREME RISK =====
            CrimeType.ARMED_ROBBERY -> CrimeBase(
                name = "Armed Robbery",
                description = "High-stakes robbery with force.",
                riskTier = RiskTier.EXTREME_RISK,
                notorietyRequired = 52,
                baseSuccessChance = 0.18,
                payoutMin = 2000,
                payoutMax = 12000,
                jailMin = 36,
                jailMax = 120,
                notorietyGain = 8,
                notorietyLoss = -8,
                scenario = pick(
                    "Masks on. Time dilates.",
                    "You shout commands; glass rains down.",
                    "You count seconds against the silent alarm."
                )
            )
            CrimeType.EXTORTION -> CrimeBase(
                name = "Extortion",
                description = "Coerce payment with threats.",
                riskTier = RiskTier.EXTREME_RISK,
                notorietyRequired = 56,
                baseSuccessChance = 0.20,
                payoutMin = 1500,
                payoutMax = 9000,
                jailMin = 30,
                jailMax = 96,
                notorietyGain = 7,
                notorietyLoss = -7,
                scenario = pick(
                    "An envelope of photos changes the tone.",
                    "Your 'protection' offer feels non-optional.",
                    "A CEO answers your blocked number."
                )
            )
            CrimeType.KIDNAPPING_FOR_RANSOM -> CrimeBase(
                name = "Kidnapping for Ransom",
                description = "Abduct and negotiate payment.",
                riskTier = RiskTier.EXTREME_RISK,
                notorietyRequired = 60,
                baseSuccessChance = 0.15,
                payoutMin = 5000,
                payoutMax = 30000,
                jailMin = 120,
                jailMax = 365,
                notorietyGain = 10,
                notorietyLoss = -10,
                scenario = pick(
                    "A van door slides shut on a scream.",
                    "You switch safehouses twice before midnight.",
                    "A distorted voice names a price."
                )
            )
            CrimeType.PONZI_SCHEME -> CrimeBase(
                name = "Ponzi Scheme",
                description = "Pay old investors with new money.",
                riskTier = RiskTier.EXTREME_RISK,
                notorietyRequired = 62,
                baseSuccessChance = 0.17,
                payoutMin = 3000,
                payoutMax = 20000,
                jailMin = 60,
                jailMax = 240,
                notorietyGain = 9,
                notorietyLoss = -9,
                scenario = pick(
                    "Charts go up and to the right — until they don't.",
                    "A 'guaranteed' return seals another account.",
                    "You host a seminar at a hotel ballroom."
                )
            )
            CrimeType.CONTRACT_KILLING -> CrimeBase(
                name = "Contract Killing",
                description = "Eliminate a target for a fee.",
                riskTier = RiskTier.EXTREME_RISK,
                notorietyRequired = 70,
                baseSuccessChance = 0.10,
                payoutMin = 8000,
                payoutMax = 50000,
                jailMin = 365,
                jailMax = 999,
                notorietyGain = 12,
                notorietyLoss = -12,
                scenario = pick(
                    "You assemble a rifle in a motel bathroom.",
                    "A silencer coughs once. The night holds its breath.",
                    "You vanish into a crowd before the sirens start."
                )
            )
            CrimeType.DARK_WEB_SALES -> CrimeBase(
                name = "Dark Web Sales",
                description = "Move illegal goods through online markets.",
                riskTier = RiskTier.EXTREME_RISK,
                notorietyRequired = 68,
                baseSuccessChance = 0.22,
                payoutMin = 1200,
                payoutMax = 15000,
                jailMin = 24,
                jailMax = 120,
                notorietyGain = 6,
                notorietyLoss = -6,
                scenario = pick(
                    "PGP lights up as orders flood in.",
                    "You ship 'souvenirs' with a fake return address.",
                    "A moderator flags your listing — or does he?"
                )
            )
            CrimeType.ART_THEFT -> CrimeBase(
                name = "Art Theft",
                description = "Steal priceless works of art.",
                riskTier = RiskTier.EXTREME_RISK,
                notorietyRequired = 66,
                baseSuccessChance = 0.14,
                payoutMin = 5000,
                payoutMax = 40000,
                jailMin = 48,
                jailMax = 240,
                notorietyGain = 9,
                notorietyLoss = -9,
                scenario = pick(
                    "You swap a gallery piece for a perfect replica.",
                    "A curator's keycard opens more than doors.",
                    "The frame is heavier than it looks."
                )
            )
            CrimeType.DIAMOND_HEIST -> CrimeBase(
                name = "Diamond Heist",
                description = "Rob vaults and transports for diamonds.",
                riskTier = RiskTier.EXTREME_RISK,
                notorietyRequired = 75,
                baseSuccessChance = 0.08,
                payoutMin = 10000,
                payoutMax = 100000,
                jailMin = 365,
                jailMax = 1200,
                notorietyGain = 15,
                notorietyLoss = -15,
                scenario = pick(
                    "A drill bites into steel — sparks and prayers.",
                    "You time the guard rotation to the second.",
                    "You leave a playing card where the vault used to be."
                )
            )
        }
    }

    // ====== Public API ======

    enum class CrimeType {
        // LOW RISK
        PICKPOCKETING, SHOPLIFTING, VANDALISM, PETTY_SCAM,

        // MEDIUM RISK
        MUGGING, BREAKING_AND_ENTERING, DRUG_DEALING, COUNTERFEIT_GOODS,

        // HIGH RISK
        BURGLARY, FRAUD, ARMS_SMUGGLING, DRUG_TRAFFICKING,

        // EXTREME RISK
        ARMED_ROBBERY, EXTORTION, KIDNAPPING_FOR_RANSOM, PONZI_SCHEME,
        CONTRACT_KILLING, DARK_WEB_SALES, ART_THEFT, DIAMOND_HEIST
    }
}
