package com.liveongames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.liveongames.domain.model.Education
import com.liveongames.domain.model.EducationLevel

@Entity(tableName = "educations")
data class EducationEntity(
    @PrimaryKey val id: String,
    val characterId: String,
    val name: String,
    val description: String,
    val level: String,
    val cost: Int,
    val durationMonths: Int,
    val requiredGpa: Double,
    val currentGpa: Double = 0.0,
    val isActive: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val completionDate: Long? = null,
    val attendClassCount: Int = 0,
    val doHomeworkCount: Int = 0,
    val studyCount: Int = 0
)

fun EducationEntity.toDomain(): Education = Education(
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
