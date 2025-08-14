// domain/src/main/java/com/liveongames/domain/usecase/ApplyEventUseCase.kt
package com.liveongames.domain.usecase

import com.liveongames.domain.model.Event
import com.liveongames.domain.model.EventOutcome
import com.liveongames.domain.model.CharacterStats
import javax.inject.Inject

class ApplyEventUseCase @Inject constructor() {
    operator fun invoke(characterStats: CharacterStats, event: Event, chosenOutcome: EventOutcome): CharacterStats {
        val updatedStats = characterStats.toMutableMap()

        // Apply stat changes from the chosen outcome
        for ((statName, change) in chosenOutcome.statChanges) {
            val currentStatValue = updatedStats[statName] ?: 0
            updatedStats[statName] = currentStatValue + change
        }

        // TODO: Implement logic to handle ageProgression, achievements, and consequences

        return CharacterStats(
            health = updatedStats["health"] ?: characterStats.health,
            happiness = updatedStats["happiness"] ?: characterStats.happiness,
            smarts = updatedStats["smarts"] ?: characterStats.smarts,
            looks = updatedStats["looks"] ?: characterStats.looks,
            money = updatedStats["money"] ?: characterStats.money,
            reputation = updatedStats["reputation"] ?: characterStats.reputation
            // Add other stats here
        )
    }
}