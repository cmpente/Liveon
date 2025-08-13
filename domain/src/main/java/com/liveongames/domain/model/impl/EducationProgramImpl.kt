// domain/src/main/java/com/liveongames/domain/model/impl/EducationProgramImpl.kt
package com.liveongames.domain.model.impl

import com.liveongames.domain.model.AcademicSchema
import com.liveongames.domain.model.EduTier
import com.liveongames.domain.model.EducationProgram

data class EducationProgramImpl(
    override val id: String,
    override val title: String,
    override val description: String,
    override val tier: EduTier,
    override val schema: AcademicSchema,
    override val minGpa: Double,
    override val tuition: Int,
    override val requirements: Set<String>
) : EducationProgram