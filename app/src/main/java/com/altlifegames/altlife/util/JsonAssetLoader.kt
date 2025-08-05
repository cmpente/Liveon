// app/src/main/java/com/altlifegames/util/JsonAssetLoader.kt
package com.altlifegames.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.altlifegames.domain.model.GameEvent
import java.io.InputStream
import java.io.InputStreamReader

class JsonAssetLoader(private val context: Context) {

    private val gson = Gson()

    fun loadEvents(): List<GameEvent> {
        return try {
            val inputStream: InputStream = context.assets.open("events.json")
            val reader = InputStreamReader(inputStream)
            val listType = object : TypeToken<List<GameEvent>>() {}.type
            gson.fromJson(reader, listType)
        } catch (e: Exception) {
            e.printStackTrace()  // ✅ Add error logging
            emptyList()
        }
    }
}