package com.liveongames.domain.usecase

import com.liveongames.domain.model.EduTier
import com.liveongames.domain.repository.EducationRepository
import com.liveongames.domain.repository.PlayerRepository
import javax.inject.Inject

class AutoProgressEducationUseCase @Inject constructor(
    private val educationRepository: EducationRepository,
    private val playerRepository: PlayerRepository
) {
    suspend fun execute() {
        val player = playerRepository.getPlayer() ?: return // Cannot proceed without player
        val currentEnrollment = educationRepository.getEnrollment()

        if (currentEnrollment == null) { // Player is not currently enrolled in any program
 val age = player.age

            when (age) {
 6 -> { // Assuming age 6 is Elementary start
 // Find Elementary program and enroll
 val elementaryProgram = educationRepository.getPrograms()
                        .firstOrNull { it.tier == EduTier.ELEMENTARY }
 elementaryProgram?.let {
 educationRepository.enroll(it.id)
 }
 }
 11 -> { // Assuming age 11 is Middle School start
                    // Find Middle School program and enroll
                    val middleSchoolProgram = educationRepository.getPrograms()
                        .firstOrNull { it.tier == EduTier.MIDDLE_SCHOOL }
 middleSchoolProgram?.let {
 educationRepository.enroll(it.id)
 }
                }
 14 -> { // Assuming age 14 is High School start
                    val highSchoolProgram = educationRepository.getPrograms()
                        .firstOrNull { it.tier == EduTier.HIGH }
                    highSchoolProgram?.let {
                        educationRepository.enroll(it.id)
                    }
                }
                // Add other age-based auto-enrollment logic here (e.g., vocational school entry age)
            }
 } else {
 // Player is already enrolled. Future logic will handle graduation/failure based on progress/GPA
 // This Use Case primarily handles initial enrollment for school tiers.
 // Graduation/Failure logic will be handled elsewhere, likely triggered after applying actions
 // and checking if progress is 100%.
        }
    }
}