package com.liveongames.domain.model

data class Pet(
    val id: String,
    val name: String,
    val type: String,
    val happiness: Int,
    val cost: Int
)