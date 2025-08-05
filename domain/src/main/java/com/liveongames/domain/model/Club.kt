package com.liveongames.domain.model

data class Club(
    val id: String,
    val name: String,
    val description: String,
    val membershipFee: Int,
    val benefits: Map<String, Int> = emptyMap(),
    val minimumAge: Int = 0,
    val minimumSocialRequirement: Int = 0
)