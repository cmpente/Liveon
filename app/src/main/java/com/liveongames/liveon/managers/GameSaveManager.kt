// app/src/main/java/com/liveongames/liveon/managers/GameSaveManager.kt
package com.liveongames.liveon.managers

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.liveongames.liveon.models.GameSave
import com.liveongames.liveon.models.PlayerSaveData
import com.liveongames.liveon.models.SaveFileInfo
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class GameSaveManager(private val context: Context) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private fun getSaveDirectory(): File {
        val dir = File(context.filesDir, "saves")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    companion object {
        private const val TAG = "GameSaveManager"
        private const val SAVE_FILE_EXTENSION = ".save"
    }

    // Save game state
    fun saveGame(saveName: String, playerData: PlayerSaveData): Boolean {
        return try {
            val saveData = GameSave(
                saveName = saveName,
                playerData = playerData
            )

            val saveFile = File(getSaveDirectory(), "$saveName$SAVE_FILE_EXTENSION")
            val json = gson.toJson(saveData)

            saveFile.writeText(json)

            Log.d(TAG, "Game saved successfully: $saveName")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save game", e)
            false
        }
    }

    // Load game state
    fun loadGame(saveName: String): GameSave? {
        return try {
            val saveFile = File(getSaveDirectory(), "$saveName$SAVE_FILE_EXTENSION")
            if (!saveFile.exists()) return null

            val json = saveFile.readText()
            val type = object : TypeToken<GameSave>() {}.type
            val saveData = gson.fromJson<GameSave>(json, type)

            Log.d(TAG, "Game loaded successfully: $saveName")
            saveData
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load game", e)
            null
        }
    }

    // Get list of all save files
    fun getSaveFiles(): List<SaveFileInfo> {
        return try {
            val files = getSaveDirectory().listFiles { file ->
                file.extension == "save"
            } ?: return emptyList()

            files.mapNotNull { file ->
                try {
                    val saveName = file.nameWithoutExtension
                    val saveData = loadGame(saveName)
                    saveData?.let {
                        SaveFileInfo(
                            name = saveName,
                            saveDate = it.createdAt,
                            playerName = it.playerData.playerName,
                            playerAge = it.playerData.age,
                            lastPlayed = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                                .format(Date(it.createdAt))
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading save file: ${file.name}", e)
                    null
                }
            }.sortedByDescending { it.saveDate }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get save files", e)
            emptyList()
        }
    }

    // Delete a save file
    fun deleteSave(saveName: String): Boolean {
        return try {
            val saveFile = File(getSaveDirectory(), "$saveName$SAVE_FILE_EXTENSION")
            val result = saveFile.delete()
            Log.d(TAG, "Save file ${if (result) "deleted" else "not found"}: $saveName")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete save file: $saveName", e)
            false
        }
    }

    // Auto-save functionality
    fun autoSave(playerData: PlayerSaveData): Boolean {
        val autoSaveName = "autosave"
        return saveGame(autoSaveName, playerData)
    }

    // Load auto-save
    fun loadAutoSave(): GameSave? {
        return loadGame("autosave")
    }
}