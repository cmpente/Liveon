package com.liveongames.data.model.education

import com.liveongames.domain.model.*

data class DialogChoice(
    val id: String,
    val label: String,
    val effects: ChoiceEffects
)

data class ChoiceEffects(
    val gpaMin: Double = 0.0,
    val gpaMax: Double = 0.0,
    val progress: Int = 0,
    val riskProb: Double = 0.0,
    val riskPenaltyGpa: Double = 0.0
)