package com.liveongames.domain.model

data class Character(
    val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val health: Int = 100,
    val happiness: Int = 50,
    val money: Int = 1000,
    val intelligence: Int = 10,
    val fitness: Int = 10,
    val social: Int = 10,
    val education: Int = 0,
    val career: String? = null,
    val relationships: List<Relationship> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val events: List<Event> = emptyList()
)