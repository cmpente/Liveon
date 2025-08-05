package com.altlifegames.data.repository

import com.altlifegames.domain.model.Club
import com.altlifegames.domain.model.Character
import com.altlifegames.domain.repository.ClubRepository
import javax.inject.Inject

class ClubRepositoryImpl @Inject constructor() : ClubRepository {

    private val allClubs = listOf<Club>(
        Club(
            id = "book_club",
            name = "Book Club",
            description = "A quiet gathering of literature enthusiasts",
            membershipFee = 10,
            benefits = mapOf("intelligence" to 2),
            minimumAge = 16,
            minimumSocialRequirement = 30
        ),
        Club(
            id = "sports_club",
            name = "Sports Club",
            description = "Stay fit and make friends through sports",
            membershipFee = 25,
            benefits = mapOf("fitness" to 3, "social" to 2),
            minimumAge = 14,
            minimumSocialRequirement = 20
        )
        // Add more clubs as needed
    )

    override fun getAvailableClubs(character: Character): List<Club> {
        return allClubs.filter { club ->
            character.age >= club.minimumAge &&
                    character.social >= club.minimumSocialRequirement
        }
    }

    override fun getClubById(id: String): Club? {
        return allClubs.find { it.id == id }
    }

    override suspend fun joinClub(character: Character, club: Club) {
        // Implementation for joining club
        // This would typically update character state
    }

    override suspend fun leaveClub(character: Character, clubId: String) {
        // Implementation for leaving club
        // This would typically update character state
    }
}