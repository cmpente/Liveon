// Updated Player.kt to match your JSON events
package com.altlifegames.altlife.data.models

import java.io.Serializable

data class Player(
    var id: Long = 0L,
    var firstName: String = "Newborn",
    var lastName: String = "Character",
    var age: Int = 0,
    var gender: Gender = Gender.OTHER,
    
    // Stats that match your JSON events
    var health: Int = 50,
    var happiness: Int = 50,
    var intelligence: Int = 50,
    var looks: Int = 50,
    var social: Int = 50,
    var karma: Int = 50,
    var stress: Int = 30,
    var finances: Int = 50,
    var fame: Int = 0,
    var sexuality: Int = 50,
    var addictionRisk: Int = 0,
    var fertility: Int = 50,
    var physicalHealth: Int = 50,
    var mentalHealth: Int = 50,
    
    var money: Int = 1000, // Starting money
    
    // Life progression
    var career: Career? = null,
    var education: Education? = null,
    var isAlive: Boolean = true,
    
    var statsHistory: MutableList<StatSnapshot> = mutableListOf()
) : Serializable {

    // Add a function to apply stat changes from events
    fun applyStatChanges(statChanges: Map<String, Int>) {
        statChanges.forEach { (statName, change) ->
            when (statName) {
                "health" -> health += change
                "happiness" -> happiness += change
                "intelligence" -> intelligence += change
                "looks" -> looks += change
                "social" -> social += change
                "karma" -> karma += change
                "stress" -> stress += change
                "finances" -> finances += change
                "fame" -> fame += change
                "sexuality" -> sexuality += change
                "addictionRisk" -> addictionRisk += change
                "fertility" -> fertility += change
                "physicalHealth" -> physicalHealth += change
                "mentalHealth" -> mentalHealth += change
                "money" -> money += change
            }
            // Clamp values between 0-100 (or appropriate ranges)
            clampStats()
        }
    }
    
    private fun clampStats() {
        health = health.coerceIn(0, 100)
        happiness = happiness.coerceIn(0, 100)
        intelligence = intelligence.coerceIn(0, 100)
        looks = looks.coerceIn(0, 100)
        social = social.coerceIn(0, 100)
        karma = karma.coerceIn(0, 100)
        stress = stress.coerceIn(0, 100)
        finances = finances.coerceIn(0, 100)
        fame = fame.coerceIn(0, 100)
        sexuality = sexuality.coerceIn(0, 100)
        addictionRisk = addictionRisk.coerceIn(0, 100)
        fertility = fertility.coerceIn(0, 100)
        physicalHealth = physicalHealth.coerceIn(0, 100)
        mentalHealth = mentalHealth.coerceIn(0, 100)
    }
    
    fun ageOneYear() {
        age++
        // Natural stat changes with aging
        if (age > 60) {
            health -= 1
            physicalHealth -= 1
        }
        if (age > 80) {
            health -= 2
            mentalHealth -= 1
        }
    }
}

enum class Gender {
    MALE, FEMALE, OTHER
}

data class StatSnapshot(
    val age: Int,
    val health: Int,
    val happiness: Int,
    val intelligence: Int,
    val looks: Int,
    val social: Int,
    val karma: Int,
    val stress: Int,
    val finances: Int
) : Serializable

data class Career(
    val name: String,
    val salary: Int,
    val startAge: Int,
    val isActive: Boolean = true,
    val level: Int = 1
) : Serializable

data class Education(
    val level: String,
    val graduated: Boolean = false,
    val graduationAge: Int? = null
) : Serializable