// data/src/main/java/com/liveongames/data/model/education/EducationActionDef.kt
package com.liveongames.data.model.education

import com.liveongames.domain.model.ActionDef
import com.liveongames.domain.model.DialogStep
import com.liveongames.domain.model.EduTier
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class EducationActionDef(
    override val id: String,
    override val title: String,
    val tiersList: List<String>,
    override val minGpa: Double? = null,
    override val maxGpa: Double? = null,
    override val cooldownMinutes: Int,
    override val baseProgress: Int,
    override val gpaDeltaMin: Double,
    override val gpaDeltaMax: Double,
    @Contextual
    override val dialog: List<DialogStep>
) : ActionDef {
    override val tiers: List<EduTier> = tiersList.mapNotNull { tierStr ->
        EduTier.values().find { it.name.equals(tierStr, ignoreCase = true) }
    }
}