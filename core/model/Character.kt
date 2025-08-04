package com.altlifegames.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "characters")
data class Character(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    var firstName: String,
    var lastName: String,
    var gender: Gender,
    var age: Int,
    var health: Int,
    var happiness: Int,
    var smarts: Int,
    var looks: Int,
    var money: Long,
    var reputation: Int,
    var isAlive: Boolean = true,
    @TypeConverters(RelationshipConverter::class)
    var relationships: List<Relationship> = emptyList(),
    @TypeConverters(AchievementConverter::class)
    var achievements: List<Long> = emptyList()
)

enum class Gender { MALE, FEMALE, OTHER }

data class Relationship(
    val type: RelationshipType,
    val name: String,
    val level: Int // 0 = stranger, 100 = soulmate/parent
)

enum class RelationshipType {
    PARENT, SIBLING, FRIEND, PARTNER, SPOUSE, CHILD, OTHER
}
