// app/src/main/java/com/liveongames/data/db/entity/PetEntity.kt
package com.liveongames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.liveongames.domain.model.Pet

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey
    val id: String,
    val characterId: String,
    val name: String,
    val type: String,
    val happiness: Int,
    val cost: Int
)

// Mapper extension
fun PetEntity.toPet(): Pet {
    return Pet(
        id = this.id,
        name = this.name,
        type = this.type,
        happiness = this.happiness,
        cost = this.cost
    )
}