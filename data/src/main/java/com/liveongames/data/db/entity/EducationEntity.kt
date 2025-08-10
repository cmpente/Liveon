// data/db/entity/EducationEntity.kt
package com.liveongames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.liveongames.domain.model.Education
import com.liveongames.domain.model.EducationLevel

@Entity(tableName = "educations")
data class EducationEntity(
    @PrimaryKey
    val id: String,
    val characterId: String,
    val name: String,
    val description: String,
    val level: String,
    val cost: Int,
    val duration: Int,
    val requiredGPA: Double,
    val completionDate: Long?,
    val isActive: Boolean,
    val timestamp: Long,
    val currentGPA: Double // Track student's performance
)

fun EducationEntity.toEducation(): Education {
    return Education(
        id = this.id,
        name = this.name,
        description = this.description,
        level = EducationLevel.valueOf(this.level),
        cost = this.cost,
        duration = this.duration,
        requiredGPA = this.requiredGPA,
        completionDate = this.completionDate,
        isActive = this.isActive,
        timestamp = this.timestamp,
        currentGPA = this.currentGPA
    )
}

fun Education.toEntity(characterId: String): EducationEntity {
    return EducationEntity(
        id = this.id,
        characterId = characterId,
        name = this.name,
        description = this.description,
        level = this.level.name,
        cost = this.cost,
        duration = this.duration,
        requiredGPA = this.requiredGPA,
        completionDate = this.completionDate,
        isActive = this.isActive,
        timestamp = this.timestamp,
        currentGPA = this.currentGPA
    )
}