package com.altlifegames.core.content

import android.content.Context
import com.altlifegames.core.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object JsonContentLoader {
    private val gson = Gson()

    fun loadAllEvents(context: Context): List<Event> {
        val assetManager = context.assets
        val files = assetManager.list("")?.filter { it.startsWith("events_en_") && it.endsWith(".json") } ?: emptyList()
        return files.flatMap { loadEvents(context, it) }
    }

    fun loadAllScenarios(context: Context): List<Scenario> {
        val assetManager = context.assets
        val files = assetManager.list("")?.filter { it.startsWith("scenarios_en_") && it.endsWith(".json") } ?: emptyList()
        return files.flatMap { loadScenarios(context, it) }
    }

    fun loadAllAchievements(context: Context): List<Achievement> {
        val assetManager = context.assets
        val files = assetManager.list("")?.filter { it.startsWith("achievements_en_") && it.endsWith(".json") } ?: emptyList()
        return files.flatMap { loadAchievements(context, it) }
    }

    fun loadEvents(context: Context, assetFileName: String): List<Event> {
        val json = context.assets.open(assetFileName).bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<Event>>() {}.type
        return gson.fromJson(json, listType)
    }

    fun loadAchievements(context: Context, assetFileName: String): List<Achievement> {
        val json = context.assets.open(assetFileName).bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<Achievement>>() {}.type
        return gson.fromJson(json, listType)
    }

    fun loadScenarios(context: Context, assetFileName: String): List<Scenario> {
        val json = context.assets.open(assetFileName).bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<Scenario>>() {}.type
        return gson.fromJson(json, listType)
    }
}
