package com.liveongames.liveon.assets.scenarios

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liveongames.liveon.assets.common.RawAssetReader

class ScenariosAssetLoader(
    private val reader: RawAssetReader,
    private val gson: Gson
) {
    companion object { private const val SCENARIOS_FILE = "scenarios.json" }

    fun loadScenarios(): List<ScenarioAsset> {
        val type = object : TypeToken<List<ScenarioAsset>>() {}.type
        return reader.loadArray(SCENARIOS_FILE, gson, type)
    }
}
