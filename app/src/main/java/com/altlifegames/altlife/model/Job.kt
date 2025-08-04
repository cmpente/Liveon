// app/src/main/java/com/altlifegames/altlife/model/Job.kt
package com.altlifegames.altlife.model

data class Job(
    val id: String,
    val title: String,
    val company: String,
    val baseSalary: Int,
    val level: Int, // 1-10 (1=entry, 10=executive)
    val educationRequirement: String,
    val iconRes: Int,
    val description: String,
    val maxSalary: Int? = null // For jobs with growth potential
)

enum class CareerFilter {
    ALL, 
    ENTRY, 
    MID, 
    SENIOR, 
    EXECUTIVE
}