package com.liveongames.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.liveongames.data.db.entity.TermStateEntity.Companion.TABLE_NAME

/**
 * Entity representing the state of the game term/period.
 * This is the database model for TermState.
 *
 * @param id The unique identifier for this term state instance. (Primary Key)
 *         (Consider if this needs to be a String like "current_term" or a simple Int key
 *          if you are storing multiple historical term states)
 * @param currentYear The player's current age/year in the game.
 * @param termProgress The overall progress within the current educational term/period (0-100).
 * @param activeEducationProgramId The ID of the currently active education program, if any (nullable).
 * @param lastActionTimestamp The timestamp of the last player action affecting the term/education (used for cooldowns).
 * @param isYearTransitionPending Flag to indicate if a year transition logic needs to be processed.
 */
@Entity(tableName = TABLE_NAME)
data class TermStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") // Consider if "id" as a string key or an auto-generated Int is better
    val id: String, // E.g., "current" or a UUID if storing history

    @ColumnInfo(name = "current_year")
    val currentYear: Int,

    @ColumnInfo(name = "term_progress")
    val termProgress: Int, // Clamped 0-100

    @ColumnInfo(name = "active_education_program_id", defaultValue = "NULL")
    val activeEducationProgramId: String?, // Nullable if not enrolled

    @ColumnInfo(name = "last_action_timestamp")
    val lastActionTimestamp: Long?, // Nullable if no action taken yet

    @ColumnInfo(name = "is_year_transition_pending")
    val isYearTransitionPending: Boolean = false // For triggering year-end logic

) {
    companion object {
        const val TABLE_NAME = "term_states"
        const val ID_CURRENT = "current_term" // Standard ID for the active term state
    }
}