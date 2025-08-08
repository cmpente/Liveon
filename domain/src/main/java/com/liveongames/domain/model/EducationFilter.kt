// domain/src/main/java/com/liveongames/domain/model/EducationFilter.kt
package com.liveongames.domain.model

enum class EducationFilter {
    ALL,
    FREE,
    PAID,
    SHORT,
    LONG;

    fun getDisplayName(): String {
        return when (this) {
            ALL -> "All"
            FREE -> "Free"
            PAID -> "Paid"
            SHORT -> "Short"
            LONG -> "Long"
        }
    }
}