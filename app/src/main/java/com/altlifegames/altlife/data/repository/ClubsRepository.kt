// app/src/main/java/com/altlifegames/altlife/data/repository/ClubsRepository.kt
package com.altlifegames.altlife.data.repository

import android.R
import com.altlifegames.domain.model.Club
import com.altlifegames.domain.model.ClubCategory
import com.altlifegames.domain.model.TryoutDifficulty

object ClubsRepository {
    val allClubs = listOf(
        // Sports Clubs
        Club(
            id = "basketball_team",
            name = "Basketball Team",
            description = "Competitive basketball for varsity players",
            iconRes = R.drawable.ic_menu_gallery,
            category = ClubCategory.SPORTS,
            skillRequirement = "Athletics",
            skillLevel = 7,
            tryoutDifficulty = TryoutDifficulty.HARD
        ),
        Club(
            id = "soccer_club",
            name = "Soccer Club",
            description = "Intramural and competitive soccer teams",
            iconRes = R.drawable.ic_menu_gallery,
            category = ClubCategory.SPORTS,
            skillRequirement = "Athletics",
            skillLevel = 5,
            tryoutDifficulty = TryoutDifficulty.MEDIUM
        ),
        Club(
            id = "swimming_team",
            name = "Swimming Team",
            description = "Competitive swimming for all skill levels",
            iconRes = R.drawable.ic_menu_gallery,
            category = ClubCategory.SPORTS,
            skillRequirement = "Athletics",
            skillLevel = 6,
            tryoutDifficulty = TryoutDifficulty.MEDIUM
        ),
        Club(
            id = "track_field",
            name = "Track & Field",
            description = "Sprint, distance, and field events",
            iconRes = R.drawable.ic_menu_gallery,
            category = ClubCategory.SPORTS,
            skillRequirement = "Athletics",
            skillLevel = 4,
            tryoutDifficulty = TryoutDifficulty.EASY
        ),
        
        // Academic Clubs
        Club(
            id = "debate_team",
            name = "Debate Team",
            description = "Competitive debate and public speaking",
            iconRes = R.drawable.ic_menu_gallery,
            category = ClubCategory.ACADEMIC,
            gpaRequirement = 3.0,
            skillRequirement = "Intelligence",
            skillLevel = 6,
            tryoutDifficulty = TryoutDifficulty.MEDIUM
        ),
        Club(
            id = "math_club",
            name = "Math Club",
            description = "Math competitions and problem solving",
            iconRes = R.drawable.ic_menu_gallery,
            category = ClubCategory.ACADEMIC,
            gpaRequirement = 2.5,
            skillRequirement = "Intelligence",
            skillLevel = 5,
            tryoutDifficulty = TryoutDifficulty.EASY
        ),
        Club(
            id = "science_olympiad",
            name = "Science Olympiad",
            description = "STEM competitions and experiments",
            iconRes = R.drawable.ic_menu_gallery,
            category = ClubCategory.ACADEMIC,
            gpaRequirement = 2.8,
            skillRequirement = "Intelligence",
            skillLevel = 7,
            tryoutDifficulty = TryoutDifficulty.HARD
        ),
        Club(
            id = "foreign_language",
            name = "Foreign Language Club",
            description = "Cultural exchange and language practice",
            iconRes = R.drawable.ic_menu_gallery,
            category = ClubCategory.ACADEMIC,
            gpaRequirement = 2.0,
            tryoutDifficulty = TryoutDifficulty.EASY
        ),
        
        // Creative Clubs
        Club(
            id = "drama_club",
            name = "Drama Club",
            description = "Theater productions and acting workshops",
            iconRes = R.drawable.ic_menu_gallery,
            category = ClubCategory.CREATIVE,
            skillRequirement = "Creativity",
            skillLevel = 5,
            tryoutDifficulty = TryoutDifficulty.MEDIUM
        ),
        Club(
            id = "art_club",
            name = "Art Club",
            description = "Visual arts projects and exhibitions",
            iconRes = R.drawable.ic_menu_gallery,
            category = ClubCategory.CREATIVE,
            skillRequirement = "Creativity",
            skillLevel = 4,
            tryoutDifficulty = TryoutDifficulty.EASY
        ),
        Club(
            id = "band",
            name = "School Band",
            description = "Concert and marching band performances",
            iconRes = R.drawable.ic_menu_gallery,
            category = ClubCategory.CREATIVE,
            skillRequirement = "Creativity",
            skillLevel = 3,
            tryoutDifficulty = TryoutDifficulty.EASY
        ),
        Club(
            id = "yearbook",
            name = "Yearbook Committee",
            description = "Design and publish the school yearbook",
            iconRes = R.drawable.ic_menu_gallery,
            category = ClubCategory.CREATIVE,
            gpaRequirement = 2.0,
            tryoutDifficulty = TryoutDifficulty.EASY
        ),
        
        // Volunteer/Leadership
        Club(
            id = "student_government",
            name = "Student Government",
            description = "Represent student interests and lead initiatives",
            iconRes = R.drawable.ic_menu_gallery,
            category = ClubCategory.LEADERSHIP,
            gpaRequirement = 2.5,
            skillRequirement = "Social",
            skillLevel = 6,
            tryoutDifficulty = TryoutDifficulty.HARD
        ),
        Club(
            id = "key_club",
            name = "Key Club",
            description = "Community service and volunteer work",
            iconRes = R.drawable.ic_menu_gallery,
            category = ClubCategory.VOLUNTEER,
            tryoutDifficulty = TryoutDifficulty.EASY
        ),
        Club(
            id = "environmental_club",
            name = "Environmental Club",
            description = "Sustainability projects and eco-awareness",
            iconRes = R.drawable.ic_menu_gallery,
            category = ClubCategory.VOLUNTEER,
            tryoutDifficulty = TryoutDifficulty.EASY
        )
    )
    
    fun getClubsByCategory(category: ClubCategory): List<Club> {
        return allClubs.filter { it.category == category }
    }
    
    fun getClubsByPlayerStats(gpa: Double, skills: Map<String, Int>): List<Club> {
        return allClubs.filter { club ->
            // Check GPA requirement
            if (club.gpaRequirement > 0 && gpa < club.gpaRequirement) {
                false
            } else if (club.skillRequirement != null) {
                // Check skill requirement
                val playerSkillLevel = skills[club.skillRequirement] ?: 0
                playerSkillLevel >= club.skillLevel
            } else {
                true
            }
        }
    }
}