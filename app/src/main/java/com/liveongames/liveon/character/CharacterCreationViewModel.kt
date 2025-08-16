@file:Suppress("MemberVisibilityCanBePrivate")

package com.liveongames.liveon.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt
import kotlin.random.Random

/* ------------ Steps & lightweight UI state ------------- */

enum class CreateStep { IDENTITY, APPEARANCE, NATIONALITY, TRAITS, STATS, REVIEW }

data class IdentitySel(
    val firstName: String = "",
    val lastName: String = "",
    val gender: Gender = Gender.MALE,
    val pronouns: Pronouns = Pronouns.HE_HIM
)

data class GenStats(
    val intelligence: Int = 50,
    val creativity: Int = 50,
    val charisma: Int = 50,
    val athleticism: Int = 50,
    val health: Int = 50,
    val luck: Int = 50,
    val sociability: Int = 50,
    val discipline: Int = 50
)

data class Selections(
    val identity: IdentitySel = IdentitySel(),
    val appearance: Appearance = Appearance(
        skinToneIndex = 0,
        hairStyleId = "",
        hairColor = "Brown",
        eyeColor = "Brown",
        bodyType = "Average"
    ),
    val heightCm: Int = 170,
    val notableFeatures: Set<String> = emptySet(),
    val countryCode: String = "",
    val cityId: String = "",
    val ethnicityId: String = "",          // UI-only, not persisted in PlayerProfile
    val traitIds: Set<String> = emptySet(),
    val stats: GenStats = GenStats(),
    val pointsPool: Int = 40
)

/** What the screen returns to GameViewModel (kept compatible). */
data class NewLifePayload(
    val profile: PlayerProfile,
    val stats: PlayerStats
)

/** Lists shown in UI (mix of your models + small UI-only list). */
data class UiLists(
    val appearance: AppearancePresets = AppearancePresets(
        palettes = Palettes(listOf("#FFD1B5", "#F2B495", "#E39A76", "#C47B55", "#A3613C", "#7C472A", "#5C341E")),
        hairStyles = listOf(HairStyleDef("short", "Short")),
        eyeColors = listOf("Brown"),
        hairColors = listOf("Brown"),
        bodyTypes = listOf("Average", "Athletic", "Slim", "Stocky")
    ),
    val countries: List<CountryDef> = listOf(
        CountryDef("US", "United States", listOf(CityDef("nyc", "New York")))
    ),
    val traitsPack: TraitPack = TraitPack(TraitLimits(2, 0), traits = emptyList()),
    val ethnicities: List<Ethnicity> = listOf(
        Ethnicity("afr", "African"),
        Ethnicity("eas", "East Asian"),
        Ethnicity("sas", "South Asian"),
        Ethnicity("mea", "Middle Eastern"),
        Ethnicity("eur", "European"),
        Ethnicity("lat", "Latino / Hispanic"),
        Ethnicity("ind", "Indigenous"),
        Ethnicity("mix", "Mixed")
    )
)
data class Ethnicity(val id: String, val name: String)

