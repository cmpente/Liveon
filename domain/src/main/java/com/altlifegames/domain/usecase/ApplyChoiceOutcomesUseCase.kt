package com.altlifegames.domain.usecase

import com.altlifegames.domain.model.Scenario
import com.altlifegames.domain.model.ScenarioChoice
import com.altlifegames.domain.model.ScenarioOutcome
import javax.inject.Inject

class ApplyChoiceOutcomesUseCase @Inject constructor() {

    operator fun invoke(
        scenario: Scenario,
        choice: ScenarioChoice,
        characterAttributes: Map<String, Int>
    ): Map<String, Int> {
        val updatedAttributes = characterAttributes.toMutableMap()

        // Use explicit type for destructuring
        for (outcome: ScenarioOutcome in choice.outcomes) {
            val attribute: String = outcome.attribute
            val change: Int = outcome.change

            // Apply the outcome
            val currentValue = updatedAttributes[attribute] ?: 0
            updatedAttributes[attribute] = currentValue + change
        }

        return updatedAttributes
    }
}