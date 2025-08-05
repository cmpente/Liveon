package com.altlifegames.data.repository

import com.altlifegames.domain.model.Career
import com.altlifegames.domain.repository.CareerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class CareerRepositoryImpl @Inject constructor() : CareerRepository {

    private val careers = MutableStateFlow<List<Career>>(emptyList())
    private val currentCareer = MutableStateFlow<Career?>(null)

    override fun getAvailableCareers(): Flow<List<Career>> {
        return careers
    }

    override suspend fun getCareerById(careerId: String): Career? {
        return careers.value.find { it.id == careerId }
    }

    override suspend fun getCurrentCareer(): Career? {
        return currentCareer.value
    }

    override suspend fun changeCareer(career: Career) {
        currentCareer.value = career
        val currentCareers = careers.value.toMutableList()
        if (!currentCareers.contains(career)) {
            currentCareers.add(career)
            careers.value = currentCareers
        }
    }
}