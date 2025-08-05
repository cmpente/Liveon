package com.liveongames.data.db

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromStringIntMap(value: Map<String, Int>): String {
        return value.entries.joinToString(";") { "${it.key}=${it.value}" }
    }

    @TypeConverter
    fun toStringIntMap(value: String): Map<String, Int> {
        if (value.isEmpty()) return emptyMap()
        return value.split(";").associate {
            val (key, value) = it.split("=")
            key to value.toInt()
        }
    }
}