// domain/src/main/java/com/liveongames/domain/usecase/AddEventUseCase.kt
package com.liveongames.domain.usecase

import com.liveongames.domain.model.Event
import com.liveongames.domain.repository.EventRepository
import javax.inject.Inject

class AddEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(event: Event) {
        eventRepository.addEvent(event)
    }
}