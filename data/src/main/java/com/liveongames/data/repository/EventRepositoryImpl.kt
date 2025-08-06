// data/src/main/java/com/liveongames/data/repository/EventRepositoryImpl.kt
package com.liveongames.data.repository

import android.util.Log
import com.liveongames.data.datasource.EventDataSource
import com.liveongames.domain.model.Event
import com.liveongames.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import kotlin.random.Random

class EventRepositoryImpl @Inject constructor(
    private val eventDataSource: EventDataSource
) : EventRepository {

    private val events = MutableStateFlow<List<Event>>(emptyList())

    init {
        Log.d("EventRepository", "Initializing EventRepository")
        loadEventsFromAssets()
    }

    private fun loadEventsFromAssets() {
        Log.d("EventRepository", "Loading events from assets...")
        val loadedEvents = eventDataSource.loadAllEventsFromAssets()
        Log.d("EventRepository", "Setting ${loadedEvents.size} events in repository")
        events.value = loadedEvents
    }

    override fun getActiveEvents(): Flow<List<Event>> {
        Log.d("EventRepository", "getActiveEvents called, current events: ${events.value.size}")
        return events
    }

    override suspend fun addEvent(event: Event) {
        val currentEvents = events.value.toMutableList()
        currentEvents.add(event)
        events.value = currentEvents
    }

    override suspend fun removeEvent(eventId: String) {
        val currentEvents = events.value.toMutableList()
        val index = currentEvents.indexOfFirst { it.id == eventId }
        if (index != -1) {
            currentEvents.removeAt(index)
            events.value = currentEvents
        }
    }

    override suspend fun getRandomEvents(): List<Event> {
        Log.d("EventRepository", "Getting random events from ${events.value.size} total events")
        val randomEvents = events.value.filter { event ->
            (event.type == "random" || event.type == "NEUTRAL") &&
                    Random.nextDouble() <= event.probability
        }
        Log.d("EventRepository", "Filtered to ${randomEvents.size} random events")
        return randomEvents
    }

    override suspend fun getYearlyEvents(): List<Event> {
        return events.value.filter { it.type == "LIFE_MILESTONE" || it.type == "yearly" }
    }

    override suspend fun markEventAsShown(eventId: String) {
        val currentEvents = events.value.toMutableList()
        val index = currentEvents.indexOfFirst { it.id == eventId }
        if (index != -1) {
            val event = currentEvents[index].copy(isShown = true)
            currentEvents[index] = event
            events.value = currentEvents
        }
    }

    fun reloadEvents() {
        Log.d("EventRepository", "Reloading events...")
        loadEventsFromAssets()
    }
}