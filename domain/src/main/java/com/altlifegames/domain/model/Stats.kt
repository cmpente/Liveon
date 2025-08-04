package com.altlifegames.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Stats(
    val health: Stat = Stat(),
    val happiness: Stat = Stat(),
    val energy: Stat = Stat(),
    val money: Stat = Stat(),
    val reputation: Stat = Stat()
)