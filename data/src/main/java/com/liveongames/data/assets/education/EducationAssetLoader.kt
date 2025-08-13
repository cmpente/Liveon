package com.liveongames.data.assets.education

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.liveongames.data.model.education.EducationActionDef
import com.liveongames.domain.model.AcademicSchema
import com.liveongames.domain.model.EduTier
import com.liveongames.domain.model.EducationProgram
import com.liveongames.domain.model.impl.EducationProgramImpl
import java.io.InputStreamReader

class EducationAssetLoader(private val context: Context) {

    private val gson = Gson()

    // ---- helpers -----------------------------------------------------------

    private fun JsonElement?.asStringOrNull(): String? = try {
        if (this == null || this is JsonNull || this.isJsonNull) null else this.asString
    } catch (_: Exception) { null }

    private fun JsonElement?.asIntOrNull(): Int? = try {
        if (this == null || this is JsonNull || this.isJsonNull) null else this.asInt
    } catch (_: Exception) { null }

    private fun JsonElement?.asDoubleOrNull(): Double? = try {
        if (this == null || this is JsonNull || this.isJsonNull) null else this.asDouble
    } catch (_: Exception) { null }

    // ---- Courses -----------------------------------------------------------

    suspend fun loadCourses(): List<EducationProgram> = try {
        context.assets.open("education_courses.json").use { input ->
            InputStreamReader(input).use { reader ->
                val arr = JsonParser.parseReader(reader).asJsonArray

                arr.mapNotNull { el ->
                    try {
                        val obj = el.asJsonObject

                        // Schema (null-safe)
                        val schemaObj = obj.getAsJsonObject("schema") ?: return@mapNotNull null
                        val periodsPerYear = schemaObj.get("periodsPerYear").asIntOrNull() ?: 1
                        val totalPeriods = schemaObj.get("totalPeriods").asIntOrNull() ?: periodsPerYear

                        val schema = AcademicSchema(
                            displayPeriodName = schemaObj.get("displayPeriodName").asStringOrNull() ?: "Period",
                            periodsPerYear = periodsPerYear,
                            totalPeriods = totalPeriods,
                            groupingLabel = schemaObj.get("groupingLabel").asStringOrNull()
                        )

                        // Tier
                        val tierStr = obj.get("tier").asStringOrNull()?.uppercase() ?: "ELEMENTARY"
                        val tier = when (tierStr) {
                            "ELEMENTARY" -> EduTier.ELEMENTARY
                            "MIDDLE"     -> EduTier.MIDDLE
                            "HIGH"       -> EduTier.HIGH
                            "CERT"       -> EduTier.CERT
                            "ASSOC"      -> EduTier.ASSOC
                            "BACH"       -> EduTier.BACH
                            "MAST"       -> EduTier.MAST
                            "PHD"        -> EduTier.PHD
                            else -> {
                                println("Warning: Unknown tier '$tierStr', defaulting to ELEMENTARY")
                                EduTier.ELEMENTARY
                            }
                        }

                        // Core fields
                        val id = obj.get("id").asStringOrNull() ?: return@mapNotNull null
                        val title = obj.get("title").asStringOrNull() ?: "Unnamed Program"
                        val description = obj.get("description").asStringOrNull() ?: ""

                        val minGpa = obj.get("minGpa").asDoubleOrNull() ?: 0.0
                        val tuition = obj.get("tuition").asIntOrNull() ?: 0

                        val requirements = obj.getAsJsonArray("requirements")
                            ?.mapNotNull { it.asStringOrNull() }
                            ?.toSet()
                            ?: emptySet()

                        EducationProgramImpl(
                            id = id,
                            title = title,
                            description = description,
                            tier = tier,
                            schema = schema,
                            minGpa = minGpa,
                            tuition = tuition,
                            requirements = requirements
                        )
                    } catch (e: Exception) {
                        println("Error parsing education course: ${e.message}")
                        e.printStackTrace()
                        null
                    }
                }
            }
        }
    } catch (e: Exception) {
        println("Error loading education courses: ${e.message}")
        e.printStackTrace()
        emptyList()
    }

    // ---- Actions -----------------------------------------------------------

    suspend fun loadActions(): List<EducationActionDef> = try {
        context.assets.open("education_actions.json").use { input ->
            InputStreamReader(input).use { reader ->
                val listType = object : TypeToken<List<EducationActionDef>>() {}.type
                gson.fromJson(reader, listType)
            }
        }
    } catch (e: Exception) {
        println("Error loading education actions: ${e.message}")
        e.printStackTrace()
        emptyList()
    }
}
