// domain/src/main/java/com/liveongames/domain/usecase/ApplyEventUseCase.kt
package com.liveongames.domain.usecase

import com.liveongames.domain.model.Event
import com.liveongames.domain.model.EventOutcome
import com.liveongames.domain.model.CharacterStats
import javax.inject.Inject

class ApplyEventUseCase @Inject constructor() {
    operator fun invoke(characterStats: CharacterStats, event: Event, chosenOutcome: EventOutcome): CharacterStats {
        var updatedStats = characterStats.copy() // Start with a copy of the original stats

        // Apply stat changes from the chosen outcome
        for ((statName, change) in chosenOutcome.statChanges) {
            when (statName) {
                "health" -> updatedStats = updatedStats.copy(health = updatedStats.health + change)
                "happiness" -> updatedStats = updatedStats.copy(happiness = updatedStats.happiness + change)
                "smarts" -> updatedStats = updatedStats.copy(smarts = updatedStats.smarts + change)
                "looks" -> updatedStats = updatedStats.copy(looks = updatedStats.looks + change)
                "money" -> updatedStats = updatedStats.copy(money = updatedStats.money + change)
                "reputation" -> updatedStats = updatedStats.copy(reputation = updatedStats.reputation + change)
                // Add other stats here as needed
            }
        }

        // TODO: Implement logic to handle ageProgression, achievements, and consequences
        return updatedStats
    }
}