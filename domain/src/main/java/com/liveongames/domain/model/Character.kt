// domain/src/main/java/com/liveongames/domain/model/Character.kt
package com.liveongames.domain.model

data class Character(
    val id: String,
    val name: String,
    val age: Int,
    val health: Int,
    val happiness: Int,
    val money: Int,
    val intelligence: Int,
    val fitness: Int,
    val social: Int,
    val education: Int,
    val career: String?,
    val relationships: List<String>,
    val achievements: List<String>,
    val events: List<String>,
    val jailTime: Int,
    val notoriety: Int = 0
)