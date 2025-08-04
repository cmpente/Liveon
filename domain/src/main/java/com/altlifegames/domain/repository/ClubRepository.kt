package com.altlifegames.domain.repository

import com.altlifegames.domain.model.Character
import com.altlifegames.domain.model.Club

interface ClubRepository {
    suspend fun getAvailableClubs(character: Character): List<Club>
    suspend fun joinClub(character: Character, club: Club): Character
    suspend fun leaveClub(character: Character, clubId: String): Character
}