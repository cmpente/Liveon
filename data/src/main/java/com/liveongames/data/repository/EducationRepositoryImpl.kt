// data/src/main/java/com/liveongames/data/repository/EducationRepositoryImpl.kt
package com.liveongames.data.repository

import android.util.Log
import com.liveongames.data.db.dao.EducationActionStateDao
import com.liveongames.data.db.dao.EducationDao
import com.liveongames.data.db.entity.EducationEntity
import com.liveongames.domain.model.*
import com.liveongames.domain.repository.EducationRepository
import com.liveongames.data.assets.education.EducationAssetLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

import com.liveongames.data.model.education.EducationCourse
import com.liveongames.data.model.education.EducationActionDef

class EducationRepositoryImpl @Inject constructor(
    private val educationDao: EducationDao,
    private val actionStateDao: EducationActionStateDao,
    private val assetLoader: EducationAssetLoader
) : EducationRepository {

    companion object {
        private const val PLAYER = "player_character"
        private const val TAG = "EducationRepoImpl"
    }

    // === SCHEMA-BASED EDUCATION METHODS ===

    override suspend fun getPrograms(): List<EducationProgram> = withContext(Dispatchers.IO) {
        assetLoader.loadCourses().map { it }
    }

    override suspend fun getActions(): List<ActionDef> = withContext(Dispatchers.IO) {
        assetLoader.loadActions().map { it }
    }

    override suspend fun getEnrollment(): Enrollment? = withContext(Dispatchers.IO) {
        val activeEntity = educationDao.getForCharacter(PLAYER).firstOrNull { it.isActive }
        return@withContext activeEntity?.toEnrollmentFull(getPrograms())
    }

    override suspend fun enroll(programId: String): Enrollment {
        // 1. Check if already enrolled
        if (getEnrollment() != null) {
            throw IllegalStateException("Cannot enroll: Player is already enrolled in a program.")
        }

        // 2. Validate Program
        val course = getPrograms().firstOrNull { it.id == programId }
            ?: throw IllegalArgumentException("Program ID '$programId' not found in available programs.")

        // 3. Deactivate any previous enrollment for the player (shouldn't be any due to the check above, but as a safeguard)
        educationDao.deactivateAll(PLAYER)

        // 3. Create the new Enrollment domain object
        val newEnrollment = Enrollment(
            programId = course.id,
            tier = course.tier,
            schema = course.schema,
            progressPct = 0,
            gpa = 1.0, // Starting GPA
            startedAge = 18, // Placeholder, link to player state
            repeats = 0,
            lastActionAt = null
        )

        // 4. Save to persistence
        // Map to the entity for saving. You'll need to ensure the fields match your DB schema.
        // This is a simplified example; adapt to your entity's constructor/fields.
        val entityToSave = EducationEntity(
            id = newEnrollment.programId,
            characterId = PLAYER,
            name = course.title,
            description = course.description,
            level = course.tier.name, // Map enum to string
            cost = course.tuition,
            durationMonths = course.schema.totalPeriods, // Or a dedicated field if you have it in the entity
            requiredGpa = course.minGpa,
            currentGpa = newEnrollment.gpa,
            progressPct = newEnrollment.progressPct,
            isActive = true,
            timestamp = System.currentTimeMillis(), // Or use lastActionAt if set
            completionDate = null, // Not completed yet
            // Map counts if needed, or leave as 0/false for a new enrollment
            attendClassCount = 0,
            doHomeworkCount = 0,
            studyCount = 0
        )
        // 5. Persist the new entity
        educationDao.upsert(entityToSave)

        return newEnrollment
    }

    private suspend fun findChoice(actionId: String, choiceId: String): DialogChoice? {
        val action = getActions().firstOrNull { it.id == actionId } ?: return null
        return action.dialog.flatMap { it.choices }.firstOrNull { it.id == choiceId }
    }

    private fun applyChoiceEffects(
        enrollment: Enrollment,
        actionDef: ActionDef,
        choice: DialogChoice,
        multiplier: Double
    ): Enrollment {
        // 1. Calculate base GPA change from the choice
        val baseGpaDelta = kotlin.random.Random.nextDouble(choice.effects.gpaMin, choice.effects.gpaMax)

        // 2. Check for and apply risk penalty
        val isRiskTriggered = choice.effects.riskProb > 0.0 && kotlin.random.Random.nextDouble() < choice.effects.riskProb
        val riskPenaltyGpa = if (isRiskTriggered) choice.effects.riskPenaltyGpa else 0.0
        val gpaAfterRisk = (enrollment.gpa - riskPenaltyGpa).coerceIn(0.0, 4.0)

        // 3. Apply final GPA change with multiplier, clamping to 0.0-4.0
        val newGpa = (gpaAfterRisk + baseGpaDelta * multiplier).coerceIn(0.0, 4.0)

        // 4. Calculate progress increase and apply, clamping to 0-100
        val newProgress = (enrollment.progressPct + actionDef.baseProgress + choice.effects.progress).coerceIn(0, 100)

        // 5. Update the timestamp for action cooldowns
        val newLastActionAt = System.currentTimeMillis()

        // 6. Return the updated Enrollment object
        return enrollment.copy(
            gpa = newGpa,
            progressPct = newProgress,
            lastActionAt = newLastActionAt
        )
    }

    // --- FIXED: Return the correct EducationActionResult data class ---
    override suspend fun applyAction(
        actionId: String,
        choiceId: String,
        miniGameMultiplier: Double
    ): EducationActionResult = withContext(Dispatchers.IO) {
        try {
            // 1. Get Current Active Enrollment
            val enrollment = getEnrollment() ?: run {
                // Return Error state inside the result object is suboptimal, but since
                // the interface isn't a sealed class, we can't throw. Returning a default
                // or generic enrollment state with error flag in UI layer is an option,
                // or just log and return a stable error state. For simplicity, we'll get
                // the most common case working.
                // Let's revisit logic directly here:
                throw IllegalStateException("Cannot perform action: No active program enrolled.")
            }

            // 2. Validate and Find Action Definition
            val actionDef = getActions().firstOrNull { it.id == actionId }
                ?: throw IllegalArgumentException("Action definition for '$actionId' not found.")

            // 3. Validate and Find Selected Choice within the Action
            val selectedChoice = findChoice(actionId, choiceId)
                ?: throw IllegalArgumentException("Choice '$choiceId' not found for action '$actionId'.")

            // 4. Apply the Effects of the Choice (calculate new state)
            val updatedEnrollmentIntermediate = applyChoiceEffects(enrollment, actionDef, selectedChoice, miniGameMultiplier)

            // 5. Get program data for checks (like graduation requirements)
            val course = getPrograms().firstOrNull { it.id == updatedEnrollmentIntermediate.programId }
                ?: throw IllegalStateException("Associated course data for program '${updatedEnrollmentIntermediate.programId}' is missing.")

            // 6. Finalize State Updates (ensure progress is capped at 100)
            val finalEnrollment = updatedEnrollmentIntermediate.copy(progressPct = updatedEnrollmentIntermediate.progressPct.coerceAtMost(100))

            // 7. Persist the Updated State
            val entityToUpdate = educationDao.getById(PLAYER, finalEnrollment.programId)
            if (entityToUpdate != null) {
                // Update the relevant fields in the entity from the domain object
                val updatedEntity = entityToUpdate.copy(
                    currentGpa = finalEnrollment.gpa,
                    progressPct = finalEnrollment.progressPct,
                    timestamp = finalEnrollment.lastActionAt ?: System.currentTimeMillis() // Use the action timestamp
                )
                // Persist the changes
                educationDao.upsert(updatedEntity)
            } else {
                // This case is unexpected if enrollments are persisted correctly
                Log.w(TAG, "Entity for program ${finalEnrollment.programId} not found during state persistence.")
            }

            // 8. Determine Final Outcome based on new state
            // Check if the program duration is complete
            val isComplete = finalEnrollment.progressPct >= 100
            val meetsGpa = finalEnrollment.gpa >= course.minGpa
            val tierRequiresDecision = finalEnrollment.tier.ordinal >= EduTier.HIGH.ordinal // HS, CERT, etc.

            // --- Construct the correct, unified EducationActionResult ---
            EducationActionResult(
                enrollment = finalEnrollment,
                graduated = isComplete && meetsGpa,
                // Indicate that a decision is required for HS+ if complete but GPA is not met
                decisionRequired = isComplete && !meetsGpa && tierRequiresDecision,
                graduationEligible = isComplete && meetsGpa // Optional helper for UI
            )

        } catch (e: Exception) {
            // Catch any unexpected errors during the process and report them
            Log.e(TAG, "Unexpected error applying action '$actionId'", e)
            // We must return an ActionResult, so we create one for the UI VM to handle generically.
            // A best-effort attempt would be to return the original enrollment, or a null/default one.
            // Throwing might also be acceptable if UI layer traps exceptions from repo calls.
            // For robustness returning a result is better. Let's pass a copy of the original enrollment.
            // If getEnrollment() throws/crashes again in catch block, that's a deeper problem.
            getEnrollment()?.let { safeEnrollment ->
                EducationActionResult(
                    enrollment = safeEnrollment,
                    graduated = false,
                    decisionRequired = false,
                    graduationEligible = false
                )
            } ?: run {
                // If we can't even retrieve the current state, default/error
                Log.wtf(TAG, "Fatal error getting enrollment state inside applyAction error handler.", e)
                // Fallback to a neutral Enrollment object. In practice, the VM/UI would need
                // grace handling for such a failure path.
                EducationActionResult(
                    enrollment = Enrollment(
                        programId = "error_default",
                        tier = EduTier.ELEMENTARY, // Placeholder defaults
                        schema = AcademicSchema("Error", 1, 1, null),
                        progressPct = 0,
                        gpa = 0.0,
                        startedAge = 0,
                        repeats = 0,
                        lastActionAt = null
                    ),
                    graduated = false,
                    decisionRequired = false,
                    graduationEligible = false
                )
            }

        }
    }

    override suspend fun retakeProgram(programId: String) = withContext(Dispatchers.IO) {
        val entityToRetake = educationDao.getById(PLAYER, programId)
        if (entityToRetake != null) {
            // Reset progress and GPA for retake
            val retakeEntity = entityToRetake.copy(
                currentGpa = 1.0, // Reset GPA
                progressPct = 0, // Reset progress
                timestamp = System.currentTimeMillis(), // Update timestamp
                completionDate = null, // Clear completion date
                isActive = true // Ensure it's active for retake
            )
            educationDao.upsert(retakeEntity)
        } else {
            // This case is unexpected if we're trying to retake a non-existent or inactive program
            Log.w(TAG, "Entity for program $programId not found or not active during retake attempt.")
            }

        }
    }

    override suspend fun onAgeUp(): Enrollment? {
        // This method is intended to reset age-dependent action caps.
        // It should not alter the core state of the enrollment (like progress or GPA).
        val currentEnrollment = getEnrollment() ?: return null

        // Call the data layer function to reset the action state for this age/program
        resetActionCapsForAge(currentEnrollment.programId)

        // Return the current enrollment as its logical state hasn't changed
        return currentEnrollment
    }

    override suspend fun resetEducation() {
        // This clears the current active enrollment, effectively "unenrolling" the player.
        // It typically just deactivates the current enrollment record.
        educationDao.deactivateAll(PLAYER)
        // Note: If you have player-global action state that needs reset, do it here too.
        // actionStateDao.clearForPlayer(PLAYER) // Example if such a method exists
    }

    override suspend fun dropOut(characterId: String) = withContext(Dispatchers.IO) {
        educationDao.deactivateAll(characterId)
    }

    override suspend fun getCurrentTermState(): TermState? = withContext(Dispatchers.IO) {
        // This method derives the current term state from the active enrollment.
        val enrollment = getEnrollment() ?: return@withContext null

        val schema = enrollment.schema
        val progressPct = enrollment.progressPct

        // Calculate the current period using the helper function
        val currentPeriod = periodFromProgress(progressPct, schema.totalPeriods)

        // Calculate the current group (e.g. Year 2, Semester 3) using the helper function
        val currentGroup = groupFromPeriod(currentPeriod, schema.periodsPerYear)

        // Determine the label to use for the group (e.g., "Year", "Semester", or null if it's just periods)
        val groupLabel = schema.groupingLabel ?: schema.displayPeriodName

        // Create and return the TermState object with the calculated information
        return@withContext TermState(
            periodIndex = currentPeriod,
            groupLabel = groupLabel,
            groupNumber = currentGroup,
            progressPercent = progressPct
        )
    }

    // --- Internal Domain Logic Helpers ---
    // These functions implement the specified schema-driven period and group number calculation.

    /**
     * Calculates the current period index based on overall progress and the total number of periods.
     * @param progressPct The overall progress through the program (0-100).
     * @param totalPeriods The total number of display periods defined by the program's schema.
     * @return The 1-based index of the current period.
     */
    private fun periodFromProgress(progressPct: Int, totalPeriods: Int): Int =
        ((progressPct * totalPeriods + 99) / 100).coerceIn(1, totalPeriods)

    /**
     * Calculates the group number (e.g., Year 2, Semester 3) based on period index and periods per year.
     * @param period The 1-based index of the current period.
     * @param periodsPerYear The number of display periods that make up one group (e.g., 2 for semesters, 4 for quarters).
     * @return The 1-based number of the current group.
     */
    private fun groupFromPeriod(period: Int, periodsPerYear: Int): Int =
        ((period - 1) / periodsPerYear) + 1

    /**
     * Resets the age-based usage counter for actions related to a specific education program.
     * This method interacts with the `EducationActionStateDao` to perform the reset.
     * @param educationId The ID of the education program for which to reset action caps.
     */
    private suspend fun resetActionCapsForAge(educationId: String) {
        // It resets the usage count for actions tied to this specific education and player's age.
        actionStateDao.resetUsedThisAge(PLAYER, educationId)
        // If actions have global player-based age caps (not tied to a program), reset them too.
        // actionStateDao.resetUsedThisAgeForPlayer(PLAYER) // Example signature
    }
}

