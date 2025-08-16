package com.liveongames.liveon.character

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject
import org.json.JSONArray

interface CharacterAssetLoader {
    suspend fun backgrounds(): List<BackgroundDef>
    suspend fun traits(): TraitPack
    suspend fun names(gender: Gender = Gender.UNISEX): List<String>
    suspend fun surnames(): List<String>
    suspend fun locations(): List<CountryDef>
    suspend fun appearance(): AppearancePresets
}

@Singleton
class CharacterAssetLoaderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CharacterAssetLoader {

    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }

    override suspend fun backgrounds(): List<BackgroundDef> {
        val json = JSONObject(readAsset("character_backgrounds.json"))
        val arr = json.getJSONArray("backgrounds")
        val out = mutableListOf<BackgroundDef>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val mods = o.getJSONObject("statMods")
            val loc = o.getJSONObject("startLocation")
            out += BackgroundDef(
                id = o.getString("id"),
                name = o.getString("name"),
                desc = o.getString("desc"),
                statMods = StatMods(
                    health = mods.optInt("health"),
                    happiness = mods.optInt("happiness"),
                    intelligence = mods.optInt("intelligence"),
                    social = mods.optInt("social"),
                    money = mods.optInt("money")
                ),
                startAge = o.optInt("startAge", 0),
                startLocation = LocationRef(
                    countryCode = loc.getString("countryCode"),
                    cityId = loc.getString("cityId")
                ),
                tags = o.optJSONArray("tags")?.toListString() ?: emptyList()
            )
        }
        return out
    }

    override suspend fun traits(): TraitPack {
        val json = JSONObject(readAsset("character_traits.json"))
        val limits = json.getJSONObject("limits")
        val arr = json.getJSONArray("traits")
        val traits = mutableListOf<TraitDef>()
        for (i in 0 until arr.length()) {
            val t = arr.getJSONObject(i)
            val eff = t.getJSONArray("effects")
            val effects = mutableListOf<TraitEffect>()
            for (j in 0 until eff.length()) {
                val e = eff.getJSONObject(j)
                effects += TraitEffect(
                    type = e.getString("type"),
                    key = e.getString("key"),
                    amount = if (e.has("amount")) e.getInt("amount") else null,
                    mult = if (e.has("mult")) e.getDouble("mult").toFloat() else null
                )
            }
            traits += TraitDef(
                id = t.getString("id"),
                name = t.getString("name"),
                desc = t.getString("desc"),
                effects = effects,
                excludes = t.optJSONArray("excludes")?.toListString() ?: emptyList(),
                tags = t.optJSONArray("tags")?.toListString() ?: emptyList()
            )
        }
        return TraitPack(
            limits = TraitLimits(
                maxSelected = limits.optInt("maxSelected", 3),
                minSelected = limits.optInt("minSelected", 0)
            ),
            traits = traits
        )
    }

    override suspend fun names(gender: Gender): List<String> {
        val file = when (gender) {
            Gender.MALE -> "names_male.json"
            Gender.FEMALE -> "names_female.json"
            Gender.UNISEX -> "names_unisex.json"
        }
        val json = JSONObject(readAsset(file))
        return json.getJSONArray("names").toListString()
    }

    override suspend fun surnames(): List<String> {
        val json = JSONObject(readAsset("surnames.json"))
        return json.getJSONArray("names").toListString()
    }

    override suspend fun locations(): List<CountryDef> {
        val json = JSONObject(readAsset("locations.json"))
        val arr = json.getJSONArray("countries")
        val out = mutableListOf<CountryDef>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val citiesArr = o.getJSONArray("cities")
            val cities = mutableListOf<CityDef>()
            for (j in 0 until citiesArr.length()) {
                val c = citiesArr.getJSONObject(j)
                cities += CityDef(c.getString("id"), c.getString("name"))
            }
            out += CountryDef(
                code = o.getString("code"),
                name = o.getString("name"),
                cities = cities
            )
        }
        return out
    }

    override suspend fun appearance(): AppearancePresets {
        val json = JSONObject(readAsset("appearance_presets.json"))
        val palettes = json.getJSONObject("palettes")
        val skinTones = palettes.getJSONArray("skinTones").toListString()
        val hairStylesArr = json.getJSONArray("hairStyles")
        val hairStyles = mutableListOf<HairStyleDef>()
        for (i in 0 until hairStylesArr.length()) {
            val h = hairStylesArr.getJSONObject(i)
            hairStyles += HairStyleDef(h.getString("id"), h.getString("name"))
        }
        val eyeColors = json.getJSONArray("eyeColors").toListString()
        val hairColors = json.getJSONArray("hairColors").toListString()
        val bodyTypes = json.optJSONArray("bodyTypes")?.toListString()
            ?: listOf("Average", "Athletic", "Slim", "Stocky")
        return AppearancePresets(
            palettes = Palettes(skinTones),
            hairStyles = hairStyles,
            eyeColors = eyeColors,
            hairColors = hairColors,
            bodyTypes = bodyTypes
        )
    }
}

/* ---- JSONArray helper ---- */

private fun JSONArray.toListString(): List<String> {
    val out = mutableListOf<String>()
    for (i in 0 until length()) out += getString(i)
    return out
}
