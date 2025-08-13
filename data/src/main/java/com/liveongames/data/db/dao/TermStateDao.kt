package com.liveongames.data.db.dao

import androidx.room.*
import com.liveongames.data.db.entity.TermStateEntity
import com.liveongames.data.db.entity.TermStateEntity.Companion.ID_CURRENT
import kotlinx.coroutines.flow.Flow

/**
 * DAO for [TermStateEntity].
 * Provides methods to interact with the term_states table.
 */
@Dao
interface TermStateDao {

    /**
     * Retrieves the current term state as a Flow.
     * This allows observing changes to the term state in a reactive way.
     *
     * Uses a specific query to fetch the row identified by [ID_CURRENT].
     * Assumes only one "current" term state exists.
     */
    @Query("SELECT * FROM ${TermStateEntity.TABLE_NAME} WHERE id = '${ID_CURRENT}'")
    fun getCurrentTermState(): Flow<TermStateEntity?>

    /**
     * Retrieves the current term state synchronously (non-reactive).
     * This is useful for one-off reads or when used inside a coroutine.
     *
     * @return The current [TermStateEntity] or null if not found.
     */
    @Query("SELECT * FROM ${TermStateEntity.TABLE_NAME} WHERE id = '${ID_CURRENT}'")
    suspend fun getCurrentTermStateOnce(): TermStateEntity?

    /**
     * Inserts a new term state or replaces an existing one if the primary key (id) matches.
     * Used for initializing or completely overwriting the current term state.
     *
     * @param termState The [TermStateEntity] to insert or replace.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTermState(termState: TermStateEntity)

    /**
     * Updates specific fields of the current term state.
     * This is more efficient than replacing the entire entity if only a few fields change.
     *
     * @param currentYear The new current year.
     * @param termProgress The new term progress (0-100).
     * @param activeEducationProgramId The new active education program ID (nullable).
     * @param lastActionTimestamp The timestamp of the last action.
     * @param isYearTransitionPending Whether a year transition is pending.
     * @return The number of rows affected (should be 1 if successful).
     */
    @Query(
        "UPDATE ${TermStateEntity.TABLE_NAME} SET " +
                "current_year = :currentYear, " +
                "term_progress = :termProgress, " +
                "active_education_program_id = :activeEducationProgramId, " +
                "last_action_timestamp = :lastActionTimestamp, " +
                "is_year_transition_pending = :isYearTransitionPending " +
                "WHERE id = '${ID_CURRENT}'"
    )
    suspend fun updateCurrentTermState(
        currentYear: Int,
        termProgress: Int,
        activeEducationProgramId: String?,
        lastActionTimestamp: Long?,
        isYearTransitionPending: Boolean
    ): Int

    /**
     * A more generic update method using the entity itself.
     * Useful if many fields are changing or for full entity replacement when necessary.
     *
     * @param termState The updated [TermStateEntity] object.
     * @return The number of rows updated.
     */
    @Update
    suspend fun updateTermState(termState: TermStateEntity): Int

    /**
     * Marks the year transition as processed (sets the flag to false).
     * This would be called after the year-end logic has been handled.
     *
     * @return The number of rows affected.
     */
    @Query("UPDATE ${TermStateEntity.TABLE_NAME} SET is_year_transition_pending = 0 WHERE id = '${ID_CURRENT}'")
    suspend fun markYearTransitionProcessed(): Int

    /**
     * Resets the term progress to 0.
     * This might be used at the start of a new education period or term.
     *
     * @return The number of rows affected.
     */
    @Query("UPDATE ${TermStateEntity.TABLE_NAME} SET term_progress = 0 WHERE id = '${ID_CURRENT}'")
    suspend fun resetTermProgress(): Int

    // Optional: Method to delete the term state, though likely not needed for "current" state.
    // @Query("DELETE FROM ${TermStateEntity.TABLE_NAME} WHERE id = '${ID_CURRENT}'")
    // suspend fun deleteCurrentTermState(): Int
}