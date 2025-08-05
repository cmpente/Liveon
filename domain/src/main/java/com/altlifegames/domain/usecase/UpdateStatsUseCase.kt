package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.CharacterStats
import javax.inject.Inject

class UpdateStatsUseCase @Inject constructor() {
    operator fun invoke(currentStats: CharacterStats, changes: Map<String, Int>): CharacterStats {
        var newStats = currentStats
        changes.forEach { (stat, change) ->
            newStats = when (stat) {
                "health" -> newStats.copy(health = newStats.health + change)
                "happiness" -> newStats.copy(happiness = newStats.happiness + change)
                "intelligence" -> newStats.copy(intelligence = newStats.intelligence + change)
                "money" -> newStats.copy(money = newStats.money + change)
                "social" -> newStats.copy(social = newStats.social + change)
                else -> newStats
            }
        }
        return newStats
    }
}