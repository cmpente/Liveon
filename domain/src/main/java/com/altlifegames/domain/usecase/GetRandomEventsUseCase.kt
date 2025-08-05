package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.Event
import com.altlifegames.domain.repository.EventRepository
import javax.inject.Inject

class GetRandomEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(): List<Event> {
        return eventRepository.getRandomEvents()
    }
}