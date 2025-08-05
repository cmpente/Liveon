package com.liveongames.domain.model

data class Job(
    val id: String,
    val title: String,
    val company: String,
    val baseSalary: Int,
    val level: Int,
    val educationRequirement: String,
    val iconRes: Int,
    val description: String,
    val maxSalary: Int? = null
)