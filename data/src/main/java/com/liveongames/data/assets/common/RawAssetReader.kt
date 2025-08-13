package com.liveongames.data.assets.common

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.InputStreamReader
import java.lang.reflect.Type

class RawAssetReader(@PublishedApi internal val context: Context) {

    fun loadJsonArray(assetFileName: String): JsonArray = try {
        context.assets.open(assetFileName).use { input ->
            InputStreamReader(input).use { reader ->
                val root: JsonElement = JsonParser.parseReader(reader)
                if (root.isJsonArray) root.asJsonArray else JsonArray()
            }
        }
    } catch (e: Exception) { e.printStackTrace(); JsonArray() }

    // Explicit Type to avoid inline/reified visibility issues
    fun <T> loadArray(assetFileName: String, gson: Gson, type: Type): List<T> = try {
        context.assets.open(assetFileName).use { input ->
            InputStreamReader(input).use { reader ->
                @Suppress("UNCHECKED_CAST")
                gson.fromJson<List<T>>(reader, type) ?: emptyList()
            }
        }
    } catch (e: Exception) { e.printStackTrace(); emptyList() }
}
