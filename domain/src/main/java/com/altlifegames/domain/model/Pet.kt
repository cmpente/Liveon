package com.altlifegames.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Pet(
    val id: Long = 0,
    val name: String = "",
    val petType: String = "",
    val age: Int = 0,
    val health: Stat = Stat(),
    val happiness: Stat = Stat()
)

enum class PetType {
    DOG, CAT, BIRD, FISH, RABBIT, REPTILE, EXOTIC
}