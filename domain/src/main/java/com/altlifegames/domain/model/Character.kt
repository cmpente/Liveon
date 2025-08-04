// domain/src/main/java/com/altlifegames/domain/model/Character.kt
package com.altlifegames.domain.model

data class Character(
    val id: Long = 0L,
    val firstName: String = "",
    val lastName: String = "",
    val age: Int = 0,
    val health: Int = 50,
    val happiness: Int = 50,
    val intelligence: Int = 50,
    val money: Int = 1000,
    // ... other stats
)