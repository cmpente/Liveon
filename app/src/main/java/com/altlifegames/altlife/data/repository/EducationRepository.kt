// app/src/main/java/com/altlifegames/altlife/data/repository/EducationRepository.kt
package com.altlifegames.altlife.data.repository

import android.R
import com.altlifegames.domain.model.EducationOption

object EducationRepository {
    val educationOptions = listOf(
        // Elementary Education
        EducationOption(
            id = "grade_school",
            name = "Grade School",
            minAge = 6,
            maxAge = 11,
            cost = 0,
            description = "Elementary education foundation",
            iconRes = R.drawable.ic_menu_gallery,
            yearsRequired = 6
        ),
        
        // Middle School
        EducationOption(
            id = "middle_school",
            name = "Middle School",
            minAge = 12,
            maxAge = 13,
            cost = 0,
            description = "Junior high preparation",
            iconRes = R.drawable.ic_menu_gallery,
            yearsRequired = 2,
            prerequisites = listOf("grade_school")
        ),
        
        // High School
        EducationOption(
            id = "high_school",
            name = "High School",
            minAge = 14,
            maxAge = 18,
            cost = 0,
            description = "Complete your high school education",
            iconRes = R.drawable.ic_menu_gallery,
            yearsRequired = 4,
            prerequisites = listOf("middle_school")
        ),
        
        // Community College
        EducationOption(
            id = "community_college",
            name = "Community College",
            minAge = 18,
            maxAge = 20,
            cost = 2000,
            description = "2-year associate degree",
            iconRes = R.drawable.ic_menu_gallery,
            yearsRequired = 2,
            prerequisites = listOf("high_school")
        ),
        
        // University
        EducationOption(
            id = "university",
            name = "University",
            minAge = 18,
            maxAge = 22,
            cost = 15000,
            description = "4-year bachelor's degree",
            iconRes = R.drawable.ic_menu_gallery,
            yearsRequired = 4,
            prerequisites = listOf("high_school")
        ),
        
        // Graduate School
        EducationOption(
            id = "graduate_school",
            name = "Graduate School",
            minAge = 22,
            maxAge = 24,
            cost = 30000,
            description = "Master's degree",
            iconRes = R.drawable.ic_menu_gallery,
            yearsRequired = 2,
            prerequisites = listOf("university")
        ),
        
        // PhD Program
        EducationOption(
            id = "phd_program",
            name = "PhD Program",
            minAge = 24,
            maxAge = 28,
            cost = 25000,
            description = "Doctoral degree",
            iconRes = R.drawable.ic_menu_gallery,
            yearsRequired = 4,
            prerequisites = listOf("graduate_school")
        ),
        
        // Medical School
        EducationOption(
            id = "medical_school",
            name = "Medical School",
            minAge = 22,
            maxAge = 26,
            cost = 50000,
            description = "Become a doctor",
            iconRes = R.drawable.ic_menu_gallery,
            yearsRequired = 4,
            prerequisites = listOf("university"),
            gpaRequirement = 3.5
        ),
        
        // Law School
        EducationOption(
            id = "law_school",
            name = "Law School",
            minAge = 22,
            maxAge = 25,
            cost = 40000,
            description = "Become a lawyer",
            iconRes = R.drawable.ic_menu_gallery,
            yearsRequired = 3,
            prerequisites = listOf("university"),
            gpaRequirement = 3.3
        ),
        
        // Business School
        EducationOption(
            id = "business_school",
            name = "Business School",
            minAge = 22,
            maxAge = 24,
            cost = 35000,
            description = "MBA program",
            iconRes = R.drawable.ic_menu_gallery,
            yearsRequired = 2,
            prerequisites = listOf("university"),
            gpaRequirement = 3.0
        )
    )
    
    fun getAvailableEducations(playerAge: Int, currentEducation: String): List<EducationOption> {
        return educationOptions.filter { 
            it.minAge <= playerAge && 
            it.maxAge >= playerAge && 
            meetsPrerequisites(it, currentEducation)
        }
    }
    
    private fun meetsPrerequisites(education: EducationOption, currentEducation: String): Boolean {
        return if (education.prerequisites.isEmpty()) {
            true
        } else {
            education.prerequisites.contains(currentEducation)
        }
    }
}