// app/src/main/java/com/liveongames/data/db/entity/CrimeEntity.kt
package com.liveongames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.liveongames.domain.model.Crime

@Entity(tableName = "crimes")
data class CrimeEntity(
    @PrimaryKey
    val id: String,
    val characterId: String,
    val name: String,
    val description: String,
    val severity: Int,
    val chanceOfGettingCaught: Double,
    val fine: Int,
    val jailTime: Int
)

// Mapper extension
fun CrimeEntity.toCrime(): Crime {
    return Crime(
        id = this.id,
        name = this.name,
        description = this.description,
        severity = this.severity,
        chanceOfGettingCaught = this.chanceOfGettingCaught,
        fine = this.fine,
        jailTime = this.jailTime
    )
}