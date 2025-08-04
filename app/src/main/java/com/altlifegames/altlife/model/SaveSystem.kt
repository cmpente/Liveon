package com.altlifegames.altlife.model

import android.content.Context
import com.google.gson.Gson
import java.io.*

data class SaveGame(
    val player: Player,
    val skills: Map<String, Skill>,
    val investments: List<Pair<String, Int>>,
    val properties: List<Property>,
    val quests: List<Quest>,
    val currentLocation: String,
    val personality: Personality,
    val saveDate: Long = System.currentTimeMillis()
)

class SaveSystem {
    companion object {
        private const val SAVE_FILE = "savegame.json"
        private val gson = Gson()
        
        fun saveGame(context: Context, saveData: SaveGame): Boolean {
            return try {
                val jsonString = gson.toJson(saveData)
                val file = File(context.filesDir, SAVE_FILE)
                file.writeText(jsonString)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
        
        fun loadGame(context: Context): SaveGame? {
            return try {
                val file = File(context.filesDir, SAVE_FILE)
                if (file.exists()) {
                    val jsonString = file.readText()
                    gson.fromJson(jsonString, SaveGame::class.java)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        
        fun getSaveSlots(context: Context): List<SaveGame> {
            return listOfNotNull(loadGame(context))
        }
        
        fun deleteSave(context: Context): Boolean {
            return try {
                val file = File(context.filesDir, SAVE_FILE)
                file.delete()
            } catch (e: Exception) {
                false
            }
        }
    }
}