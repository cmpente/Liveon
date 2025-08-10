// app/src/main/java/com/liveongames/liveon/ui/screens/EducationScreen.kt
package com.liveongames.liveon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.domain.model.Education
import com.liveongames.domain.model.EducationLevel
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.screens.education.*
import com.liveongames.liveon.ui.theme.AllGameThemes
import com.liveongames.liveon.ui.theme.LiveonTheme
import com.liveongames.liveon.viewmodel.EducationViewModel
import com.liveongames.liveon.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EducationScreen(
    viewModel: EducationViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onEducationCompleted: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val educations by viewModel.educations.collectAsState()
    val selectedThemeIndex by settingsViewModel.selectedThemeIndex.collectAsState()
    val theme: LiveonTheme = AllGameThemes.getOrElse(selectedThemeIndex) { AllGameThemes[0] }

    // Derive GPA: prefer currently active non-cert education; fallback 2.0
    val activeEducation = remember(educations) { educations.find { it.isActive && it.level != EducationLevel.CERTIFICATION } }
    val playerGPA = activeEducation?.currentGPA ?: 2.0

    // Completions set for tier unlock rules
    val completedIds = remember(educations) {
        educations.filter { it.completionDate != null }.map { it.id }.toSet()
    }

    // UI state
    var selectedCourse by remember { mutableStateOf<Education?>(null) }
    var showGpaInfo by remember { mutableStateOf(false) }
    var showNextTierInfo by remember { mutableStateOf(false) }
    var showCertificateDetails by remember { mutableStateOf<Education?>(null) }

    // Build tiers with realistic costs/durations and proper unlocks
    val tiers = remember(playerGPA, completedIds) { buildTiers(playerGPA, completedIds) }

    val highestTierLabel = remember(tiers) { tiers.lastOrNull { it.unlocked }?.name ?: "Basic" }

    // ---------- Scrim + content (no click-through) ----------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
    ) {
        // Scrim captures only outside taps
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    onClick = onDismiss,
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                )
        )

        // Modal content
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(20.dp))
                .background(theme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Education",
                        style = MaterialTheme.typography.headlineMedium,
                        color = theme.text,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                            contentDescription = "Close",
                            tint = theme.text
                        )
                    }
                }

                // Active Education header (optional)
                activeEducation?.let { edu ->
                    ActiveEducationHeader(
                        education = edu,
                        theme = theme,
                        onViewDetails = { /* hook up your existing dialog if desired */ }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Profile card
                EducationProfileCard(
                    playerName = "Player",
                    highestTier = highestTierLabel,
                    currentGPA = playerGPA,
                    milestone = activeEducation?.let { "Currently Enrolled: ${it.name}" } ?: "No active education",
                    theme = theme,
                    onCrestTap = { showNextTierInfo = true },
                    onGPATap = { showGpaInfo = true }
                )

                Spacer(Modifier.height(12.dp))

                // Path map (tiers)
                EducationPathMap(
                    tiers = tiers,
                    theme = theme,
                    onCourseSelected = { selectedCourse = it }
                )

                Spacer(Modifier.height(16.dp))

                // Certificates shelf
                EducationAchievementsShelf(
                    certificates = educations.filter { it.completionDate != null },
                    theme = theme,
                    onCertificateSelected = { showCertificateDetails = it }
                )
            }
        }
    }

    // ---------- Dialogs ----------

    // Course details
    selectedCourse?.let { course ->
        CourseDetailsPanel(
            course = course,
            theme = theme,
            onEnroll = {
                viewModel.enrollInEducation(course)
                selectedCourse = null
            },
            onContinue = { selectedCourse = null },
            onDropOut = { /* optional: implement */ selectedCourse = null },
            onDismiss = { selectedCourse = null }
        )
    }

    // GPA Info (minute cooldown + 3-tap zero return)
    if (showGpaInfo) {
        GpaInfoDialog(theme = theme) { showGpaInfo = false }
    }

    // Tier info (actual unlock rules)
    if (showNextTierInfo) {
        AlertDialog(
            onDismissRequest = { showNextTierInfo = false },
            title = {
                Text("Tier Unlock Requirements", style = MaterialTheme.typography.headlineSmall, color = theme.primary)
            },
            text = {
                Text(
                    "• Tier 1 – Basic: Always available\n" +
                            "• Tier 2 – Intermediate: Complete Tier 1 (e.g., High School)\n" +
                            "• Tier 3 – Advanced: Complete Tier 2 and GPA ≥ 2.5\n" +
                            "• Tier 4 – Elite: Complete Tier 3 and GPA ≥ 3.0",
                    color = theme.text
                )
            },
            confirmButton = {
                TextButton(onClick = { showNextTierInfo = false }) { Text("OK", color = theme.primary) }
            },
            containerColor = theme.surface
        )
    }

    // Certificate details
    showCertificateDetails?.let { certificate ->
        val dateStr = certificate.completionDate?.let {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
        } ?: "—"

        AlertDialog(
            onDismissRequest = { showCertificateDetails = null },
            title = { Text(certificate.name, style = MaterialTheme.typography.headlineSmall, color = theme.primary) },
            text = {
                Column {
                    Text("Completed on: $dateStr", color = theme.text)
                    Spacer(Modifier.height(6.dp))
                    Text("Final GPA: ${"%.2f".format(certificate.currentGPA)}", color = theme.text)
                }
            },
            confirmButton = {
                TextButton(onClick = { showCertificateDetails = null }) { Text("OK", color = theme.primary) }
            },
            containerColor = theme.surface
        )
    }
}

