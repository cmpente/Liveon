package com.liveongames.domain.repository

import com.liveongames.domain.model.Club
import com.liveongames.domain.model.Character

interface ClubRepository {
    fun getAvailableClubs(character: Character): List<Club>
    fun getClubById(id: String): Club?
    suspend fun joinClub(character: Character, club: Club)
    suspend fun leaveClub(character: Character, clubId: String)
}