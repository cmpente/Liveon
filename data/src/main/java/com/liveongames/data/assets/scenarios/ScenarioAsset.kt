package com.liveongames.data.assets.scenarios

data class ScenarioAsset(
    val id: String,
    val name: String,
    val description: String? = null,
    val conditions: Map<String, Any?>? = null,
    val rewards: Map<String, Any?>? = null,
    val tags: List<String>? = null
)
