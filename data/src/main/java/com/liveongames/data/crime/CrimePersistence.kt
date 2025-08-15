// data/src/main/java/com/liveongames/data/crime/CrimePersistence.kt
package com.liveongames.data.crime

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DS_NAME = "crime_prefs"

// Creates Context.crimeDataStore
val Context.crimeDataStore: DataStore<Preferences> by preferencesDataStore(name = DS_NAME)

internal object CrimeKeys {
    val CURRENT_YEAR = intPreferencesKey("crime_current_year")
    val EARNED_THIS_YEAR = intPreferencesKey("crime_earned_this_year")
    val RECORDS_JSON = stringPreferencesKey("crime_records_json") // JSON array of CrimeRecordEntry
}

internal class CrimePersistence(private val appContext: Context) {

    data class Snapshot(
        val currentYear: Int,
        val earnedThisYear: Int,
        val recordsJson: String
    )

    fun snapshot(): Flow<Snapshot> = appContext.crimeDataStore.data.map { prefs ->
        Snapshot(
            currentYear = prefs[CrimeKeys.CURRENT_YEAR] ?: 0,
            earnedThisYear = prefs[CrimeKeys.EARNED_THIS_YEAR] ?: 0,
            recordsJson = prefs[CrimeKeys.RECORDS_JSON] ?: "[]"
        )
    }

    suspend fun setYearAndMaybeReset(year: Int) {
        appContext.crimeDataStore.edit { prefs ->
            val storedYear = prefs[CrimeKeys.CURRENT_YEAR] ?: 0
            if (storedYear != year) {
                prefs[CrimeKeys.CURRENT_YEAR] = year
                prefs[CrimeKeys.EARNED_THIS_YEAR] = 0
            }
        }
    }

    suspend fun setEarnedThisYear(value: Int) {
        appContext.crimeDataStore.edit { it[CrimeKeys.EARNED_THIS_YEAR] = value }
    }

    suspend fun getEarnedThisYear(): Int {
        var v = 0
        appContext.crimeDataStore.edit { prefs -> v = prefs[CrimeKeys.EARNED_THIS_YEAR] ?: 0 }
        return v
    }

    /** Overwrite the serialized records JSON array. */
    suspend fun writeRecords(serialized: String) {
        appContext.crimeDataStore.edit { it[CrimeKeys.RECORDS_JSON] = serialized }
    }
}