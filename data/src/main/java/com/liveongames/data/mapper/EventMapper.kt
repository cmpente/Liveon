// data/src/main/java/com/liveongames/data/mapper/EventMapper.kt
package com.liveongames.data.mapper

import com.liveongames.data.model.JsonEvent
import com.liveongames.domain.model.Event
import com.liveongames.domain.model.EventChoice
import com.liveongames.domain.model.EventOutcome
import javax.inject.Inject

class EventMapper @Inject constructor() {

    fun mapJsonEventsToDomain(jsonEvents: List<JsonEvent>): List<Event> {
        return jsonEvents.map { jsonEvent ->
            Event(
                id = jsonEvent.id.toString(),
                title = jsonEvent.title,
                description = jsonEvent.description,
                type = jsonEvent.type ?: "NEUTRAL",
                isMature = jsonEvent.isMature ?: false,
                isShown = false,
                minAge = jsonEvent.minAge ?: 0,
                maxAge = jsonEvent.maxAge ?: 100,
                probability = jsonEvent.probability ?: 1.0,
                isRepeatable = jsonEvent.isRepeatable ?: false,
                category = jsonEvent.category ?: "life",
                choices = jsonEvent.choices.mapIndexed { index, jsonChoice ->
                    EventChoice(
                        id = jsonChoice.id ?: "${jsonEvent.id}_choice_$index",
                        description = jsonChoice.description,
                        text = jsonChoice.text ?: jsonChoice.description,
                        outcomes = if (jsonChoice.outcomes != null) {
                            jsonChoice.outcomes.map { jsonOutcome ->
                                EventOutcome(
                                    description = jsonOutcome.description,
                                    statChanges = jsonOutcome.statChanges,
                                    ageProgression = jsonOutcome.ageProgression
                                )
                            }
                        } else if (jsonChoice.result != null) {
                            // Handle childhood event format
                            listOf(
                                EventOutcome(
                                    description = jsonChoice.result.outcome,
                                    statChanges = jsonChoice.result.statChanges
                                )
                            )
                        } else {
                            emptyList()
                        }
                    )
                }
            )
        }
    }
}