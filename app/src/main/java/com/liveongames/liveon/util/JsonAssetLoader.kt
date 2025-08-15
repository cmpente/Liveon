// app/src/main/java/com/liveongames/liveon/util/JsonAssetLoader.kt
package com.liveongames.liveon.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.liveongames.domain.model.AcademicSchema
import com.liveongames.domain.model.EducationProgram
import com.liveongames.domain.model.EduTier
import com.liveongames.domain.model.GameEvent
import com.liveongames.data.model.education.EducationActionDef
import java.io.InputStreamReader
import com.liveongames.data.assets.achievements.AchievementAsset

/**
 * Unified assets loader:
 * - Keeps existing loaders (events, scenarios, achievements).
 * - Adds Education loaders (courses + actions).
 * - Safe: returns empty defaults on failures.
 *
 * Updated to correctly parse and pass the 'schema' parameter
 * for EducationProgram based on the provided data class.
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
    fun loadEducationCourses(): List<EducationProgram> = try {
        context.assets.open("education_courses.json").use { input ->
            InputStreamReader(input).use { reader ->
                val arr = JsonParser.parseReader(reader).asJsonArray
                arr.mapNotNull { el ->
                    try {
                        val obj = el.asJsonObject

                        // Parse schema
                        val schemaObj = obj.getAsJsonObject("schema") ?: JsonObject()
                        val schema = AcademicSchema(
                            displayPeriodName = schemaObj.get("displayPeriodName")?.asString ?: "Period",
                            periodsPerYear = schemaObj.get("periodsPerYear")?.asInt ?: 1,
                            totalPeriods = schemaObj.get("totalPeriods")?.asInt ?: 1,
                            groupingLabel = schemaObj.get("groupingLabel")?.asString
                        )

                        // Map tier string → EduTier enum
                        val tierStr = obj.get("tier")?.asString ?: return@mapNotNull null
                        val tier = when (tierStr.uppercase()) {
                            "ELEMENTARY" -> EduTier.ELEMENTARY
                            "MIDDLE" -> EduTier.MIDDLE
                            "HIGH" -> EduTier.HIGH
                            "CERT" -> EduTier.CERT
                            "ASSOC" -> EduTier.ASSOC
                            "BACH" -> EduTier.BACH
                            "MAST" -> EduTier.MAST
                            "PHD" -> EduTier.PHD
                            else -> EduTier.ELEMENTARY
                        }

                        // Construct EducationProgramImpl
                        EducationProgramImpl(
                            id = obj.get("id")?.asString ?: return@mapNotNull null,
                            title = obj.get("title")?.asString ?: "Unnamed Program",
                            description = obj.get("description")?.asString ?: "",
                            tier = tier,
                            schema = schema,
                            minGpa = obj.get("minGpa")?.asDouble ?: 0.0,
                            tuition = obj.get("tuition")?.asInt ?: 0,
                            requirements = obj.getAsJsonArray("requirements")
                                ?.mapNotNull { it?.asString }?.toSet() ?: emptySet()
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }

    /**
     * Helper function to map JSON tier strings to integer IDs.
     * This is necessary because your data class expects `tier: Int`.
     * You should define a consistent mapping based on your game's tier system.
     * Adjust the IDs as needed for your application.
     * Tier IDs match `EduTier` enum ordinal values for clarity.
     * ELEMENTARY(0), MIDDLE(1), HIGH(2), CERT(3), ASSOC(4), BACH(5), MAST(6), PHD(7)
     */
    private fun mapStringTierToInt(tierStr: String): Int {
        // Use a `when` expression for clear mapping
        return when (tierStr.uppercase()) {
            "ELEMENTARY" -> 0 // EduTier.ELEMENTARY.ordinal
            "MIDDLE" -> 1     // EduTier.MIDDLE.ordinal
            "HIGH" -> 2       // EduTier.HIGH.ordinal
            "CERT" -> 3       // EduTier.CERT.ordinal
            "ASSOC" -> 4      // EduTier.ASSOC.ordinal
            "BACH" -> 5       // EduTier.BACH.ordinal
            "MAST" -> 6       // EduTier.MAST.ordinal
            "PHD" -> 7        // EduTier.PHD.ordinal
            else -> {
                // Log a warning for unknown tiers and default to a base tier (e.g., ELEMENTARY)
                println("Warning: Unknown tier string '$tierStr' encountered. Defaulting to ELEMENTARY (0).")
                0 // Default to ELEMENTARY
            }
        }
    }

    // Existing actions loader (assumed to be working or for future implementation)
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

// =====================
// Local Implementation
// =====================

/**
 * Data class implementing the EducationProgram interface.
 */
data class EducationProgramImpl(
    override val id: String,
    override val title: String,
    override val description: String,
    override val tier: EduTier,
    override val schema: AcademicSchema,
    override val minGpa: Double,
    override val tuition: Int,
    override val requirements: Set<String>
) : EducationProgram

