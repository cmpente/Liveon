// app/src/main/java/com/altlifegames/altlife/model/CharacterData.kt
package com.altlifegames.altlife.model

data class CharacterData(
    val firstName: String = "Alex",
    val lastName: String = "Johnson",
    val gender: String = "Male",
    val bodyType: Int = 1, // 0: Slim, 1: Average, 2: Athletic, 3: Large
    val skinTone: Int = 1, // 0-3 different tones
    val hairStyle: Int = 0, // 0-3 different styles
    val hairColor: Int = 0, // 0-3 different colors
    val eyeShape: Int = 0, // 0-3 different shapes
    val eyeColor: Int = 0, // 0-3 different colors
    val outfit: Int = 0 // 0-3 different outfits
) {
    companion object {
        fun random(): CharacterData {
            val firstNames = listOf("Alex", "Jordan", "Taylor", "Casey", "Riley", "Morgan", "Cameron", "Quinn", "Drew", "Skyler")
            val lastNames = listOf("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez")
            val genders = listOf("Male", "Female", "Other")
            
            return CharacterData(
                firstName = firstNames.random(),
                lastName = lastNames.random(),
                gender = genders.random(),
                bodyType = (0..3).random(),
                skinTone = (0..3).random(),
                hairStyle = (0..3).random(),
                hairColor = (0..3).random(),
                eyeShape = (0..3).random(),
                eyeColor = (0..3).random(),
                outfit = (0..3).random()
            )
        }
    }
}