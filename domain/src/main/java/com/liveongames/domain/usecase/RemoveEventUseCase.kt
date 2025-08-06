// domain/src/main/java/com/liveongames/domain/usecase/RemoveEventUseCase.kt
package com.liveongames.domain.usecase

import com.liveongames.domain.repository.EventRepository
import javax.inject.Inject

class RemoveEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String) {
        eventRepository.removeEvent(eventId)
    }
}