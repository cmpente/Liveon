package com.altlifegames.domain.model

data class Education(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val duration: Int,
    val minimumAge: Int,
    val skillIncrease: Int
)