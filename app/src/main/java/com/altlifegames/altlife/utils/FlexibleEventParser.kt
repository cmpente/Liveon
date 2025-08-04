// FlexibleEventParser.kt
package com.altlifegames.altlifealpha.utils

import com.google.gson.*
import com.altlifegames.altlifealpha.data.models.GameEvent
import com.altlifegames.altlifealpha.data.models.EventChoice
import com.altlifegames.altlifealpha.data.models.EventOutcome
import java.lang.reflect.Type

class FlexibleEventDeserializer : JsonDeserializer<GameEvent> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): GameEvent {
        val jsonObject = json.asJsonObject
        
        return GameEvent(
            id = jsonObject.get("id").asString,
            title = jsonObject.get("title").asString,
            description = jsonObject.get("description").asString,
            type = jsonObject.get("type")?.asString ?: "NEUTRAL",
            isMature = jsonObject.get("isMature")?.asBoolean ?: false,
            choices = parseChoices(jsonObject.get("choices").asJsonArray),
            minAge = jsonObject.get("minAge").asInt,
            maxAge = jsonObject.get("maxAge").asInt,
            probability = jsonObject.get("probability")?.asDouble ?: 1.0,
            isRepeatable = jsonObject.get("isRepeatable")?.asBoolean ?: false,
            category = jsonObject.get("category")?.asString ?: "life"
        )
    }
    
    private fun parseChoices(choicesArray: JsonArray): List<EventChoice> {
        return choicesArray.map { choiceElement ->
            val choiceObject = choiceElement.asJsonObject
            
            // Handle both formats
            val description = choiceObject.get("description")?.asString 
                ?: choiceObject.get("text")?.asString 
                ?: ""
            
            val outcomes = parseOutcomes(choiceObject.get("outcomes").asJsonArray)
            
            EventChoice(
                id = choiceObject.get("id")?.asString,
                description = description,
                outcomes = outcomes,
                text = choiceObject.get("text")?.asString
            )
        }
    }
    
    private fun parseOutcomes(outcomesArray: JsonArray): List<EventOutcome> {
        return outcomesArray.map { outcomeElement ->
            val outcomeObject = outcomeElement.asJsonObject
            
            EventOutcome(
                description = outcomeObject.get("description").asString,
                statChanges = parseStatChanges(outcomeObject.get("statChanges").asJsonObject),
                ageProgression = outcomeObject.get("ageProgression")?.asInt ?: 0
            )
        }
    }
    
    private fun parseStatChanges(statChangesObject: JsonObject): Map<String, Int> {
        val statChanges = mutableMapOf<String, Int>()
        statChangesObject.entrySet().forEach { (key, value) ->
            statChanges[key] = value.asInt
        }
        return statChanges
    }
}