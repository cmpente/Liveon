package com.altlifegames.domain.model

data class Save(
    val id: String,
    val name: String,
    val createdDate: Long,
    val lastPlayed: Long,
    val characterData: String // Serialized character data
)