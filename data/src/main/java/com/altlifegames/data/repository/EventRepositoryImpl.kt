package com.altlifegames.data.repository

import com.altlifegames.domain.model.Event
import com.altlifegames.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor() : EventRepository {

    private val events = MutableStateFlow<List<Event>>(emptyList())

    override fun getActiveEvents(): Flow<List<Event>> {
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
        return events.value
    }

    override suspend fun getYearlyEvents(): List<Event> {
        return events.value.filter { it.type == "yearly" }
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
}