@HiltViewModel
class CharacterCreationViewModel @Inject constructor(
    private val assets: CharacterAssetLoader
) : ViewModel() {

    private val _step = MutableStateFlow(CreateStep.IDENTITY)
    val step: StateFlow<CreateStep> = _step.asStateFlow()

    private val _lists = MutableStateFlow(UiLists())
    val lists: StateFlow<UiLists> = _lists.asStateFlow()

    private val _sel = MutableStateFlow(Selections())
    val sel: StateFlow<Selections> = _sel.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Load JSON content (names/traits/appearance/locations) on IO thread.
        viewModelScope.launch(Dispatchers.IO) {
            val presets = assets.appearance()
            val countries = assets.locations()
            val traits = assets.traits()

            // Push lists
            _lists.value = _lists.value.copy(
                appearance = presets,
                countries = countries,
                traitsPack = traits
            )

            // Establish sensible defaults once lists are loaded
            val defCountry = countries.firstOrNull()
            val defCity = defCountry?.cities?.firstOrNull()
            val defHair = presets.hairStyles.firstOrNull()

            _sel.value = _sel.value.copy(
                countryCode = defCountry?.code ?: _sel.value.countryCode,
                cityId = defCity?.id ?: _sel.value.cityId,
                appearance = _sel.value.appearance.copy(
                    hairStyleId = defHair?.id ?: _sel.value.appearance.hairStyleId,
                    hairColor = presets.hairColors.firstOrNull() ?: _sel.value.appearance.hairColor,
                    eyeColor = presets.eyeColors.firstOrNull() ?: _sel.value.appearance.eyeColor,
                    bodyType = presets.bodyTypes.firstOrNull() ?: _sel.value.appearance.bodyType
                )
            )
        }
    }

    /* --------- Navigation --------- */
    fun next() {
        _error.value = null
        _step.value = when (_step.value) {
            CreateStep.IDENTITY -> CreateStep.APPEARANCE
            CreateStep.APPEARANCE -> CreateStep.NATIONALITY
            CreateStep.NATIONALITY -> CreateStep.TRAITS
            CreateStep.TRAITS -> CreateStep.STATS
            CreateStep.STATS -> CreateStep.REVIEW
            CreateStep.REVIEW -> CreateStep.REVIEW
        }
    }
    fun back() {
        _error.value = null
        _step.value = when (_step.value) {
            CreateStep.IDENTITY -> CreateStep.IDENTITY
            CreateStep.APPEARANCE -> CreateStep.IDENTITY
            CreateStep.NATIONALITY -> CreateStep.APPEARANCE
            CreateStep.TRAITS -> CreateStep.NATIONALITY
            CreateStep.STATS -> CreateStep.TRAITS
            CreateStep.REVIEW -> CreateStep.STATS
        }
    }

    /* --------- Identity --------- */
    fun setName(first: String, last: String) {
        _sel.value = _sel.value.copy(identity = _sel.value.identity.copy(firstName = first, lastName = last))
    }
    fun setGender(g: Gender) {
        val newPronouns = when (g) {
            Gender.MALE -> Pronouns.HE_HIM
            Gender.FEMALE -> Pronouns.SHE_HER
            Gender.UNISEX -> Pronouns.THEY_THEM
        }
        _sel.value = _sel.value.copy(identity = _sel.value.identity.copy(gender = g, pronouns = newPronouns))
    }
    fun setPronouns(p: Pronouns) {
        _sel.value = _sel.value.copy(identity = _sel.value.identity.copy(pronouns = p))
    }
    fun randomizeIdentity() {
        val firsts = listOf("Alex","Jamal","Ava","Maya","Diego","Noah","Liam","Layla","Ibrahim","Priya")
        val lasts = listOf("Singh","Johnson","Garcia","Kim","Martinez","Khan","Ali","Lee","Brown","Smith")
        val g = Gender.values().random()
        _sel.value = _sel.value.copy(
            identity = _sel.value.identity.copy(
                firstName = firsts.random(),
                lastName = lasts.random(),
                gender = g,
                pronouns = when (g) {
                    Gender.MALE -> Pronouns.HE_HIM
                    Gender.FEMALE -> Pronouns.SHE_HER
                    Gender.UNISEX -> Pronouns.THEY_THEM
                }
            )
        )
    }

    /* --------- Appearance --------- */
    fun setAppearance(a: Appearance) { _sel.value = _sel.value.copy(appearance = a) }
    fun setHeight(cm: Int) { _sel.value = _sel.value.copy(heightCm = cm.coerceIn(120, 210)) }
    fun toggleFeature(tag: String) {
        val s = _sel.value
        _sel.value = s.copy(
            notableFeatures = if (tag in s.notableFeatures) s.notableFeatures - tag else s.notableFeatures + tag
        )
    }

    /* --------- Nationality & Ethnicity --------- */
    fun setCountry(code: String) {
        val c = _lists.value.countries.firstOrNull { it.code == code } ?: return
        val city = c.cities.firstOrNull() ?: return
        _sel.value = _sel.value.copy(countryCode = c.code, cityId = city.id)
    }
    fun setCity(cityId: String) { _sel.value = _sel.value.copy(cityId = cityId) }
    fun setEthnicity(id: String) { _sel.value = _sel.value.copy(ethnicityId = id) }

    /* --------- Traits --------- */
    fun toggleTrait(id: String) {
        val pack = _lists.value.traitsPack
        val current = _sel.value.traitIds
        val next = if (id in current) current - id else (current + id).take(pack.limits.maxSelected).toSet()
        _sel.value = _sel.value.copy(traitIds = next)
    }

    /* --------- Stats (Point-buy) --------- */
    fun setStat(key: String, value: Int) {
        val v = value.coerceIn(0, 100)
        val s = _sel.value.stats
        val poolBefore = _sel.value.pointsPool
        val currentValue = when (key) {
            "intelligence" -> s.intelligence
            "creativity" -> s.creativity
            "charisma" -> s.charisma
            "athleticism" -> s.athleticism
            "health" -> s.health
            "luck" -> s.luck
            "sociability" -> s.sociability
            "discipline" -> s.discipline
            else -> return
        }
        val delta = v - currentValue
        val newPool = (poolBefore - delta).coerceAtLeast(0)

        val appliedValue = if (poolBefore - delta < 0) currentValue + poolBefore else v
        val newStats = when (key) {
            "intelligence" -> s.copy(intelligence = appliedValue)
            "creativity" -> s.copy(creativity = appliedValue)
            "charisma" -> s.copy(charisma = appliedValue)
            "athleticism" -> s.copy(athleticism = appliedValue)
            "health" -> s.copy(health = appliedValue)
            "luck" -> s.copy(luck = appliedValue)
            "sociability" -> s.copy(sociability = appliedValue)
            "discipline" -> s.copy(discipline = appliedValue)
            else -> s
        }
        _sel.value = _sel.value.copy(stats = newStats, pointsPool = newPool)
    }

    /* --------- Quick Start --------- */
    fun quickStart() {
        randomizeIdentity()
        val ap = _lists.value.appearance
        val s = _sel.value
        val country = _lists.value.countries.random()
        val city = country.cities.random()
        val ethnicity = _lists.value.ethnicities.random()
        val randomStats = GenStats(
            intelligence = Random.nextInt(40, 80),
            creativity = Random.nextInt(40, 80),
            charisma = Random.nextInt(40, 80),
            athleticism = Random.nextInt(40, 80),
            health = Random.nextInt(40, 80),
            luck = Random.nextInt(35, 85),
            sociability = Random.nextInt(40, 80),
            discipline = Random.nextInt(40, 80)
        )
        _sel.value = s.copy(
            appearance = s.appearance.copy(
                skinToneIndex = Random.nextInt(ap.palettes.skinTones.size),
                hairStyleId = ap.hairStyles.random().id,
                hairColor = ap.hairColors.random(),
                eyeColor = ap.eyeColors.random(),
                bodyType = ap.bodyTypes.random()
            ),
            heightCm = Random.nextInt(150, 195),
            notableFeatures = setOf("Tattoo").take(Random.nextInt(0, 2)).toSet(),
            countryCode = country.code,
            cityId = city.id,
            ethnicityId = ethnicity.id,
            stats = randomStats,
            pointsPool = 0,
            traitIds = _lists.value.traitsPack.traits.shuffled().take(2).map { it.id }.toSet()
        )
        _step.value = CreateStep.REVIEW
    }

    /* --------- Finalize --------- */
    fun finalizeCreate(): NewLifePayload? {
        val s = _sel.value
        if (s.identity.firstName.isBlank() || s.identity.lastName.isBlank()) {
            _error.value = "Please enter a name."
            return null
        }

        val profile = PlayerProfile(
            id = "", // your GameVM can assign a GUID later
            firstName = s.identity.firstName.trim(),
            lastName = s.identity.lastName.trim(),
            pronouns = s.identity.pronouns,
            birthdayEpochDays = 0, // newborn; avoids java.time on API < 26
            birthplace = LocationRef(countryCode = s.countryCode, cityId = s.cityId),
            backgroundId = "custom_child",
            traitIds = s.traitIds.toList(),
            appearance = s.appearance
        )

        val stats = toPlayerStats(s.stats, s.traitIds, _lists.value.traitsPack)

        return NewLifePayload(profile = profile, stats = stats)
    }

    private fun toPlayerStats(gen: GenStats, traitIds: Set<String>, pack: TraitPack): PlayerStats {
        val intelligence = gen.intelligence
        val social = ((gen.charisma + gen.sociability) / 2.0).roundToInt()
        val health = ((gen.health + gen.athleticism) / 2.0).roundToInt()
        var happiness = ((gen.creativity + gen.luck) / 2.0).roundToInt()

        // Apply trait boosts (modeled as "stat_boost")
        pack.traits.filter { it.id in traitIds }.flatMap { it.effects }.forEach { eff ->
            val amt = eff.amount ?: 0
            when (eff.key) {
                "intelligence" -> { /* intelligence used directly */ }
                "health" -> { /* covered in aggregate */ }
                "charisma", "sociability" -> { /* inside social aggregate */ }
                "creativity", "luck" -> happiness = (happiness + amt).coerceIn(0, 100)
            }
        }

        return PlayerStats(
            age = 0,
            health = health.coerceIn(0, 100),
            happiness = happiness.coerceIn(0, 100),
            intelligence = intelligence.coerceIn(0, 100),
            social = social.coerceIn(0, 100),
            money = 0
        )
    }

    /* --------- Narrative summary --------- */
    fun buildSummary(lists: UiLists, s: Selections): String {
        val country = lists.countries.firstOrNull { it.code == s.countryCode }?.name ?: "an unknown country"
        val city = lists.countries.firstOrNull { it.code == s.countryCode }?.cities?.firstOrNull { it.id == s.cityId }?.name
        val eth = lists.ethnicities.firstOrNull { it.id == s.ethnicityId }?.name
        val genderWord = when (s.identity.gender) {
            Gender.MALE -> "boy"
            Gender.FEMALE -> "girl"
            Gender.UNISEX -> "child"
        }
        val traitNames = lists.traitsPack.traits.filter { it.id in s.traitIds }.map { it.name }
        val statAdjs = buildStatAdjectives(s.stats)

        val birthplace = if (city != null) "$city, $country" else country
        val ethPart = eth?.let { " of $it background" } ?: ""
        val traitsPart = when {
            traitNames.isEmpty() -> ""
            traitNames.size == 1 -> " You carry the trait \"${traitNames[0]}\"."
            else -> " You carry the traits \"${traitNames[0]}\" and \"${traitNames[1]}\"."
        }
        return "You are ${s.identity.firstName} ${s.identity.lastName}, a $genderWord born in $birthplace$ethPart. " +
                "You are ${statAdjs.joinToString(", ")}, and your early life hints at your unique path.$traitsPart"
    }

    private fun buildStatAdjectives(gs: GenStats): List<String> {
        val list = mutableListOf<String>()
        if (gs.intelligence >= 70) list += "bright"
        if (gs.creativity >= 70) list += "imaginative"
        if (gs.charisma >= 70) list += "persuasive"
        if (gs.athleticism >= 70) list += "athletic"
        if (gs.health <= 35) list += "prone to illness"
        if (gs.luck >= 70) list += "lucky"
        if (gs.sociability >= 70) list += "outgoing"
        if (gs.discipline >= 70) list += "disciplined"
        if (list.isEmpty()) list += "curious"
        return list
    }
}