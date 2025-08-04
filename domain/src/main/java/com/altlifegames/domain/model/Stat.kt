package com.altlifegames.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Stat(
    val current: Int = 0,
    val max: Int = 100
)