package com.liveongames.data.assets.scenarios

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liveongames.data.assets.common.RawAssetReader
import com.liveongames.data.assets.scenarios.ScenarioAsset // assuming exists

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
