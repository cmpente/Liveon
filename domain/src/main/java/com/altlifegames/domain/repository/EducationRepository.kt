package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Character
import com.altlifegames.domain.model.EducationOption

interface EducationRepository {
    suspend fun getAvailableEducation(character: Character): List<EducationOption>
    suspend fun enrollInEducation(character: Character, education: EducationOption): Character
    suspend fun completeEducation(character: Character, education: EducationOption): Character
}