package com.liveongames.liveon.assets.events

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liveongames.domain.model.GameEvent
import com.liveongames.liveon.assets.common.RawAssetReader

class EventsAssetLoader(
    private val reader: RawAssetReader,
    private val gson: Gson
) {
    companion object {
        private const val EVENTS_FILE = "events.json"
        private const val CHILD_UNIQUE_FILE = "events_childhood_unique.json"
    }

    fun loadEvents(): List<GameEvent> {
        val type = object : TypeToken<List<GameEvent>>() {}.type
        return reader.loadArray(EVENTS_FILE, gson, type)
    }

    fun loadChildhoodUniqueEvents(): List<GameEvent> {
        val type = object : TypeToken<List<GameEvent>>() {}.type
        return reader.loadArray(CHILD_UNIQUE_FILE, gson, type)
    }
}
