// app/src/main/java/com/liveongames/liveon/models/SaveModels.kt
package com.liveongames.liveon.models

import com.liveongames.domain.model.education.Education
import java.util.*

data class PlayerSaveData(
    val playerId: String = UUID.randomUUID().toString(),
    val playerName: String = "Player",
    val age: Int = 0,
    val health: Int = 100,
    val happiness: Int = 50,
    val intelligence: Int = 20,
    val money: Int = 1000,
    val social: Int = 30,
    val gpa: Double = 0.0,
    val educations: List<Education> = listOf(),
    val completedEducations: List<Education> = listOf(),
    val activeEducationId: String? = null,
    val gameTime: Long = System.currentTimeMillis(),
    val saveDate: Long = System.currentTimeMillis()
)

data class GameSave(
    val saveId: String = UUID.randomUUID().toString(),
    val saveName: String = "Save",
    val playerData: PlayerSaveData,
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

data class SaveFileInfo(
    val name: String,
    val saveDate: Long,
    val playerName: String,
    val playerAge: Int,
    val lastPlayed: String
)