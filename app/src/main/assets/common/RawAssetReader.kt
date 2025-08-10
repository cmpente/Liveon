package com.liveongames.liveon.assets.common

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class RawAssetReader(private val context: Context) {

    fun loadJsonArray(assetFileName: String): JsonArray = try {
        context.assets.open(assetFileName).use { input ->
            InputStreamReader(input).use { reader ->
                val root: JsonElement = JsonParser.parseReader(reader)
                if (root.isJsonArray) root.asJsonArray else JsonArray()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace(); JsonArray()
    }

    inline fun <reified T> loadArray(assetFileName: String, gson: com.google.gson.Gson): List<T> = try {
        context.assets.open(assetFileName).use { input ->
            InputStreamReader(input).use { reader ->
                val type = object : TypeToken<List<T>>() {}.type
                gson.fromJson(reader, type)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace(); emptyList()
    }
}
