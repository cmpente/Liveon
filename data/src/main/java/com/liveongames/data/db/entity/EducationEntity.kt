package com.liveongames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.liveongames.domain.model.Education
import com.liveongames.domain.model.EducationLevel

/**
 * One enrollment/track for the player. We store per-track GPA and action counters.
 * Diminishing returns counters are reset by the ViewModel on age-up (and can also be reset here if desired).
 */
@Entity(tableName = "educations")
data class EducationEntity(
    @PrimaryKey val id: String,           // courseId (stable, from JSON)
    val characterId: String,              // "player_character" (for now)
    val name: String,
    val description: String,
    val level: String,                    // enum name of EducationLevel
    val cost: Int,
    val durationMonths: Int,
    val requiredGpa: Double,
    val currentGpa: Double = 0.0,         // 0.0 .. 4.0
    val isActive: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val completionDate: Long? = null,     // null if not completed
    // diminishing-returns counters for the current age period
    val attendClassCount: Int = 0,
    val doHomeworkCount: Int = 0,
    val studyCount: Int = 0
)

fun EducationEntity.toEducation(): Education = Education(
    id = id,
    name = name,
    description = description,
    level = EducationLevel.valueOf(level),
    cost = cost,
    duration = durationMonths,
    requiredGPA = requiredGpa,
    currentGPA = currentGpa,
    isActive = isActive,
    timestamp = timestamp,
    completionDate = completionDate,
    attendClassCount = attendClassCount,
    doHomeworkCount = doHomeworkCount,
    studyCount = studyCount
)

fun Education.toEntity(characterId: String): EducationEntity = EducationEntity(
    id = id,
    characterId = characterId,
    name = name,
    description = description,
    level = level.name,
    cost = cost,
    durationMonths = duration,
    requiredGpa = requiredGPA,
    currentGpa = currentGPA,
    isActive = isActive,
    timestamp = timestamp,
    completionDate = completionDate,
    attendClassCount = attendClassCount,
    doHomeworkCount = doHomeworkCount,
    studyCount = studyCount
)
