package com.liveongames.data.repository

import android.util.Log
import com.liveongames.data.db.dao.EducationDao
import com.liveongames.data.db.entity.toEducation
import com.liveongames.data.db.entity.toEntity
import com.liveongames.domain.model.Education
import com.liveongames.domain.repository.EducationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EducationRepositoryImpl @Inject constructor(
    private val educationDao: EducationDao
) : EducationRepository {

    companion object {
        private const val TAG = "EducationRepository"
        private const val PLAYER_CHARACTER_ID = "player_character"
    }

    override fun getEducations(): Flow<List<Education>> {
        Log.d(TAG, "getEducations called for character: $PLAYER_CHARACTER_ID")
        return educationDao.getEducationsForCharacter(PLAYER_CHARACTER_ID).map { entities ->
            entities.map { it.toEducation() }
        }
    }

    override suspend fun getEducationById(educationId: String): Education? {
        return educationDao.getEducationById(PLAYER_CHARACTER_ID, educationId)?.toEducation()
    }

    override suspend fun addEducation(education: Education) {
        educationDao.insertEducation(education.toEntity(PLAYER_CHARACTER_ID))
    }

    override suspend fun updateEducation(education: Education) {
        educationDao.insertEducation(education.toEntity(PLAYER_CHARACTER_ID))
    }

    override suspend fun removeEducation(educationId: String) {
        educationDao.removeEducation(educationId, PLAYER_CHARACTER_ID)
    }

    override suspend fun clearEducations() {
        educationDao.clearEducationsForCharacter(PLAYER_CHARACTER_ID)
    }
}
