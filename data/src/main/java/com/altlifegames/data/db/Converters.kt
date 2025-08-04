package com.altlifegames.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

class Converters {
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Json.encodeToString(ListSerializer(String.serializer()), value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            Json.decodeFromString(ListSerializer(String.serializer()), value)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    @TypeConverter
    fun fromStringMap(value: Map<String, Int>): String {
        return Json.encodeToString(MapSerializer(String.serializer(), Int.serializer()), value)
    }
    
    @TypeConverter
    fun toStringMap(value: String): Map<String, Int> {
        return try {
            Json.decodeFromString(MapSerializer(String.serializer(), Int.serializer()), value)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}