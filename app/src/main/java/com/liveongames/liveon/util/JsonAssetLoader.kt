package com.liveongames.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.liveongames.domain.model.GameEvent
import com.liveongames.domain.model.EducationLevel
import com.liveongames.liveon.model.EducationCourse
import java.io.InputStreamReader

class JsonAssetLoader(private val context: Context) {
    private val gson = Gson()

    fun loadEvents(): List<GameEvent> {
        return try {
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
    }

    fun loadEducationCourses(): List<EducationCourse> {
        return try {
            context.assets.open("education_courses.json").use { input ->
                InputStreamReader(input).use { reader ->
                    val root = JsonParser.parseReader(reader)
                    val array = root.asJsonArray
                    array.map { elem ->
                        val obj = elem.asJsonObject
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
                            schoolCrestResId = if (obj.has("schoolCrestResId")) obj["schoolCrestResId"].asString else null,
                            milestones = obj["milestones"]?.asJsonArray?.map { it.asString } ?: emptyList()
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
