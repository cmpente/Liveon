// app/src/main/java/com/liveongames/data/db/entity/CrimeEntity.kt
package com.liveongames.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.liveongames.domain.model.Crime
import com.liveongames.domain.model.RiskTier

@Entity(tableName = "crimes")
data class CrimeEntity(
    @PrimaryKey
    val id: String,
    val characterId: String,
    val name: String,
    val description: String,
    val riskTier: String,           // stored as enum name
    val notorietyRequired: Int,
    val baseSuccessChance: Double,  // stored as 0.0..100.0
    val payoutMin: Int,
    val payoutMax: Int,
    val jailMin: Int,
    val jailMax: Int,
    val notorietyGain: Int,
    val notorietyLoss: Int,
    val iconDescription: String,
    val scenario: String,           // non-null in DB (empty = none)
    val success: Boolean?,          // last outcome snapshot (optional)
    val caught: Boolean?,
    val moneyGained: Int?,
    val actualJailTime: Int?,
    val timestamp: Long             // last run timestamp (ms)
)

@Suppress("unused")
fun CrimeEntity.toCrime(): Crime {
    return Crime(
        id = this.id,
        name = this.name,
        description = this.description,
        riskTier = RiskTier.valueOf(this.riskTier),
        notorietyRequired = this.notorietyRequired,
        // entity Double -> domain Int
        baseSuccessChance = this.baseSuccessChance.toInt(),
        payoutMin = this.payoutMin,
        payoutMax = this.payoutMax,
        jailMin = this.jailMin,
        jailMax = this.jailMax,
        notorietyGain = this.notorietyGain,
        notorietyLoss = this.notorietyLoss,
        iconDescription = this.iconDescription,
        // DB guarantees non-null; domain allows nullable
        scenario = this.scenario,
        success = this.success,
        caught = this.caught,
        moneyGained = this.moneyGained,
        actualJailTime = this.actualJailTime,
        // DB non-null; domain allows nullable
        timestamp = this.timestamp
    )
}

@Suppress("unused")
fun Crime.toEntity(characterId: String): CrimeEntity {
    return CrimeEntity(
        id = this.id,
        characterId = characterId,
        name = this.name,
        description = this.description,
        riskTier = this.riskTier.name,
        notorietyRequired = this.notorietyRequired,
        // domain Int -> entity Double
        baseSuccessChance = this.baseSuccessChance.toDouble(),
        payoutMin = this.payoutMin,
        payoutMax = this.payoutMax,
        jailMin = this.jailMin,
        jailMax = this.jailMax,
        notorietyGain = this.notorietyGain,
        notorietyLoss = this.notorietyLoss,
        iconDescription = this.iconDescription,
        // entity expects non-null string
        scenario = this.scenario ?: "",
        success = this.success,
        caught = this.caught,
        moneyGained = this.moneyGained,
        actualJailTime = this.actualJailTime,
        // entity expects non-null timestamp
        timestamp = this.timestamp ?: System.currentTimeMillis()
    )
}