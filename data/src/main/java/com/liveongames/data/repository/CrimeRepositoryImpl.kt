// data/src/main/java/com/liveongames/data/repository/CrimeRepositoryImpl.kt
package com.liveongames.data.repository

import android.content.Context
import com.liveongames.data.crime.CrimePersistence
import com.liveongames.domain.model.CrimeRecordEntry
import com.liveongames.domain.model.CrimeStats
import com.liveongames.domain.repository.CrimeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class CrimeRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : CrimeRepository {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val store = CrimePersistence(appContext)

    override fun observeStats(): Flow<CrimeStats> =
        store.snapshot().map { snap ->
            val list: List<CrimeRecordEntry> =
                runCatching { json.decodeFromString<List<CrimeRecordEntry>>(snap.recordsJson) }
                    .getOrElse { emptyList() }
            CrimeStats(
                currentYear = snap.currentYear,
                earnedThisYear = snap.earnedThisYear,
                records = list
            )
        }

    override suspend fun ensureYear(year: Int) = store.setYearAndMaybeReset(year)
    override suspend fun getEarnedThisYear(): Int = store.getEarnedThisYear()
    override suspend fun setEarnedThisYear(value: Int) = store.setEarnedThisYear(value)
    override suspend fun resetEarnedForNewYear(year: Int) { store.setYearAndMaybeReset(year) }

    override suspend fun appendRecord(entry: CrimeRecordEntry, maxKeep: Int) {
        val snap = store.snapshot().first()
        val old: List<CrimeRecordEntry> =
            runCatching { json.decodeFromString<List<CrimeRecordEntry>>(snap.recordsJson) }
                .getOrElse { emptyList() }
        val next = (listOf(entry) + old).take(maxKeep)
        store.writeRecords(json.encodeToString(next))
    }
}