// app/src/main/java/com/liveongames/liveon/viewmodel/CrimeViewModel.kt
package com.liveongames.liveon.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.domain.model.*
import com.liveongames.domain.repository.CrimeRepository
import com.liveongames.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random
import javax.inject.Inject

@HiltViewModel
class CrimeViewModel @Inject constructor(
    private val crimeRepository: CrimeRepository,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    companion object {
        private const val CHARACTER_ID = "player_character"
        private const val TAG = "CrimeViewModel"
    }

    private val _crimes = MutableStateFlow<List<Crime>>(emptyList())
    val crimes: StateFlow<List<Crime>> = _crimes.asStateFlow()

    private val _playerNotoriety = MutableStateFlow(0)
    val playerNotoriety: StateFlow<Int> = _playerNotoriety.asStateFlow()

    init {
        observeCrimes()
        observePlayerNotoriety()
        viewModelScope.launch {
            ensureCharacterExists()
        }
    }

    private fun observeCrimes() {
        Log.d(TAG, "Observing crimes...")
        viewModelScope.launch {
            try {
                crimeRepository.getCrimes().collect { crimeList ->
                    Log.d(TAG, "Observed crime list update: ${crimeList.size} crimes")
                    if (_crimes.value != crimeList) {
                        _crimes.value = crimeList
                        Log.d(TAG, "Updated _crimes StateFlow with ${crimeList.size} crimes")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing crimes", e)
            }
        }
    }

    private fun observePlayerNotoriety() {
        viewModelScope.launch {
            try {
                playerRepository.getCharacter(CHARACTER_ID).first()?.let { character ->
                    _playerNotoriety.value = character.notoriety
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing player notoriety", e)
            }
        }
    }

    fun commitCrime(type: CrimeType) {
        Log.d(TAG, "Attempting to commit crime: $type")
        viewModelScope.launch {
            try {
                ensureCharacterExists()

                val baseCrime = createCrime(type)
                Log.d(TAG, "Created base crime: ${baseCrime.name}")

                // Generate unique ID for the crime
                val crimeId = UUID.randomUUID().toString()

                // Calculate outcome (success or failure)
                val isSuccess = Random.nextDouble() <= baseCrime.baseSuccessChance

                // Calculate chance of getting caught (based on risk tier)
                val isCaught = Random.nextDouble() < when(baseCrime.riskTier) {
                    RiskTier.LOW_RISK -> 0.3
                    RiskTier.MEDIUM_RISK -> 0.5
                    RiskTier.HIGH_RISK -> 0.7
                    RiskTier.EXTREME_RISK -> 0.9
                }

                Log.d(TAG, "Crime success: $isSuccess, caught: $isCaught")

                // Calculate money outcomes
                var moneyGained = 0
                var actualJailTime = 0
                var notorietyChange = 0

                if (isSuccess) {
                    // Successful crime - gain money
                    moneyGained = Random.nextInt(baseCrime.payoutMin, baseCrime.payoutMax + 1)
                    notorietyChange = baseCrime.notorietyGain
                    Log.d(TAG, "Crime successful, gained money: $moneyGained")
                } else {
                    notorietyChange = baseCrime.notorietyLoss
                    Log.d(TAG, "Crime failed, lost notoriety: ${baseCrime.notorietyLoss}")
                }

                // Handle consequences if caught
                if (isCaught) {
                    // Apply jail time based on risk tier
                    actualJailTime = Random.nextInt(baseCrime.jailMin, baseCrime.jailMax + 1)
                    Log.d(TAG, "Player caught, jail time: $actualJailTime days")
                }

                // Create the actual crime record with outcome
                val actualCrime = baseCrime.copy(
                    id = crimeId,
                    success = isSuccess,
                    caught = isCaught,
                    moneyGained = moneyGained,
                    actualJailTime = actualJailTime
                )

                Log.d(TAG, "Recording crime: ${actualCrime.name}")

                // Record the crime
                crimeRepository.recordCrime(CHARACTER_ID, actualCrime)

                // Apply money change to player if successful
                if (moneyGained > 0) {
                    playerRepository.updateMoney(CHARACTER_ID, moneyGained)
                    Log.d(TAG, "Updated player money by: $moneyGained")
                }

                // Apply notoriety change
                if (notorietyChange != 0) {
                    playerRepository.updateNotoriety(CHARACTER_ID, notorietyChange)
                    Log.d(TAG, "Updated player notoriety by: $notorietyChange")
                    _playerNotoriety.value += notorietyChange
                }

                // Apply jail time if applicable
                if (actualJailTime > 0) {
                    playerRepository.updateJailTime(CHARACTER_ID, actualJailTime)
                    Log.d(TAG, "Player jailed for: $actualJailTime days")
                }

                Log.d(TAG, "Crime committed successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Error committing crime", e)
            }
        }
    }

    private fun createCrime(type: CrimeType): Crime {
        return when (type) {
            // LOW RISK CRIMES
            CrimeType.PICKPOCKETING -> {
                val scenarios = listOf(
                    "You spot a distracted shopper fumbling for their phone.",
                    "A tourist stops to take a photo, bag open on their shoulder.",
                    "A gambler counts his winnings openly in the street."
                )
                Crime(
                    id = "",
                    name = "Pickpocketing",
                    description = "Stealing wallets and valuables from unsuspecting victims",
                    riskTier = RiskTier.LOW_RISK,
                    notorietyRequired = 0,
                    baseSuccessChance = 0.7,
                    payoutMin = 20,
                    payoutMax = 200,
                    jailMin = 1,
                    jailMax = 3,
                    notorietyGain = 2,
                    notorietyLoss = -3,
                    iconDescription = "Gloved hand pulling a wallet from a back pocket",
                    scenario = scenarios.random()
                )
            }
            CrimeType.SHOPLIFTING -> {
                val scenarios = listOf(
                    "You notice a blind spot in the store's camera coverage.",
                    "The fitting rooms are unattended.",
                    "Someone else triggers a loud commotion."
                )
                Crime(
                    id = "",
                    name = "Shoplifting",
                    description = "Stealing goods from retail stores",
                    riskTier = RiskTier.LOW_RISK,
                    notorietyRequired = 0,
                    baseSuccessChance = 0.65,
                    payoutMin = 50,
                    payoutMax = 300,
                    jailMin = 2,
                    jailMax = 5,
                    notorietyGain = 2,
                    notorietyLoss = -3,
                    iconDescription = "Shopping bag silhouette with hand pulling it away",
                    scenario = scenarios.random()
                )
            }
            CrimeType.VANDALISM -> {
                val scenarios = listOf(
                    "A rival gang's mural taunts your crew.",
                    "A politician's poster becomes your canvas.",
                    "You tag over a rival's graffiti."
                )
                Crime(
                    id = "",
                    name = "Vandalism",
                    description = "Damaging property for fun or to send a message",
                    riskTier = RiskTier.LOW_RISK,
                    notorietyRequired = 0,
                    baseSuccessChance = 0.8,
                    payoutMin = 10,
                    payoutMax = 150,
                    jailMin = 1,
                    jailMax = 2,
                    notorietyGain = 2,
                    notorietyLoss = -3,
                    iconDescription = "Tilted spray paint can with mist cloud",
                    scenario = scenarios.random()
                )
            }
            CrimeType.PETTY_SCAM -> {
                val scenarios = listOf(
                    "You \"find\" a gold ring and offer to sell it cheap.",
                    "You sell fake raffle tickets at a busy market.",
                    "You pose as a charity collector."
                )
                Crime(
                    id = "",
                    name = "Petty Scam",
                    description = "Small-scale fraud to earn quick cash",
                    riskTier = RiskTier.LOW_RISK,
                    notorietyRequired = 0,
                    baseSuccessChance = 0.6,
                    payoutMin = 30,
                    payoutMax = 250,
                    jailMin = 2,
                    jailMax = 4,
                    notorietyGain = 2,
                    notorietyLoss = -3,
                    iconDescription = "Dollar bill with cartoon mask overlay",
                    scenario = scenarios.random()
                )
            }

            // MEDIUM RISK CRIMES
            CrimeType.MUGGING -> {
                val scenarios = listOf(
                    "You corner a lone businessman in a dark alley.",
                    "A jogger stops to catch their breath, headphones in.",
                    "A tourist wanders into the wrong neighborhood."
                )
                Crime(
                    id = "",
                    name = "Mugging",
                    description = "Robbery involving direct confrontation with victims",
                    riskTier = RiskTier.MEDIUM_RISK,
                    notorietyRequired = 20,
                    baseSuccessChance = 0.55,
                    payoutMin = 100,
                    payoutMax = 800,
                    jailMin = 5,
                    jailMax = 14,
                    notorietyGain = 4,
                    notorietyLoss = -6,
                    iconDescription = "Fist silhouette with wallet overlay",
                    scenario = scenarios.random()
                )
            }
            CrimeType.BREAKING_AND_ENTERING -> {
                val scenarios = listOf(
                    "You spot a home with lights off and mail piling up.",
                    "A back window is left unlocked.",
                    "A shopkeeper leaves the rear door ajar."
                )
                Crime(
                    id = "",
                    name = "Breaking and Entering",
                    description = "Unlawful entry into buildings or homes",
                    riskTier = RiskTier.MEDIUM_RISK,
                    notorietyRequired = 20,
                    baseSuccessChance = 0.5,
                    payoutMin = 500,
                    payoutMax = 2000,
                    jailMin = 10,
                    jailMax = 20,
                    notorietyGain = 4,
                    notorietyLoss = -6,
                    iconDescription = "House silhouette with broken door outline",
                    scenario = scenarios.random()
                )
            }
            CrimeType.DRUG_DEALING -> {
                val scenarios = listOf(
                    "A regular customer asks for a bigger order than usual.",
                    "You meet a new buyer at a busy park.",
                    "A bar patron discreetly approaches you."
                )
                Crime(
                    id = "",
                    name = "Drug Dealing",
                    description = "Selling illegal substances for profit",
                    riskTier = RiskTier.MEDIUM_RISK,
                    notorietyRequired = 20,
                    baseSuccessChance = 0.6,
                    payoutMin = 200,
                    payoutMax = 1800,
                    jailMin = 7,
                    jailMax = 15,
                    notorietyGain = 4,
                    notorietyLoss = -6,
                    iconDescription = "Pill bottle with dollar sign overlay",
                    scenario = scenarios.random()
                )
            }
            CrimeType.COUNTERFEIT_GOODS -> {
                val scenarios = listOf(
                    "A flea market vendor agrees to move your goods.",
                    "Tourists crowd around your street stall.",
                    "A nightclub promoter buys bulk for giveaways."
                )
                Crime(
                    id = "",
                    name = "Counterfeit Goods",
                    description = "Selling fake or reproduction items illegally",
                    riskTier = RiskTier.MEDIUM_RISK,
                    notorietyRequired = 20,
                    baseSuccessChance = 0.65,
                    payoutMin = 300,
                    payoutMax = 1500,
                    jailMin = 8,
                    jailMax = 18,
                    notorietyGain = 4,
                    notorietyLoss = -6,
                    iconDescription = "Designer handbag with \"fake\" stamp icon",
                    scenario = scenarios.random()
                )
            }

            // HIGH RISK CRIMES
            CrimeType.BURGLARY -> {
                val scenarios = listOf(
                    "You disable a small shop's alarm system.",
                    "A mansion is left unattended for the weekend.",
                    "You find a warehouse with lax security."
                )
                Crime(
                    id = "",
                    name = "Burglary",
                    description = "Breaking into homes or businesses to steal valuable items",
                    riskTier = RiskTier.HIGH_RISK,
                    notorietyRequired = 50,
                    baseSuccessChance = 0.45,
                    payoutMin = 1000,
                    payoutMax = 8000,
                    jailMin = 60,  // 2 months
                    jailMax = 150, // 5 months
                    notorietyGain = 7,
                    notorietyLoss = -10,
                    iconDescription = "Crowbar over house silhouette",
                    scenario = scenarios.random()
                )
            }
            CrimeType.FRAUD -> {
                val scenarios = listOf(
                    "You set up a fake charity donation site.",
                    "You skim credit cards at a gas station.",
                    "You forge a cashier's check."
                )
                Crime(
                    id = "",
                    name = "Fraud",
                    description = "Deceiving people or institutions for financial gain",
                    riskTier = RiskTier.HIGH_RISK,
                    notorietyRequired = 50,
                    baseSuccessChance = 0.5,
                    payoutMin = 2000,
                    payoutMax = 12000,
                    jailMin = 90,   // 3 months
                    jailMax = 210,  // 7 months
                    notorietyGain = 7,
                    notorietyLoss = -10,
                    iconDescription = "Credit card with warning symbol",
                    scenario = scenarios.random()
                )
            }
            CrimeType.ARMS_SMUGGLING -> {
                val scenarios = listOf(
                    "You move a shipment through a border checkpoint.",
                    "You sell to a biker gang out of state.",
                    "You load crates into a cargo van at night."
                )
                Crime(
                    id = "",
                    name = "Arms Smuggling",
                    description = "Illegally transporting weapons across borders",
                    riskTier = RiskTier.HIGH_RISK,
                    notorietyRequired = 50,
                    baseSuccessChance = 0.4,
                    payoutMin = 5000,
                    payoutMax = 22000,
                    jailMin = 150,  // 5 months
                    jailMax = 270,  // 9 months
                    notorietyGain = 7,
                    notorietyLoss = -10,
                    iconDescription = "Rifle silhouette in crate",
                    scenario = scenarios.random()
                )
            }
            CrimeType.DRUG_TRAFFICKING -> {
                val scenarios = listOf(
                    "You drive a van across the state line.",
                    "A shipment arrives hidden in produce crates.",
                    "You use a fishing boat to transport packages."
                )
                Crime(
                    id = "",
                    name = "Drug Trafficking",
                    description = "Large-scale distribution of illegal substances",
                    riskTier = RiskTier.HIGH_RISK,
                    notorietyRequired = 50,
                    baseSuccessChance = 0.35,
                    payoutMin = 10000,
                    payoutMax = 45000,
                    jailMin = 180,  // 6 months
                    jailMax = 365,  // 12 months
                    notorietyGain = 7,
                    notorietyLoss = -10,
                    iconDescription = "Truck icon with pill/bag symbol",
                    scenario = scenarios.random()
                )
            }

            // EXTREME RISK CRIMES
            CrimeType.ARMED_ROBBERY -> {
                val scenarios = listOf(
                    "You storm a jewelry store during peak hours.",
                    "You hit an armored truck in transit.",
                    "You rob a high-stakes poker game."
                )
                Crime(
                    id = "",
                    name = "Armed Robbery",
                    description = "Robbery involving weapons and significant violence",
                    riskTier = RiskTier.EXTREME_RISK,
                    notorietyRequired = 80,
                    baseSuccessChance = 0.3,
                    payoutMin = 20000,
                    payoutMax = 120000,
                    jailMin = 1460,  // 4 years
                    jailMax = 4380,  // 12 years
                    notorietyGain = 10,
                    notorietyLoss = -15,
                    iconDescription = "Masked head with pistol silhouette",
                    scenario = scenarios.random()
                )
            }
            CrimeType.EXTORTION -> {
                val scenarios = listOf(
                    "You threaten to leak sensitive photos.",
                    "You demand \"protection\" money from a nightclub.",
                    "You blackmail a corporate executive."
                )
                Crime(
                    id = "",
                    name = "Extortion",
                    description = "Forcing victims to provide money through threats",
                    riskTier = RiskTier.EXTREME_RISK,
                    notorietyRequired = 80,
                    baseSuccessChance = 0.4,
                    payoutMin = 5000,
                    payoutMax = 40000,
                    jailMin = 730,   // 2 years
                    jailMax = 2190,  // 6 years
                    notorietyGain = 10,
                    notorietyLoss = -15,
                    iconDescription = "Envelope with dollar sign and warning triangle",
                    scenario = scenarios.random()
                )
            }
            CrimeType.KIDNAPPING_FOR_RANSOM -> {
                val scenarios = listOf(
                    "You grab a wealthy child outside a school.",
                    "You abduct a celebrity's assistant.",
                    "You take a local politician's spouse."
                )
                Crime(
                    id = "",
                    name = "Kidnapping for Ransom",
                    description = "Abducting people for financial gain",
                    riskTier = RiskTier.EXTREME_RISK,
                    notorietyRequired = 80,
                    baseSuccessChance = 0.25,
                    payoutMin = 50000,
                    payoutMax = 400000,
                    jailMin = 2190,  // 6 years
                    jailMax = 5475,  // 15 years
                    notorietyGain = 10,
                    notorietyLoss = -15,
                    iconDescription = "Bound person silhouette",
                    scenario = scenarios.random()
                )
            }
            CrimeType.PONZI_SCHEME -> {
                val scenarios = listOf(
                    "You launch a fake investment firm.",
                    "You promise impossible returns to investors.",
                    "You use new deposits to pay earlier victims."
                )
                Crime(
                    id = "",
                    name = "Ponzi Scheme",
                    description = "Complex fraud involving false investment returns",
                    riskTier = RiskTier.EXTREME_RISK,
                    notorietyRequired = 80,
                    baseSuccessChance = 0.35,
                    payoutMin = 90000,
                    payoutMax = 900000,
                    jailMin = 1095,  // 3 years
                    jailMax = 3650,  // 10 years
                    notorietyGain = 10,
                    notorietyLoss = -15,
                    iconDescription = "Stacked coins forming a pyramid",
                    scenario = scenarios.random()
                )
            }
            CrimeType.CONTRACT_KILLING -> {
                val scenarios = listOf(
                    "You accept a hit on a rival gang leader.",
                    "You take out a cheating spouse's lover.",
                    "You ambush a target in a parking garage."
                )
                Crime(
                    id = "",
                    name = "Contract Killing",
                    description = "Professional assassination for hire",
                    riskTier = RiskTier.EXTREME_RISK,
                    notorietyRequired = 80,
                    baseSuccessChance = 0.3,
                    payoutMin = 25000,
                    payoutMax = 450000,
                    jailMin = 3650,  // 10 years
                    jailMax = 7300,  // 20 years
                    notorietyGain = 10,
                    notorietyLoss = -15,
                    iconDescription = "Silenced pistol silhouette",
                    scenario = scenarios.random()
                )
            }
            CrimeType.DARK_WEB_SALES -> {
                val scenarios = listOf(
                    "You sell stolen bank credentials.",
                    "You auction off hacking tools.",
                    "You ship counterfeit passports overseas."
                )
                Crime(
                    id = "",
                    name = "Dark Web Sales",
                    description = "Selling illegal goods and services online",
                    riskTier = RiskTier.EXTREME_RISK,
                    notorietyRequired = 80,
                    baseSuccessChance = 0.4,
                    payoutMin = 12000,
                    payoutMax = 220000,
                    jailMin = 730,   // 2 years
                    jailMax = 2920,  // 8 years
                    notorietyGain = 10,
                    notorietyLoss = -15,
                    iconDescription = "Laptop with skull icon",
                    scenario = scenarios.random()
                )
            }
            CrimeType.ART_THEFT -> {
                val scenarios = listOf(
                    "You steal a masterpiece during an exhibition.",
                    "You swap a gallery piece for a replica.",
                    "You break into a private collection."
                )
                Crime(
                    id = "",
                    name = "Art Theft",
                    description = "Stealing high-value artwork",
                    riskTier = RiskTier.EXTREME_RISK,
                    notorietyRequired = 80,
                    baseSuccessChance = 0.2,
                    payoutMin = 110000,
                    payoutMax = 4800000,
                    jailMin = 1825,  // 5 years
                    jailMax = 5475,  // 15 years
                    notorietyGain = 10,
                    notorietyLoss = -15,
                    iconDescription = "Painting frame with cut-out center",
                    scenario = scenarios.random()
                )
            }
            CrimeType.DIAMOND_HEIST -> {
                val scenarios = listOf(
                    "You rob a diamond exchange vault.",
                    "You hit a guarded transport truck.",
                    "You infiltrate a mining company's storage."
                )
                Crime(
                    id = "",
                    name = "Diamond Heist",
                    description = "Major theft involving high-value gems",
                    riskTier = RiskTier.EXTREME_RISK,
                    notorietyRequired = 80,
                    baseSuccessChance = 0.15,
                    payoutMin = 500000,
                    payoutMax = 9500000,
                    jailMin = 2920,  // 8 years
                    jailMax = 7300,  // 20 years
                    notorietyGain = 10,
                    notorietyLoss = -15,
                    iconDescription = "Diamond silhouette in spotlight",
                    scenario = scenarios.random()
                )
            }
        }
    }

    private suspend fun ensureCharacterExists() {
        try {
            Log.d(TAG, "Ensuring character exists...")
            // Check if character exists
            val character = playerRepository.getCharacter(CHARACTER_ID).first()
            if (character == null) {
                Log.d(TAG, "Creating default character...")
                // Create default character with money and notoriety
                val defaultCharacter = Character(
                    id = CHARACTER_ID,
                    name = "Default Character",
                    age = 18,
                    health = 100,
                    happiness = 50,
                    money = 1000,
                    intelligence = 10,
                    fitness = 10,
                    social = 10,
                    education = 0,
                    career = null,
                    relationships = emptyList(),
                    achievements = emptyList(),
                    events = emptyList(),
                    jailTime = 0,
                    notoriety = 0
                )
                playerRepository.createCharacter(CHARACTER_ID, defaultCharacter)
                Log.d(TAG, "Default character created successfully")
            } else {
                Log.d(TAG, "Character already exists: ${character.id}")
                _playerNotoriety.value = character.notoriety
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error ensuring character exists", e)
        }
    }

    fun clearRecord() {
        Log.d(TAG, "Clearing criminal record...")
        viewModelScope.launch {
            try {
                crimeRepository.clearCriminalRecord(CHARACTER_ID)
                // Update UI to reflect cleared record
                _crimes.value = emptyList()
                Log.d(TAG, "Criminal record cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing criminal record", e)
            }
        }
    }

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