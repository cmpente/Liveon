package com.liveongames.domain.repository

import com.liveongames.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getActiveEvents(): Flow<List<Event>>
    suspend fun addEvent(event: Event)
    suspend fun removeEvent(eventId: String)
    suspend fun getRandomEvents(): List<Event>
    suspend fun getYearlyEvents(): List<Event>
    suspend fun markEventAsShown(eventId: String)
}