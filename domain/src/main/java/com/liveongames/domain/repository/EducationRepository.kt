// domain/src/main/java/com/liveongames/domain/repository/EducationRepository.kt
package com.liveongames.domain.repository

import com.liveongames.domain.model.*
import kotlinx.coroutines.flow.Flow

interface EducationRepository {

    // === SCHEMA-BASED EDUCATION CONTRACTS ===
    // Use the new, specific result type
    suspend fun getEnrollment(): Enrollment?
    suspend fun getPrograms(): List<EducationProgram>
    suspend fun getActions(): List<ActionDef>
    suspend fun enroll(programId: String): Enrollment
    // >>> CHANGE THE RETURN TYPE HERE <<<
    suspend fun applyAction(actionId: String, choiceId: String, miniGameMultiplier: Double = 1.0): EducationActionResult
    suspend fun onAgeUp(): Enrollment?
    suspend fun resetEducation()
    suspend fun getCurrentTermState(): TermState?
}