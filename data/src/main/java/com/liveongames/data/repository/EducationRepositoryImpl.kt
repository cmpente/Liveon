// app/src/main/java/com/liveongames/data/repository/EducationRepositoryImpl.kt
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
        return educationDao.getEducationsForCharacter(PLAYER_CHARACTER_ID).map { educationEntities ->
            Log.d(TAG, "DB returned ${educationEntities.size} education entities for $PLAYER_CHARACTER_ID")
            educationEntities.map { it.toEducation() }.also { educations ->
                Log.d(TAG, "Mapped to ${educations.size} education models")
            }
        }
    }

    override suspend fun getEducationById(educationId: String): Education? {
        Log.d(TAG, "getEducationById called for character: $PLAYER_CHARACTER_ID, educationId: $educationId")
        return educationDao.getEducationById(PLAYER_CHARACTER_ID, educationId)?.toEducation()
    }

    override suspend fun addEducation(education: Education) {
        Log.d(TAG, "addEducation called for character: $PLAYER_CHARACTER_ID, education: ${education.name}")
        val educationEntity = education.toEntity(PLAYER_CHARACTER_ID)
        Log.d(TAG, "Inserting education entity: ${educationEntity.id} for character: $PLAYER_CHARACTER_ID")
        educationDao.insertEducation(educationEntity)
        Log.d(TAG, "Education inserted successfully")
    }

    override suspend fun updateEducation(education: Education) {
        Log.d(TAG, "updateEducation called for character: $PLAYER_CHARACTER_ID, education: ${education.name}")
        val educationEntity = education.toEntity(PLAYER_CHARACTER_ID)
        Log.d(TAG, "Updating education entity: ${educationEntity.id} for character: $PLAYER_CHARACTER_ID")
        educationDao.insertEducation(educationEntity)
        Log.d(TAG, "Education updated successfully")
    }

    override suspend fun removeEducation(educationId: String) {
        Log.d(TAG, "removeEducation called for character: $PLAYER_CHARACTER_ID, educationId: $educationId")
        educationDao.removeEducation(educationId, PLAYER_CHARACTER_ID)
        Log.d(TAG, "Education removed successfully")
    }

    override suspend fun clearEducations() {
        Log.d(TAG, "clearEducations called for character: $PLAYER_CHARACTER_ID")
        educationDao.clearEducationsForCharacter(PLAYER_CHARACTER_ID)
        Log.d(TAG, "Educations cleared for character: $PLAYER_CHARACTER_ID")
    }
}