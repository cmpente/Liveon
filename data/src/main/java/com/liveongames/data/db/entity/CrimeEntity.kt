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
    val riskTier: String,
    val notorietyRequired: Int,
    val baseSuccessChance: Double,
    val payoutMin: Int,
    val payoutMax: Int,
    val jailMin: Int,
    val jailMax: Int,
    val notorietyGain: Int,
    val notorietyLoss: Int,
    val iconDescription: String,
    val scenario: String,
    val success: Boolean?,
    val caught: Boolean?,
    val moneyGained: Int?,
    val actualJailTime: Int?,
    val timestamp: Long
)

fun CrimeEntity.toCrime(): Crime {
    return Crime(
        id = this.id,
        name = this.name,
        description = this.description,
        riskTier = com.liveongames.domain.model.RiskTier.valueOf(this.riskTier),
        notorietyRequired = this.notorietyRequired,
        baseSuccessChance = this.baseSuccessChance,
        payoutMin = this.payoutMin,
        payoutMax = this.payoutMax,
        jailMin = this.jailMin,
        jailMax = this.jailMax,
        notorietyGain = this.notorietyGain,
        notorietyLoss = this.notorietyLoss,
        iconDescription = this.iconDescription,
        scenario = this.scenario,
        success = this.success,
        caught = this.caught,
        moneyGained = this.moneyGained,
        actualJailTime = this.actualJailTime,
        timestamp = this.timestamp
    )
}

fun Crime.toEntity(characterId: String): CrimeEntity {
    return CrimeEntity(
        id = this.id,
        characterId = characterId,
        name = this.name,
        description = this.description,
        riskTier = this.riskTier.name,
        notorietyRequired = this.notorietyRequired,
        baseSuccessChance = this.baseSuccessChance,
        payoutMin = this.payoutMin,
        payoutMax = this.payoutMax,
        jailMin = this.jailMin,
        jailMax = this.jailMax,
        notorietyGain = this.notorietyGain,
        notorietyLoss = this.notorietyLoss,
        iconDescription = this.iconDescription,
        scenario = this.scenario,
        success = this.success,
        caught = this.caught,
        moneyGained = this.moneyGained,
        actualJailTime = this.actualJailTime,
        timestamp = this.timestamp
    )
}