package com.altlifegames.domain.usecase

import com.altlifegames.domain.repository.EventRepository
import javax.inject.Inject

class MarkEventAsShownUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String) {
        eventRepository.markEventAsShown(eventId)
    }
}