package com.altlifegames.domain.model

data class Game(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val enjoymentValue: Int
)