// domain/src/main/java/com/liveongames/domain/model/CharacterStats.kt
package com.liveongames.domain.model

data class CharacterStats(
    val health: Int = 100,
    val happiness: Int = 50,
    val intelligence: Int = 20,
    val money: Int = 1000,
    val social: Int = 30,
    val age: Int = 18,
    val smarts: Int = 20, // Added smarts property
    val reputation: Int = 30, // Added reputation property
    val fitness: Int = 50 // Added fitness property
)