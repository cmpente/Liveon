package com.liveongames.liveon.data.datasource

import android.content.Context
import com.liveongames.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liveongames.domain.model.EducationOption
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    fun loadEvents(): List<Event> {
        return try {
            val inputStream = context.assets.open("events.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<Event>>() {}.type
            gson.fromJson(reader, type) ?: emptyList()
        } catch (e: Exception) {
            println("Error loading events: ${e.message}")
            emptyList()
        }
    }

    fun loadAchievements(): List<Achievement> {
        return try {
            val inputStream = context.assets.open("achievements.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<Achievement>>() {}.type
            gson.fromJson(reader, type) ?: emptyList()
        } catch (e: Exception) {
            println("Error loading achievements: ${e.message}")
            emptyList()
        }
    }

    fun loadScenarios(): List<Scenario> {
        return try {
            val inputStream = context.assets.open("scenarios.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<Scenario>>() {}.type
            gson.fromJson(reader, type) ?: emptyList()
        } catch (e: Exception) {
            println("Error loading scenarios: ${e.message}")
            emptyList()
        }
    }

    fun loadTraits(): List<Trait> {
        return try {
            val inputStream = context.assets.open("traits.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<Trait>>() {}.type
            gson.fromJson(reader, type) ?: emptyList()
        } catch (e: Exception) {
            println("Error loading traits: ${e.message}")
            emptyList()
        }
    }

    fun loadClubs(): List<Club> {
        return try {
            val inputStream = context.assets.open("clubs.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<Club>>() {}.type
            gson.fromJson(reader, type) ?: emptyList()
        } catch (e: Exception) {
            println("Error loading clubs: ${e.message}")
            emptyList()
        }
    }

    fun loadEducationOptions(): List<EducationOption> {
        return try {
            val inputStream = context.assets.open("education.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<EducationOption>>() {}.type
            gson.fromJson(reader, type) ?: emptyList()
        } catch (e: Exception) {
            println("Error loading education options: ${e.message}")
            emptyList()
        }
    }
}