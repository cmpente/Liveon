package com.liveongames.liveon.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.liveongames.domain.model.EducationLevel
import com.liveongames.domain.model.GameEvent
import com.liveongames.liveon.model.EducationActionDef
import com.liveongames.liveon.model.EducationCourse
import java.io.InputStreamReader
import com.liveongames.liveon.assets.achievements.AchievementAsset

/**
 * Unified assets loader:
 * - Keeps existing loaders (events, scenarios, achievements).
 * - Adds Education loaders (courses + actions).
 * - Safe: returns empty defaults on failures.
 */
class JsonAssetLoader(
    @PublishedApi internal val context: Context
) {
    @PublishedApi internal val gson: Gson = Gson()

    // ------------------------------------------------------------
    // Generic helpers (safe; return empty defaults on any failure)
    // ------------------------------------------------------------

    /** Reads a raw string from assets. */
    fun readRaw(assetFileName: String): String = try {
        context.assets.open(assetFileName).use { it.bufferedReader().use { br -> br.readText() } }
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }

    /** Parses a JsonElement (object or array) from assets. */
    fun parse(assetFileName: String): JsonElement? = try {
        context.assets.open(assetFileName).use { input ->
            InputStreamReader(input).use { reader ->
                JsonParser.parseReader(reader)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    /** Loads a JSON object; returns {} if not found or not an object. */
    fun loadObject(assetFileName: String): JsonObject = try {
        val el = parse(assetFileName)
        if (el != null && el.isJsonObject) el.asJsonObject else JsonObject()
    } catch (e: Exception) {
        e.printStackTrace()
        JsonObject()
    }

    /** Loads a JSON array; returns [] if not found or not an array. */
    fun loadArray(assetFileName: String): JsonArray = try {
        val el = parse(assetFileName)
        if (el != null && el.isJsonArray) el.asJsonArray else JsonArray()
    } catch (e: Exception) {
        e.printStackTrace()
        JsonArray()
    }

    /** Loads a typed list from a JSON array file. */
    inline fun <reified T> loadList(assetFileName: String): List<T> = try {
        context.assets.open(assetFileName).use { input ->
            InputStreamReader(input).use { reader ->
                val type = object : TypeToken<List<T>>() {}.type
                gson.fromJson(reader, type)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }

    /** Loads a typed object from a JSON object file. */
    inline fun <reified T> loadTypedObject(assetFileName: String): T? = try {
        context.assets.open(assetFileName).use { input ->
            InputStreamReader(input).use { reader ->
                val type = object : TypeToken<T>() {}.type
                gson.fromJson<T>(reader, type)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    // -------------------------
    // Legacy / existing loaders
    // -------------------------

    /** events.json */
    fun loadEvents(): List<GameEvent> = try {
        context.assets.open("events.json").use { input ->
            InputStreamReader(input).use { reader ->
                val listType = object : TypeToken<List<GameEvent>>() {}.type
                gson.fromJson(reader, listType)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }

    /** events_childhood_unique.json */
    fun loadChildhoodUniqueEvents(): List<GameEvent> = try {
        context.assets.open("events_childhood_unique.json").use { input ->
            InputStreamReader(input).use { reader ->
                val listType = object : TypeToken<List<GameEvent>>() {}.type
                gson.fromJson(reader, listType)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }

    /** scenarios.json — parsed into a small DTO to avoid collisions. */
    fun loadScenarios(): List<ScenarioAsset> = try {
        context.assets.open("scenarios.json").use { input ->
            InputStreamReader(input).use { reader ->
                val listType = object : TypeToken<List<ScenarioAsset>>() {}.type
                gson.fromJson(reader, listType)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }

    /** achievements.json — parsed into a small DTO. */
    fun loadAchievements(): List<AchievementAsset> = try {
        context.assets.open("achievements.json").use { input ->
            InputStreamReader(input).use { reader ->
                val listType = object : TypeToken<List<AchievementAsset>>() {}.type
                gson.fromJson(reader, listType)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }

    // -------------------------
    // Education loaders
    // -------------------------

    /** education_courses.json */
    fun loadEducationCourses(): List<EducationCourse> = try {
        context.assets.open("education_courses.json").use { input ->
            InputStreamReader(input).use { reader ->
                val arr = JsonParser.parseReader(reader).asJsonArray
                arr.map { el ->
                    val obj = el.asJsonObject
                    EducationCourse(
                        id = obj["id"].asString,
                        name = obj["name"].asString,
                        tier = obj["tier"].asInt,
                        level = EducationLevel.valueOf(obj["level"].asString),
                        cost = obj["cost"].asInt,
                        durationMonths = obj["durationMonths"].asInt,
                        requiredGpa = obj["requiredGpa"].asDouble,
                        prerequisites = obj["prerequisites"]?.asJsonArray?.map { it.asString } ?: emptyList(),
                        careerBoosts = obj["careerBoosts"]?.asJsonArray?.map { it.asString } ?: emptyList(),
                        flavorText = obj["flavorText"]?.asString ?: "",
                        milestones = obj["milestones"]?.asJsonArray?.map { it.asString } ?: emptyList(),
                        difficultyMod = obj["difficultyMod"]?.asDouble ?: 1.0
                    )
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }

    /** education_actions.json */
    fun loadEducationActions(): List<EducationActionDef> = try {
        context.assets.open("education_actions.json").use { input ->
            InputStreamReader(input).use { reader ->
                val listType = object : TypeToken<List<EducationActionDef>>() {}.type
                gson.fromJson(reader, listType)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

/** Small DTOs to keep this loader self-contained and non-breaking. */
data class ScenarioAsset(
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val tags: List<String>? = null
)
