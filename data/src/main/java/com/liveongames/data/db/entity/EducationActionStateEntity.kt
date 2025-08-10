package com.liveongames.data.db.entity

import androidx.room.Entity

@Entity(
    tableName = "education_action_state",
    primaryKeys = ["characterId", "educationId", "actionId"]
)
data class EducationActionStateEntity(
    val characterId: String,
    val educationId: String,
    val actionId: String,
    val lastUsedAt: Long = 0L,
    val usedThisAge: Int = 0
)
