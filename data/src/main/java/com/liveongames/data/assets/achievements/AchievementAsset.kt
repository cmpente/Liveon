package com.liveongames.data.assets.achievements

data class AchievementAsset(
    val id: String,
    val title: String,
    val description: String? = null,
    val icon: String? = null,
    val hidden: Boolean? = null,
    val criteria: Map<String, Any?>? = null,
    val points: Int? = null
)
