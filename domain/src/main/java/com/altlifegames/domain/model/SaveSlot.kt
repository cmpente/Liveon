package com.altlifegames.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SaveSlot(
    val id: Long = 0,
    val timestamp: Long = 0
    // Add other fields that match your domain requirements
)