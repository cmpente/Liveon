// app/src/main/java/com/liveongames/liveon/FlexibleEventParser.kt
package com.liveongames.liveon

import com.google.gson.*
import com.liveongames.domain.model.GameEvent
import com.liveongames.domain.model.EventChoice
import com.liveongames.domain.model.EventOutcome
import java.lang.reflect.Type

class FlexibleEventParser : JsonDeserializer<GameEvent> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): GameEvent {
        val jsonObject = json.asJsonObject

        return GameEvent(
            id = jsonObject.get("id")?.asString ?: "",
            title = jsonObject.get("title")?.asString ?: "",
            description = jsonObject.get("description")?.asString ?: "",
            choices = parseChoices(jsonObject.get("choices")?.asJsonArray ?: JsonArray()),
            type = jsonObject.get("type")?.asString ?: "NEUTRAL",
            isMature = jsonObject.get("isMature")?.asBoolean ?: false,
            minAge = jsonObject.get("minAge")?.asInt ?: 0,
            maxAge = jsonObject.get("maxAge")?.asInt ?: 100,
            probability = jsonObject.get("probability")?.asDouble ?: 1.0,
            isRepeatable = jsonObject.get("isRepeatable")?.asBoolean ?: false,
            category = jsonObject.get("category")?.asString ?: "life"
        )
    }

    private fun parseChoices(choicesArray: JsonArray): List<EventChoice> {
        return choicesArray.map { choiceElement ->
            val choiceObject = choiceElement.asJsonObject

            // Handle both formats
            val text = choiceObject.get("text")?.asString ?: ""
            val description = choiceObject.get("description")?.asString ?: text

            val outcomes = parseOutcomes(choiceObject.get("outcomes")?.asJsonArray ?: JsonArray())

            EventChoice(
                id = choiceObject.get("id")?.asString ?: "",
                text = text,
                description = description,
                outcomes = outcomes
            )
        }
    }

    private fun parseOutcomes(outcomesArray: JsonArray): List<EventOutcome> {
        return outcomesArray.map { outcomeElement ->
            val outcomeObject = outcomeElement.asJsonObject

            EventOutcome(
                attribute = outcomeObject.get("attribute")?.asString ?: "",
                change = outcomeObject.get("change")?.asInt ?: 0,
                description = outcomeObject.get("description")?.asString ?: ""
            )
        }
    }
}