package com.altlifegames.domain.model

data class SaveSlot(
    val id: String = "",
    val characterName: String = "",
    val age: Int = 0,
    val lastPlayed: Long = 0,
    val scenarioId: String = ""
)