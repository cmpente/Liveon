// data/src/main/java/com/liveongames/data/model/education/EducationCourse.kt
package com.liveongames.data.model.education

import com.liveongames.domain.model.AcademicSchema
import com.liveongames.domain.model.EducationProgram
import com.liveongames.domain.model.EduTier
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Concrete implementation of the EducationProgram interface for data layer/model loading.
 * This adapts the data model loaded from JSON to the domain interface.
 */
@Serializable
data class EducationCourse(
    override val id: String,
    override val title: String,
    override val description: String,
    override val tier: EduTier,

    @Contextual
    override val schema: AcademicSchema,
    override val minGpa: Double,
    override val tuition: Int,
    // Store as List for serialization, but expose as Set in interface
    private val requirementsList: List<String>
) : EducationProgram {
    // Convert List to Set for the interface implementation
    override val requirements: Set<String> = requirementsList.toSet()
}