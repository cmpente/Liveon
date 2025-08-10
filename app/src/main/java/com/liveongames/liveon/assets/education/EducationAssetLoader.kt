package com.liveongames.liveon.assets.education

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.liveongames.domain.model.EducationLevel
import com.liveongames.liveon.assets.common.RawAssetReader
import com.liveongames.liveon.model.EducationActionDef
import com.liveongames.liveon.model.EducationCourse

class EducationAssetLoader(
    private val reader: RawAssetReader,
    private val gson: Gson
) {
    companion object {
        private const val COURSES_FILE = "education_courses.json"
        private const val ACTIONS_FILE = "education_actions.json"
    }

    fun loadCourses(): List<EducationCourse> = try {
        val arr = reader.loadJsonArray(COURSES_FILE)
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
    } catch (e: Exception) { e.printStackTrace(); emptyList() }

    fun loadActions(): List<EducationActionDef> {
        val type = object : TypeToken<List<EducationActionDef>>() {}.type
        return reader.loadArray(ACTIONS_FILE, gson, type)
    }
}
