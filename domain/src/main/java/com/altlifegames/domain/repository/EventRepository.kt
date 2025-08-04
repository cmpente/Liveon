package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Event

interface EventRepository {
    suspend fun getRandomEventForAge(age: Int): Event?
    suspend fun getEventDefinition(eventId: String): Event?
    suspend fun loadEvents()
}