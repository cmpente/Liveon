package com.liveongames.liveon.character

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class CreateStep { IDENTITY, BACKGROUND, TRAITS, APPEARANCE, STATS, REVIEW }

data class UiLists(
    val backgrounds: List<BackgroundDef> = emptyList(),
    val traitsPack: TraitPack = TraitPack(TraitLimits(), emptyList()),
    val countries: List<CountryDef> = emptyList(),
    val appearance: AppearancePresets = AppearancePresets(Palettes(emptyList()), emptyList(), emptyList(), emptyList()),
    val namesUnisex: List<String> = emptyList(),
    val surnames: List<String> = emptyList()
)

data class IdentitySel(
    val firstName: String = "",
    val lastName: String = "",
    val pronouns: Pronouns = Pronouns.THEY_THEM,
    val birthdayEpochDays: Int = dayEpochNow(),  // ← no java.time
    val birthplace: LocationRef? = null
)

data class Selections(
    val identity: IdentitySel = IdentitySel(),
    val backgroundId: String? = null,
    val traitIds: Set<String> = emptySet(),
    val appearance: Appearance = Appearance(0, "", "Brown", "Brown", "Average"),
    val pointsPool: Int = 10,
    val stats: PlayerStats = PlayerStats()
)

data class NewLifePayload(val profile: PlayerProfile, val stats: PlayerStats)

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
        // Load asset content on a background thread (suspend funcs)
        viewModelScope.launch(Dispatchers.IO) {
            val backgrounds = runCatching { assets.backgrounds() }.getOrElse { emptyList() }
            val traits = runCatching { assets.traits() }.getOrElse { TraitPack(TraitLimits(), emptyList()) }
            val countries = runCatching { assets.locations() }.getOrElse { emptyList() }
            val appearance = runCatching { assets.appearance() }
                .getOrElse { AppearancePresets(Palettes(emptyList()), emptyList(), emptyList(), emptyList()) }
            val unisex = runCatching { assets.names(Gender.UNISEX) }.getOrElse { listOf("Alex","Taylor","Jordan") }
            val surnames = runCatching { assets.surnames() }.getOrElse { listOf("Smith","Johnson","Brown") }

            _lists.value = UiLists(
                backgrounds = backgrounds,
                traitsPack = traits,
                countries = countries,
                appearance = appearance,
                namesUnisex = unisex,
                surnames = surnames
            )

            // Defaults
            val defaultLoc = countries.firstOrNull()?.cities?.firstOrNull()?.let { c ->
                LocationRef(countries.first().code, c.id)
            }
            _sel.value = _sel.value.copy(
                identity = _sel.value.identity.copy(birthplace = defaultLoc),
                appearance = _sel.value.appearance.copy(
                    skinToneIndex = 0,
                    hairStyleId = appearance.hairStyles.firstOrNull()?.id ?: "",
                    hairColor = appearance.hairColors.firstOrNull() ?: "Brown",
                    eyeColor = appearance.eyeColors.firstOrNull() ?: "Brown",
                    bodyType = appearance.bodyTypes.firstOrNull() ?: "Average"
                ),
                backgroundId = backgrounds.firstOrNull()?.id
            )
        }
    }

    /* ---- navigation ---- */
    fun next() {
        _error.value = null
        _step.value = when (_step.value) {
            CreateStep.IDENTITY -> CreateStep.BACKGROUND
            CreateStep.BACKGROUND -> CreateStep.TRAITS
            CreateStep.TRAITS -> CreateStep.APPEARANCE
            CreateStep.APPEARANCE -> CreateStep.STATS
            CreateStep.STATS -> CreateStep.REVIEW
            CreateStep.REVIEW -> CreateStep.REVIEW
        }
    }
    fun back() {
        _error.value = null
        _step.value = when (_step.value) {
            CreateStep.IDENTITY -> CreateStep.IDENTITY
            CreateStep.BACKGROUND -> CreateStep.IDENTITY
            CreateStep.TRAITS -> CreateStep.BACKGROUND
            CreateStep.APPEARANCE -> CreateStep.TRAITS
            CreateStep.STATS -> CreateStep.APPEARANCE
            CreateStep.REVIEW -> CreateStep.STATS
        }
    }

    /* ---- identity ---- */
    fun randomizeAll() {
        val l = _lists.value
        val first = l.namesUnisex.randomOrNull() ?: "Alex"
        val last = l.surnames.randomOrNull() ?: "Smith"
        val ctry = l.countries.randomOrNull()
        val city = ctry?.cities?.randomOrNull()

        _sel.value = _sel.value.copy(
            identity = _sel.value.identity.copy(
                firstName = first,
                lastName = last,
                pronouns = Pronouns.THEY_THEM,
                birthdayEpochDays = dayEpochNow(),
                birthplace = if (ctry != null && city != null) LocationRef(ctry.code, city.id) else _sel.value.identity.birthplace
            ),
            backgroundId = l.backgrounds.randomOrNull()?.id ?: _sel.value.backgroundId,
            traitIds = emptySet(),
            appearance = _sel.value.appearance.copy(
                skinToneIndex = l.appearance.palettes.skinTones.indices.randomOrNull() ?: 0,
                hairStyleId = l.appearance.hairStyles.randomOrNull()?.id ?: "",
                hairColor = l.appearance.hairColors.randomOrNull() ?: "Brown",
                eyeColor = l.appearance.eyeColors.randomOrNull() ?: "Brown",
                bodyType = l.appearance.bodyTypes.randomOrNull() ?: "Average"
            ),
            pointsPool = 10,
            stats = PlayerStats()
        )
    }
    fun setName(first: String, last: String) {
        _sel.value = _sel.value.copy(identity = _sel.value.identity.copy(firstName = first, lastName = last))
    }
    fun setPronouns(p: Pronouns) {
        _sel.value = _sel.value.copy(identity = _sel.value.identity.copy(pronouns = p))
    }
    fun setBirthplace(loc: LocationRef) {
        _sel.value = _sel.value.copy(identity = _sel.value.identity.copy(birthplace = loc))
    }

    /* ---- background/traits/appearance ---- */
    fun selectBackground(id: String) { _sel.value = _sel.value.copy(backgroundId = id) }
    fun toggleTrait(id: String) {
        val pack = _lists.value.traitsPack
        val cur = _sel.value.traitIds.toMutableSet()
        if (id in cur) cur.remove(id) else {
            if (cur.size >= pack.limits.maxSelected) return
            val t = pack.traits.firstOrNull { it.id == id } ?: return
            if (pack.traits.any { it.id in cur && t.excludes.contains(it.id) }) return
            cur.add(id)
        }
        _sel.value = _sel.value.copy(traitIds = cur)
    }
    fun setAppearance(a: Appearance) { _sel.value = _sel.value.copy(appearance = a) }

    /* ---- stat pool ---- */
    fun setStat(key: StatKey, value: Int) {
        val s = _sel.value
        val current = s.stats
        val clamped = value.coerceIn(0, 100)
        val delta = when (key) {
            StatKey.HEALTH -> clamped - current.health
            StatKey.HAPPINESS -> clamped - current.happiness
            StatKey.INTELLIGENCE -> clamped - current.intelligence
            StatKey.SOCIAL -> clamped - current.social
        }
        val newPool = s.pointsPool - delta
        if (newPool < 0) return
        _sel.value = s.copy(
            pointsPool = newPool,
            stats = when (key) {
                StatKey.HEALTH -> current.copy(health = clamped)
                StatKey.HAPPINESS -> current.copy(happiness = clamped)
                StatKey.INTELLIGENCE -> current.copy(intelligence = clamped)
                StatKey.SOCIAL -> current.copy(social = clamped)
            }
        )
    }

    /* ---- finalize ---- */
    fun finalizeCreate(): NewLifePayload? {
        val l = _lists.value
        val s = _sel.value

        if (s.identity.firstName.isBlank() || s.identity.lastName.isBlank()) {
            _error.value = "Please enter a name."
            return null
        }
        if (s.identity.birthplace == null) {
            _error.value = "Please choose a birthplace."
            return null
        }
        val bg = l.backgrounds.firstOrNull { it.id == s.backgroundId } ?: run {
            _error.value = "Please select a background."
            return null
        }
        if (s.pointsPool < 0) {
            _error.value = "Allocate points within the available pool."
            return null
        }

        // derive starting stats
        var st = s.stats.copy(money = s.stats.money + bg.statMods.money)
        st = st.copy(
            health = (st.health + bg.statMods.health).coerceIn(0, 100),
            happiness = (st.happiness + bg.statMods.happiness).coerceIn(0, 100),
            intelligence = (st.intelligence + bg.statMods.intelligence).coerceIn(0, 100),
            social = (st.social + bg.statMods.social).coerceIn(0, 100),
            age = bg.startAge
        )
        // trait boosts
        val selectedTraits = l.traitsPack.traits.filter { it.id in s.traitIds }
        selectedTraits.forEach { t ->
            t.effects.filter { it.type == "stat_boost" }.forEach { e ->
                val amt = e.amount ?: 0
                st = when (e.key.lowercase()) {
                    "health" -> st.copy(health = (st.health + amt).coerceIn(0, 100))
                    "happiness" -> st.copy(happiness = (st.happiness + amt).coerceIn(0, 100))
                    "intelligence" -> st.copy(intelligence = (st.intelligence + amt).coerceIn(0, 100))
                    "social" -> st.copy(social = (st.social + amt).coerceIn(0, 100))
                    "money" -> st.copy(money = st.money + amt)
                    else -> st
                }
            }
        }

        val profile = PlayerProfile(
            id = UUID.randomUUID().toString(),
            firstName = s.identity.firstName.trim(),
            lastName = s.identity.lastName.trim(),
            pronouns = s.identity.pronouns,
            birthdayEpochDays = s.identity.birthdayEpochDays,
            birthplace = s.identity.birthplace!!,
            backgroundId = bg.id,
            traitIds = s.traitIds.toList(),
            appearance = s.appearance
        )
        return NewLifePayload(profile, st)
    }
}
private fun dayEpochNow(): Int = (System.currentTimeMillis() / 86_400_000L).toInt()