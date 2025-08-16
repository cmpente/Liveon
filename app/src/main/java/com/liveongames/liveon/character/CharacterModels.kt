package com.liveongames.liveon.character

import androidx.annotation.Keep

/* --------- Core player models --------- */

@Keep
enum class Pronouns { HE_HIM, SHE_HER, THEY_THEM }

@Keep
data class LocationRef(
    val countryCode: String,
    val cityId: String
)

@Keep
data class Appearance(
    val skinToneIndex: Int,
    val hairStyleId: String,
    val hairColor: String,
    val eyeColor: String,
    val bodyType: String
)

@Keep
data class PlayerProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val pronouns: Pronouns,
    val birthdayEpochDays: Int,
    val birthplace: LocationRef,
    val backgroundId: String,
    val traitIds: List<String>,
    val appearance: Appearance
)

@Keep
data class PlayerStats(
    val age: Int = 0,
    val health: Int = 10,
    val happiness: Int = 10,
    val intelligence: Int = 10,
    val social: Int = 10,
    val money: Int = 0
)

/* --------- JSON content (assets) --------- */

@Keep
data class BackgroundDef(
    val id: String,
    val name: String,
    val desc: String,
    val statMods: StatMods,
    val startAge: Int,
    val startLocation: LocationRef,
    val tags: List<String> = emptyList()
)
@Keep
data class StatMods(
    val health: Int = 0,
    val happiness: Int = 0,
    val intelligence: Int = 0,
    val social: Int = 0,
    val money: Int = 0
)

@Keep
data class TraitPack(
    val limits: TraitLimits,
    val traits: List<TraitDef>
)
@Keep
data class TraitLimits(val maxSelected: Int = 3, val minSelected: Int = 0)

@Keep
data class TraitDef(
    val id: String,
    val name: String,
    val desc: String,
    val effects: List<TraitEffect>,
    val excludes: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)
@Keep
data class TraitEffect(
    val type: String,        // "stat_boost" | "system_multiplier"
    val key: String,         // "health", "education_gain", etc.
    val amount: Int? = null, // for stat_boost
    val mult: Float? = null  // for system_multiplier
)

@Keep
data class CountryDef(
    val code: String,
    val name: String,
    val cities: List<CityDef>
)
@Keep
data class CityDef(val id: String, val name: String)

@Keep
data class AppearancePresets(
    val palettes: Palettes,
    val hairStyles: List<HairStyleDef>,
    val eyeColors: List<String>,
    val hairColors: List<String>,
    val bodyTypes: List<String> = listOf("Average", "Athletic", "Slim", "Stocky")
)
@Keep
data class Palettes(val skinTones: List<String>)
@Keep
data class HairStyleDef(val id: String, val name: String)

/* --------- Utilities --------- */

enum class Gender { MALE, FEMALE, UNISEX }
enum class StatKey { HEALTH, HAPPINESS, INTELLIGENCE, SOCIAL }
