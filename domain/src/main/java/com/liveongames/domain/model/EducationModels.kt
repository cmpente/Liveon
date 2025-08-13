// domain/src/main/java/com/liveongames/domain/model/EducationModels.kt
package com.liveongames.domain.model

import kotlinx.serialization.Serializable

// === Enums ===

// Existing enum - keep for legacy/database/interop
enum class EducationLevel(val displayName: String) {
    BASIC("Basic Education"),
    HIGH_SCHOOL("High School"),
    ASSOCIATE("Associate Degree"),
    BACHELOR("Bachelor's Degree"),
    MASTER("Master's Degree"),
    DOCTORATE("Doctorate"),
    CERTIFICATION("Certification");

    // Maps old EducationLevels to the new canonical EduTier
    // Fixed: Made exhaustive with 'else' to satisfy compiler in all contexts
    fun toEduTier(): EduTier {
        return when (this) {
            BASIC -> EduTier.ELEMENTARY // or MIDDLE, depending on your game's definition. Let's go with ELEMENTARY.
            HIGH_SCHOOL -> EduTier.HIGH
            ASSOCIATE -> EduTier.ASSOC
            BACHELOR -> EduTier.BACH
            MASTER -> EduTier.MAST
            DOCTORATE -> EduTier.PHD
            CERTIFICATION -> EduTier.CERT
            // The 'else' branch makes this exhaustive even if the enum changes in the future
            // (e.g., if a new value is added to EducationLevel).
            // If you want explicit coverage, list all 7, but 'else' is idiomatic for enums in mappings.
            // else -> EduTier.ELEMENTARY // Fallback; not strictly necessary if all cases are listed.
        }
    }
}

// New canonical enum - used throughout the new schema logic
enum class EduTier {
    ELEMENTARY, MIDDLE, HIGH, CERT, ASSOC, BACH, MAST, PHD;

    // Maps new EduTier back to the old EducationLevel for interop
    // Fixed: Made exhaustive to satisfy compiler
    fun toEducationLevel(): EducationLevel {
        return when (this) {
            ELEMENTARY -> EducationLevel.BASIC
            MIDDLE -> EducationLevel.BASIC
            HIGH -> EducationLevel.HIGH_SCHOOL
            CERT -> EducationLevel.CERTIFICATION
            ASSOC -> EducationLevel.ASSOCIATE
            BACH -> EducationLevel.BACHELOR
            MAST -> EducationLevel.MASTER
            PHD -> EducationLevel.DOCTORATE
            // Exhaustiveness: Kotlin sealed types/enums require all branches if used in an expression context
            // that requires it. Adding 'else' or listing all is safe; 'else' isn't needed here as we list all.
        }
    }
}


// === Schema & Helpers ===

data class AcademicSchema(
    val displayPeriodName: String, // "Semester" | "Quarter" | "Month" | "Milestone"
    val periodsPerYear: Int,       // e.g., 2 for semesters, 4 for quarters
    val totalPeriods: Int,         // overall length in display periods
    val groupingLabel: String? = "Year" // null or e.g., "Year" to show Year 1..4
)

fun periodFromProgress(progressPct: Int, totalPeriods: Int): Int =
    ((progressPct * totalPeriods + 99) / 100).coerceIn(1, totalPeriods)

fun groupFromPeriod(period: Int, periodsPerYear: Int): Int =
    ((period - 1) / periodsPerYear) + 1


// === Domain Interfaces for Catalog Items ===

// Represents a program available for enrollment (e.g., High School, Bachelor's).
interface EducationProgram {
    val id: String
    val title: String
    val description: String
    val tier: EduTier
    val schema: AcademicSchema
    val minGpa: Double
    val tuition: Int
    val requirements: Set<String>
}

// === Domain Data Classes for Runtime State ===

// Represents the player's current state within a program.
data class Enrollment(
    val programId: String,
    val tier: EduTier,
    val schema: AcademicSchema, // Cached for UI/logic speed
    val progressPct: Int,       // 0..100
    val gpa: Double,            // 0.00..4.00
    val startedAge: Int,
    val repeats: Int = 0,       // how many times the program has been repeated
    val lastActionAt: Long? = null // for cooldowns
)

// Represents the current global state of academic terms/years progression.
data class TermState(
    val periodIndex: Int,       // Sequential index (e.g., 1..8 for 4 years of semesters)
    val groupLabel: String?,    // e.g., "Year" or "Semester"
    val groupNumber: Int,       // e.g., Year 1, Semester 3
    val progressPercent: Int    // % through the current period
)


// === Action Definitions and Results ===

// Schema-Driven Study Action Definitions
interface ActionDef {
    val id: String
    val title: String
    val tiers: List<EduTier>
    val minGpa: Double?
    val maxGpa: Double?
    val gpaDeltaMin: Double
    val gpaDeltaMax: Double
    val cooldownMinutes: Int
    val baseProgress: Int
    val dialog: List<DialogStep>
}

@Serializable
data class DialogStep(
    val id: String,
    val text: String,
    val choices: List<DialogChoice>
)

@Serializable
data class DialogChoice(
    val id: String,
    val label: String,
    val effects: ChoiceEffects
)

// The mechanical impact of choosing a DialogChoice.
@Serializable
data class ChoiceEffects(
    val gpaMin: Double,          // Minimum potential GPA increase/decrease
    val gpaMax: Double,          // Maximum potential GPA increase/decrease
    val progress: Int,           // Additional progress points gained
    val riskProb: Double = 0.0,      // Probability of a risk event (0.0 to 1.0)
    val riskPenaltyGpa: Double = 0.0 // GPA penalty applied if risk occurs
)

// === Unified Domain Action Result ===
// This is the standard result returned by EducationRepository's action-modifying functions.
data class EducationActionResult(
    val enrollment: Enrollment,      // The updated enrollment state.
    val graduated: Boolean = false,  // True if graduation criteria are met.
    val failed: Boolean = false,     // True if failure (for HS+) needs UI decision.
    val graduationEligible: Boolean = false // Optional helper for UI prompts before final graduation.

    // Optional compatibility layer for legacy callers or detailed feedback
    // val message: String? = null
    // val gpaDelta: Double = 0.0
    // val progressDelta: Int = 0
) {
    // Compatibility helper method if old code expects a getter-like access
    fun getNewEnrollment(): Enrollment = this.enrollment
}