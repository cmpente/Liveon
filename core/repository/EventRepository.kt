package com.liveongames.core.repository

import com.liveongames.core.data.EventDao
import com.liveongames.core.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventRepository(private val eventDao: EventDao) {
    suspend fun getEvent(id: Long): Event? = withContext(Dispatchers.IO) {
        eventDao.getEvent(id)
    }

    suspend fun getAllEvents(): List<Event> = withContext(Dispatchers.IO) {
        eventDao.getAllEvents()
    }

    suspend fun insertEvent(event: Event): Long = withContext(Dispatchers.IO) {
        eventDao.insertEvent(event)
    }

    suspend fun updateEvent(event: Event) = withContext(Dispatchers.IO) {
        eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: Event) = withContext(Dispatchers.IO) {
        eventDao.deleteEvent(event)
    }
}
