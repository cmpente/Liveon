// data/src/main/java/com/liveongames/data/db/entity/EducationEntity.kt
package com.liveongames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an instance of education progress for a character.
 * This maps to the 'educations' table in the local Room database.
 *
 * !! IMPORTANT !! Ensure all fields used in `copy()` calls or accessed directly
 * (like entity.id, entity.name) are constructor properties (`val` or `var`)
 * defined in the primary constructor.
 */
@Entity(tableName = "educations")
data class EducationEntity(
    @PrimaryKey
    val id: String,           // programId related to EducationProgram.id
    val characterId: String,  // identifies which character this belongs to

    // --- Basic Metadata ---
    val name: String,
    val description: String,
    val level: String,        // Stored as a string value (to avoid enum issues)

    // --- Costs & Timing ---
    val cost: Int,
    val durationMonths: Int,   // overall program duration in months

    // --- Academic Requirements & Progress ---
    val requiredGpa: Double,  // Static requirement from catalog
    val currentGpa: Double = 0.0, // Current GPA earned by player (0.0 - 4.0)
    val progressPct: Int = 0, // Current progress (%) through the program

    // --- Status ---
    val isActive: Boolean = false,

    // --- Timestamps ---
    val timestamp: Long = System.currentTimeMillis(), // Last activity time/action timestamp
    val completionDate: Long? = null,                   // If set, indicates graduation

    // --- Legacy Class/Action Counts ---
    val attendClassCount: Int = 0,
    val doHomeworkCount: Int = 0,
    val studyCount: Int = 0
)