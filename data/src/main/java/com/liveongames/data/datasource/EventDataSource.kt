// data/src/main/java/com/liveongames/data/datasource/EventDataSource.kt
package com.liveongames.data.datasource

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liveongames.data.mapper.EventMapper
import com.liveongames.data.model.JsonEvent
import com.liveongames.domain.model.Event
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class EventDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val eventMapper: EventMapper
) {

    fun loadAllEventsFromAssets(): List<Event> {
        val allEvents = mutableListOf<Event>()

        // Load different JSON files
        val eventFiles = listOf(
            "events.json",
            "events_childhood_unique.json"
        )

        Log.d("EventDataSource", "Attempting to load ${eventFiles.size} event files")

        eventFiles.forEach { fileName ->
            try {
                val events = loadEventsFromFile(fileName)
                Log.d("EventDataSource", "Loaded ${events.size} events from $fileName")
                allEvents.addAll(events)
            } catch (e: Exception) {
                Log.e("EventDataSource", "Error loading events from $fileName", e)
            }
        }

        Log.d("EventDataSource", "Total events loaded: ${allEvents.size}")
        return allEvents
    }

    private fun loadEventsFromFile(fileName: String): List<Event> {
        return try {
            Log.d("EventDataSource", "Loading events from $fileName")
            val inputStream = context.assets.open(fileName)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            Log.d("EventDataSource", "JSON content length: ${jsonString.length}")

            val jsonEventType = object : TypeToken<List<JsonEvent>>() {}.type
            val jsonEvents: List<JsonEvent> = gson.fromJson(jsonString, jsonEventType)
            Log.d("EventDataSource", "Parsed ${jsonEvents.size} JSON events")

            val domainEvents = eventMapper.mapJsonEventsToDomain(jsonEvents)
            Log.d("EventDataSource", "Mapped to ${domainEvents.size} domain events")

            domainEvents
        } catch (e: Exception) {
            Log.e("EventDataSource", "Error loading events from $fileName", e)
            emptyList()
        }
    }
}