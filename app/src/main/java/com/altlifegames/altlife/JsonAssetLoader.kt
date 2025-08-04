// Updated JsonAssetLoader.kt
package com.altlifegames.altlifealpha.utils

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.altlifegames.altlifealpha.data.models.GameEvent

class JsonAssetLoader(private val context: Context) {
    
    private val gson = GsonBuilder()
        .registerTypeAdapter(GameEvent::class.java, FlexibleEventDeserializer())
        .create()
    
    fun loadEvents(fileName: String): List<GameEvent> {
        return try {
            val inputStream = context.assets.open(fileName)
            val reader = inputStream.reader()
            val listType = object : TypeToken<List<GameEvent>>() {}.type
            gson.fromJson(reader, listType)
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error loading $fileName: ${e.message}")
            emptyList()
        }
    }
}