package com.liveongames.liveon.assets.achievements

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liveongames.liveon.assets.common.RawAssetReader
import com.liveongames.liveon.assets.scenarios.ScenariosAssetLoader

class AchievementsAssetLoader(
    private val reader: RawAssetReader,
    private val gson: Gson
) {
    companion object { private const val ACHIEVEMENTS_FILE = "achievements.json" }

    fun loadAchievements(): List<AchievementAsset> {
        val type = object : TypeToken<List<AchievementAsset>>() {}.type
        return reader.loadArray(ACHIEVEMENTS_FILE, gson, type)
    }
}
