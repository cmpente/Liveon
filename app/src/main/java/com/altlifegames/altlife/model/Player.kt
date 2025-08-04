package com.altlifegames.altlife.model

data class Player(
    var firstName: String = "",
    var lastName: String = "",
    var age: Int = 0,
    var gender: String = "OTHER",
    
    // Core stats (0-100) - Random at start
    var health: Int = (Math.random() * 30 + 70).toInt(), // 70-100
    var happiness: Int = (Math.random() * 30 + 70).toInt(), // 70-100
    var intelligence: Int = (Math.random() * 40 + 30).toInt(), // 30-70
    var social: Int = (Math.random() * 40 + 30).toInt(), // 30-70
    var money: Int = 0, // Always starts at zero
    
    // Life aspects
    var educationLevel: Int = 0, // 0=none, 1=highschool, 2=college, 3=degree
    var careerLevel: Int = 0,    // 0=unemployed, 1=entry, 2=mid, 3=senior, 4=executive
    var relationshipStatus: String = "single", // single, dating, married, divorced
    var children: Int = 0,
    
    // Education tracking
    var isInSchool: Boolean = false,
    var schoolYearsCompleted: Int = 0,
    var requiredSchoolYears: Int = 0,
    
    // Achievements
    var achievements: MutableSet<String> = mutableSetOf(),
    
    // Year tracking
    var currentYear: Int = 2024
) {
    val fullName: String get() = "$firstName $lastName"
    val educationName: String get() = when(educationLevel) {
        0 -> "None"
        1 -> "High School"
        2 -> "College"
        3 -> "University Degree"
        else -> "None"
    }
    
    val careerName: String get() = when(careerLevel) {
        0 -> "Unemployed"
        1 -> "Entry Level"
        2 -> "Mid Level"
        3 -> "Senior Level"
        4 -> "Executive"
        else -> "Unemployed"
    }
    
    fun canAfford(amount: Int): Boolean = money >= amount
    
    fun addMoney(amount: Int) {
        money = (money + amount).coerceAtLeast(0)
    }

    fun modifyStats(healthChange: Int = 0, happinessChange: Int = 0,
                    intelligenceChange: Int = 0, socialChange: Int = 0,
                    moneyChange: Int = 0) {
        health = (health + healthChange).coerceIn(0, 100)
        happiness = (happiness + happinessChange).coerceIn(0, 100)
        intelligence = (intelligence + intelligenceChange).coerceIn(0, 100)
        social = (social + socialChange).coerceIn(0, 100)
        money = (money + moneyChange).coerceAtLeast(0)
    }
}