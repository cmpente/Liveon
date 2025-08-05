package com.liveongames.domain.usecase

import com.liveongames.domain.model.Event
import com.liveongames.domain.repository.EventRepository
import javax.inject.Inject

class GetRandomEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(): List<Event> {
        return eventRepository.getRandomEvents()
    }
}