/**
 * Build 4 tiers with realistic costs/durations and unlock rules:
 * - Tier 1 (Basic): Always unlocked
 * - Tier 2 (Intermediate): Requires Tier 1 completion
 * - Tier 3 (Advanced): Requires Tier 2 completion + GPA ≥ 2.5
 * - Tier 4 (Elite): Requires Tier 3 completion + GPA ≥ 3.0
 */
private fun buildTiers(currentGpa: Double, completedIds: Set<String>): List<EducationTier> {
    // Tier 1
    val hs = Education(
        id = "hs_diploma",
        name = "High School Diploma",
        description = "Complete your secondary education and meet graduation requirements.",
        level = EducationLevel.HIGH_SCHOOL,
        cost = 0,
        duration = 48, // months
        requiredGPA = 0.0
    )

    // Tier 2
    val trade = Education(
        id = "trade_cert",
        name = "Trade School Certificate",
        description = "Hands-on training for skilled careers (e.g., HVAC, Welding, LPN).",
        level = EducationLevel.CERTIFICATION,
        cost = 15000, // realistic midpoint ($5k–$25k)
        duration = 18, // months
        requiredGPA = 1.8
    )
    val cc = Education(
        id = "cc_associate",
        name = "Community College AA/AS",
        description = "Two-year associate degree and transfer preparation.",
        level = EducationLevel.ASSOCIATE,
        cost = 10000, // total (≈$8k–$12k)
        duration = 24, // months
        requiredGPA = 2.0
    )

    // Tier 3
    val ba = Education(
        id = "ba_public",
        name = "Bachelor's Degree",
        description = "Undergraduate study at a public university.",
        level = EducationLevel.BACHELOR,
        cost = 42000, // total (≈$36k–$45k)
        duration = 48, // months
        requiredGPA = 2.5
    )
    val profDip = Education(
        id = "prof_diploma",
        name = "Professional Diploma",
        description = "Targeted, job-ready credential.",
        level = EducationLevel.CERTIFICATION,
        cost = 15000, // ≈$10k–$20k
        duration = 12, // months
        requiredGPA = 2.7
    )

    // Tier 4
    val master = Education(
        id = "masters",
        name = "Master's Degree",
        description = "Advanced specialization and research (≈2 years).",
        level = EducationLevel.MASTER,
        cost = 62000, // ≈$45k–$70k
        duration = 24, // months
        requiredGPA = 3.0
    )
    val phd = Education(
        id = "phd_research",
        name = "Doctor of Philosophy (PhD)",
        description = "Funded research with dissertation; stipend may apply.",
        level = EducationLevel.MASTER, // keep MASTER unless you add DOCTORATE
        cost = 0, // tuition often waived during funded years
        duration = 60, // months
        requiredGPA = 3.3
    )

    // Unlock checks
    val tier1Unlocked = true
    val tier2Unlocked = completedIds.contains(hs.id)
    val tier3Unlocked = (completedIds.contains(trade.id) || completedIds.contains(cc.id)) && currentGpa >= 2.5
    val tier4Unlocked = (completedIds.contains(ba.id) || completedIds.contains(profDip.id)) && currentGpa >= 3.0

    return listOf(
        EducationTier(
            name = "Basic Education",
            description = "Foundational skills and knowledge.",
            icon = R.drawable.ic_school,
            unlocked = tier1Unlocked,
            courses = listOf(hs)
        ),
        EducationTier(
            name = "Intermediate",
            description = "Specialized training and certifications.",
            icon = R.drawable.ic_graduate, // use existing icon
            unlocked = tier2Unlocked,
            courses = listOf(trade, cc)
        ),
        EducationTier(
            name = "Advanced",
            description = "University-level education and professional programs.",
            icon = R.drawable.ic_university,
            unlocked = tier3Unlocked,
            courses = listOf(ba, profDip)
        ),
        EducationTier(
            name = "Elite",
            description = "Highest levels of academic achievement.",
            icon = R.drawable.ic_graduate, // fallback crest
            unlocked = tier4Unlocked,
            courses = listOf(master, phd)
        )
    )
}

@Composable
fun ActiveEducationHeader(
    education: Education,
    theme: LiveonTheme,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = theme.primary.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Currently Enrolled", style = MaterialTheme.typography.titleMedium, color = theme.primary, fontWeight = FontWeight.Bold)
                IconButton(onClick = onViewDetails) {
                    Icon(painter = painterResource(id = R.drawable.ic_continue), contentDescription = "View Details", tint = theme.primary)
                }
            }
            Text(education.name, style = MaterialTheme.typography.bodyLarge, color = theme.text, fontWeight = FontWeight.Medium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("GPA: ${"%.2f".format(education.currentGPA)}", style = MaterialTheme.typography.bodyMedium, color = theme.primary)
                Text("Time Remaining: ${monthsToNiceString(education.duration)}", style = MaterialTheme.typography.bodyMedium, color = theme.accent)
            }
        }
    }
}
