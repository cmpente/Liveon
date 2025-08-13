// data/assets/education/EducationAssetLoader.kt (CORE PART)
package com.liveongames.data.assets.education

import android.content.Context
import com.liveongames.data.model.education.EducationCourse
import com.liveongames.data.model.education.EducationActionDef
import com.liveongames.domain.model.EduTier
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

class EducationAssetLoader(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadCourses(): List<EducationCourse> {
        val jsonString = context.assets.open("education_courses.json").use {
            InputStreamReader(it).readText()
        }
        // Assuming the top-level JSON is a list of EducationCourse objects directly
        // If it's wrapped, e.g., { "courses": [...] }, adjust this line.
        return json.decodeFromString(jsonString)
    }

    suspend fun loadActions(): List<EducationActionDef> {
        val jsonString = context.assets.open("education_actions.json").use {
            InputStreamReader(it).readText()
        }
        // Same note: adjust parsing if JSON structure is wrapped.
        return json.decodeFromString(jsonString)
    }
}

// --- OPTIONAL: Helper to convert String to EduTier if needed ---
fun String.toEduTier(): EduTier = EduTier.valueOf(this.uppercase())