// === EXTENSIONS FOR ENTITY MAPPING ===
// This extension maps the legacy EducationEntity to the new Enrollment domain model.
// It requires the full list of programs to access the complete schema and static data.

/**
 * Maps an EducationEntity from the database to the new Enrollment domain model.
 * @param courses The complete list of available EducationPrograms to find the schema and details.
 * @return An Enrollment object if the entity is active and its course definition is found; null otherwise.
 */
fun EducationEntity.toEnrollmentFull(courses: List<EducationProgram>): Enrollment? {
    // Only active entities should be considered as representing an active enrollment
    if (!this.isActive) return null

    // Find the corresponding course definition from the loaded programs list
    val course = courses.firstOrNull { it.id == this.id }
    if (course == null) {
        // Log a warning if an active enrollment entity exists but its corresponding course is missing
        Log.w("EducationRepoImpl", "Active enrollment entity for program '${this.id}' found, but no corresponding course definition exists. Cannot map to domain model.")
        return null // Cannot create a full Enrollment without its definition
    }

    // Construct the domain Enrollment object using data from both the entity and the course definition
    return Enrollment(
        programId = this.id,
        tier = course.tier, // Get tier from the program definition
        schema = course.schema, // Get full schema from the program definition
        progressPct = this.progressPct.coerceIn(0, 100), // Ensure value is within valid range, sourced from DB
        gpa = this.currentGpa.coerceIn(0.0, 4.0), // Ensure value is within valid range, sourced from DB
        startedAge = 18, // TODO: This needs to come from the player's state that started this education
        repeats = 0, // TODO: This needs to be tracked and loaded, potentially from the entity or a separate state
        lastActionAt = if (this.timestamp > 0L) this.timestamp else null // Use the entity's timestamp for cooldowns, return null for invalid times
    )
}


