// app/src/main/java/com/altlifegames/altlife/model/EducationOption.kt
package com.altlifegames.altlife.model

data class EducationOption(
    val id: String,
    val name: String,
    val minAge: Int,
    val maxAge: Int,
    val cost: Int,
    val description: String,
    val iconRes: Int,
    val yearsRequired: Int = 1,
    val gpaRequirement: Double = 0.0, // 0.0 means no requirement
    val prerequisites: List<String> = emptyList() // Required previous educations
)

enum class EducationFilter {
    ALL, 
    FREE, 
    PAID, 
    SHORT, 
    LONG
}