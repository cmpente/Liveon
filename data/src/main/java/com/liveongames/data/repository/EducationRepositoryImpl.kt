// app/src/main/java/com/liveongames/data/repository/EducationRepositoryImpl.kt
package com.liveongames.data.repository

import android.util.Log
import com.liveongames.data.db.dao.EducationActionStateDao
import com.liveongames.data.db.dao.EducationDao
import com.liveongames.data.db.dao.TermStateDao
import com.liveongames.data.db.entity.EducationActionStateEntity
import com.liveongames.data.db.entity.TermStateEntity
import com.liveongames.data.db.entity.toDomain
import com.liveongames.data.db.entity.toEntity
import com.liveongames.domain.model.Education
import com.liveongames.domain.repository.EducationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class EducationRepositoryImpl @Inject constructor(
    private val educationDao: EducationDao,
    private val actionStateDao: EducationActionStateDao,
    private val termStateDao: TermStateDao
) : EducationRepository {

    companion object { private const val PLAYER = "player_character" }

    override fun getEducations(): Flow<List<Education>> =
        educationDao.observeForCharacter(PLAYER)
            .map { it.map { e -> e.toDomain() } }
            .catch { e ->
                Log.e("EducationRepo", "observe educations failed", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)

    override suspend fun getEducationById(educationId: String): Education? =
        educationDao.getById(PLAYER, educationId)?.toDomain()

    override suspend fun addEducation(education: Education) {
        educationDao.upsert(education.toEntity(PLAYER))
    }

    override suspend fun updateEducation(education: Education) {
        educationDao.upsert(education.toEntity(PLAYER))
    }

    override suspend fun removeEducation(educationId: String) {
        educationDao.deleteById(PLAYER, educationId)
    }

    override suspend fun clearEducations() {
        educationDao.clear(PLAYER)
    }

    suspend fun deactivateAll() = educationDao.deactivateAll(PLAYER)

    suspend fun setGpa(educationId: String, gpa: Double) {
        val clamped = min(4.0, max(0.0, gpa))
        educationDao.updateGpa(PLAYER, educationId, clamped)
    }

    suspend fun updateStatus(educationId: String, isActive: Boolean, completionDate: Long?) {
        educationDao.updateStatus(PLAYER, educationId, isActive, completionDate)
    }

    suspend fun getActionState(educationId: String, actionId: String): EducationActionStateEntity? =
        actionStateDao.get(PLAYER, educationId, actionId)

    suspend fun upsertActionState(state: EducationActionStateEntity) =
        actionStateDao.upsert(state)

    suspend fun updateActionState(educationId: String, actionId: String, lastUsedAt: Long, usedThisAge: Int) =
        actionStateDao.update(PLAYER, educationId, actionId, lastUsedAt, usedThisAge)

    suspend fun resetActionCapsForAge(educationId: String) =
        actionStateDao.resetUsedThisAge(PLAYER, educationId)

    fun observeTermState(): Flow<TermStateEntity?> =
        termStateDao.observe(PLAYER)
            .catch { e ->
                Log.e("EducationRepo", "observe term state failed", e)
                emit(null)
            }
            .flowOn(Dispatchers.IO)

    suspend fun getTermState(): TermStateEntity? = termStateDao.get(PLAYER)
    suspend fun upsertTermState(state: TermStateEntity) = termStateDao.upsert(state)
    suspend fun updateTermState(focus: Int, streakDays: Int, professorRelationship: Int, weekIndex: Int, coursePhase: String) =
        termStateDao.update(PLAYER, focus, streakDays, professorRelationship, weekIndex, coursePhase)
}