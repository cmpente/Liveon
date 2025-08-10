package com.liveongames.liveon.ui.screens.education

import androidx.annotation.DrawableRes
import com.liveongames.domain.model.EducationLevel
import com.liveongames.liveon.R

fun monthsToNiceString(months: Int): String {
    val years = months / 12
    val rem = months % 12
    return when {
        years > 0 && rem > 0 -> "$years yr ${rem} mo"
        years > 0 -> "$years yr"
        else -> "$rem mo"
    }
}

@DrawableRes
fun levelToCrest(level: EducationLevel): Int = when (level) {
    EducationLevel.BASIC, EducationLevel.HIGH_SCHOOL -> R.drawable.ic_school
    EducationLevel.ASSOCIATE -> R.drawable.ic_graduate
    EducationLevel.BACHELOR -> R.drawable.ic_university
    EducationLevel.MASTER, EducationLevel.DOCTORATE -> R.drawable.ic_graduate
    EducationLevel.CERTIFICATION -> R.drawable.ic_certificate
}
