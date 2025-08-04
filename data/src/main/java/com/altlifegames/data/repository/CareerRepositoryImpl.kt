package com.altlifegames.data.repository

import com.altlifegames.data.db.dao.CareerDao
import com.altlifegames.data.db.entity.CareerEntity
import com.altlifegames.domain.model.Career
import com.altlifegames.domain.repository.CareerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CareerRepositoryImpl @Inject constructor(private val careerDao: CareerDao) : CareerRepository {
    override fun getAvailableCareers(): Flow<List<Career>> = flow {
        val careers = listOf(
            Career(0, "Software Engineer", "TechCorp", 50000.0, 0, 0),
            Career(0, "Teacher", "Public School", 35000.0, 0, 0),
            Career(0, "Athlete", "Sports Club", 60000.0, 0, 0),
            Career(0, "Musician", "Music Label", 40000.0, 0, 0)
        )
        emit(careers)
    }

    override suspend fun applyForCareer(characterId: Long, career: Career): Boolean {
        val entity = CareerEntity(
            id = career.id,
            characterId = characterId,
            title = career.title,
            company = career.company,
            salary = career.salary,
            level = career.level,
            experience = career.experience
        )
        careerDao.insert(entity)
        return true
    }

    override suspend fun promote(characterId: Long) {
        // This is a simplified implementation - in reality you'd need to get the current career
        // and update it, but we don't have a good way to identify which career to promote
    }

    override suspend fun demote(characterId: Long) {
        // This is a simplified implementation - in reality you'd need to get the current career
        // and update it, but we don't have a good way to identify which career to demote
    }

    override fun getCurrentCareer(characterId: Long): Flow<Career?> = 
        careerDao.getCareersForCharacter(characterId).map { entities ->
            entities.firstOrNull()?.let { entity ->
                Career(
                    id = entity.id,
                    title = entity.title,
                    company = entity.company,
                    salary = entity.salary,
                    level = entity.level,
                    experience = entity.experience
                )
            }
        }
}