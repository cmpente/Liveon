// app/src/main/java/com/liveongames/liveon/ui/screens/EducationScreen.kt
package com.liveongames.liveon.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.domain.model.Education
import com.liveongames.liveon.ui.theme.AllGameThemes
import com.liveongames.liveon.viewmodel.EducationViewModel
import com.liveongames.liveon.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import java.util.*

@Composable
fun EducationScreen(
    viewModel: EducationViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onEducationCompleted: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val educations by viewModel.availableEducation.collectAsState()
    val playerGPA by viewModel.playerGPA.collectAsState()
    val currentEducation by viewModel.currentEducation.collectAsState()
    val selectedThemeIndex by settingsViewModel.selectedThemeIndex.collectAsState()
    val currentTheme = AllGameThemes.getOrElse(selectedThemeIndex) { AllGameThemes[0] }
    var showEnrollDialog by remember { mutableStateOf<EducationDialogData?>(null) }
    var showEducationResult by remember { mutableStateOf<EducationResult?>(null) }
    var pendingEducationId by remember { mutableStateOf<String?>(null) }

    Log.d("EducationScreen", "EducationScreen recomposed, educations count: ${educations.size}")

    // Place this LaunchedEffect right after your variable declarations:
    LaunchedEffect(educations) {
        if (pendingEducationId != null && educations.isNotEmpty()) {
            // Find the education we just enrolled in (match by id and recent timestamp)
            val recentEducations = educations.filter {
                System.currentTimeMillis() - it.enrollmentTimestamp < 5000 // Within last 5 seconds
            }

            val matchingEducation = recentEducations.find { education ->
                education.id == pendingEducationId
            }

            if (matchingEducation != null) {
                showEducationResult = EducationResult(
                    title = "Enrollment Complete!",
                    description = "You've successfully enrolled in ${matchingEducation.name}",
                    duration = matchingEducation.duration,
                    cost = matchingEducation.cost,
                    skillIncrease = matchingEducation.skillIncrease
                )
                pendingEducationId = null
            }
        }
    }

    // Full screen modal overlay for education screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onDismiss() } // Tap outside to close
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .background(currentTheme.surface, RoundedCornerShape(20.dp))
                .clickable(enabled = false) { } // Prevent click-through
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Education Center",
                        style = MaterialTheme.typography.headlineMedium,
                        color = currentTheme.text,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                            contentDescription = "Close",
                            tint = currentTheme.text
                        )
                    }
                }

                // Player stats bar
                Card(
                    colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Current Education: ${getCurrentEducationName(currentEducation)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = currentTheme.text,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "GPA: ${"%.2f".format(playerGPA)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (playerGPA >= 3.0) Color(0xFF4CAF50) else if (playerGPA >= 2.0) Color(0xFFFFEB3B) else Color(0xFFF44336)
                            )
                        }
                    }
                }

                // Scrollable content area
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Available Education Programs
                    Text(
                        text = "Available Programs",
                        style = MaterialTheme.typography.titleMedium,
                        color = currentTheme.text,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            if (educations.isEmpty()) {
                                Text(
                                    text = "✨ No education programs available at this time ✨",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            } else {
                                educations.forEach { education ->
                                    EducationButton(
                                        education = education,
                                        playerGPA = playerGPA,
                                        currentEducation = currentEducation,
                                        theme = currentTheme,
                                        onEnrollSelected = { edu ->
                                            showEnrollDialog = EducationDialogData(
                                                id = edu.id,
                                                name = edu.name,
                                                description = edu.description,
                                                cost = edu.cost,
                                                duration = edu.duration,
                                                minimumAge = edu.minimumAge,
                                                skillIncrease = edu.skillIncrease
                                            )
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }

                    // Current Education Progress
                    if (currentEducation != "none") {
                        Text(
                            text = "Current Education Progress",
                            style = MaterialTheme.typography.titleMedium,
                            color = currentTheme.text,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Currently enrolled in ${getCurrentEducationName(currentEducation)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = currentTheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                // Add progress info here when available
                            }
                        }
                    }
                }
            }
        }
    }

    // Education Confirmation Dialog
    showEnrollDialog?.let { dialogData ->
        AlertDialog(
            onDismissRequest = { showEnrollDialog = null },
            containerColor = currentTheme.surface,
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_compass),
                        contentDescription = null,
                        tint = currentTheme.primary,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = dialogData.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = currentTheme.text,
                        fontWeight = FontWeight.Bold
                    )

                    // Education badge
                    Text(
                        text = "${dialogData.duration} years",
                        style = MaterialTheme.typography.bodyMedium,
                        color = currentTheme.text,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(
                                currentTheme.primary.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = dialogData.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = currentTheme.accent
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "💰 Cost: $${dialogData.cost}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (dialogData.cost > 0) Color(0xFFF44336) else Color(0xFF4CAF50)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "📈 Skill Increase: +${dialogData.skillIncrease}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "This action cannot be undone. Proceed with enrollment?",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9800)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingEducationId = dialogData.id
                        viewModel.enrollInEducation(dialogData.id)
                        showEnrollDialog = null
                        // Show result after a delay to allow DB update
                        CoroutineScope(Dispatchers.Main).launch {
                            kotlinx.coroutines.delay(1500)
                            onEducationCompleted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = currentTheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Enroll Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEnrollDialog = null },
                    border = BorderStroke(1.dp, currentTheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = currentTheme.primary
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Education Result Dialog
    showEducationResult?.let { result ->
        AlertDialog(
            onDismissRequest = { showEducationResult = null },
            containerColor = currentTheme.surface,
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_save),
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = result.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = currentTheme.accent,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "⏱️ Duration: ${result.duration} years",
                        style = MaterialTheme.typography.titleMedium,
                        color = currentTheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    if (result.cost > 0) {
                        Text(
                            text = "💸 Cost: $${result.cost}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Text(
                        text = "📈 Skill Increase: +${result.skillIncrease}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showEducationResult = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// Data classes
data class EducationDialogData(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val duration: Int,
    val minimumAge: Int,
    val skillIncrease: Int
)

data class EducationResult(
    val title: String,
    val description: String,
    val duration: Int,
    val cost: Int,
    val skillIncrease: Int
)

@Composable
fun EducationButton(
    education: Education,
    playerGPA: Double,
    currentEducation: String,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    onEnrollSelected: (Education) -> Unit = {}
) {
    var isPressed by remember { mutableStateOf(false) }
    val canEnroll = canEnrollInEducation(education, playerGPA, currentEducation)
    val isLocked = !canEnroll

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100), label = ""
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isPressed) 1.3f else 1f,
        animationSpec = tween(durationMillis = 100), label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = canEnroll,
                onClick = {
                    isPressed = true
                    onEnrollSelected(education)
                    CoroutineScope(Dispatchers.Main).launch {
                        kotlinx.coroutines.delay(150)
                        isPressed = false
                    }
                }
            )
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 2.dp else 6.dp
        )
    ) {
        // Create the background with proper type handling
        val backgroundColor = if (isLocked) {
            theme.surface.copy(alpha = 0.5f)
        } else {
            theme.surface
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = backgroundColor, shape = RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = education.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isLocked) theme.accent else theme.text,
                            fontWeight = FontWeight.Medium
                        )
                        if (isLocked) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_lock_idle_lock),
                                contentDescription = "Locked",
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(start = 4.dp),
                                tint = Color.Red
                            )
                        }
                    }
                    Text(
                        text = education.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isLocked) theme.accent.copy(alpha = 0.5f) else theme.accent
                    )

                    // Duration badge
                    Text(
                        text = "${education.duration} years",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(
                                theme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (education.cost > 0) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = theme.secondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "$${education.cost}",
                                style = MaterialTheme.typography.bodySmall,
                                color = theme.background,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = theme.accent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "FREE",
                                style = MaterialTheme.typography.bodySmall,
                                color = theme.background,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_compass),
                        contentDescription = null,
                        tint = if (canEnroll) theme.primary else theme.accent.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                    )
                }
            }
        }
    }
}

fun canEnrollInEducation(education: Education, playerGPA: Double, currentEducation: String): Boolean {
    // Check GPA requirement
    if (education.minimumGPA > 0 && playerGPA < education.minimumGPA) {
        return false
    }

    // Check prerequisites
    return if (education.prerequisites.isEmpty()) {
        true
    } else {
        education.prerequisites.contains(currentEducation)
    }
}

fun getCurrentEducationName(educationId: String): String {
    return when (educationId) {
        "grade_school" -> "Grade School"
        "middle_school" -> "Middle School"
        "high_school" -> "High School"
        "community_college" -> "Community College"
        "university" -> "University"
        "graduate_school" -> "Graduate School"
        "phd_program" -> "PhD Program"
        "medical_school" -> "Medical School"
        "law_school" -> "Law School"
        "business_school" -> "Business School"
        else -> "None"
    }
}