// app/src/main/java/com/liveongames/liveon/ui/screens/EducationScreen.kt
package com.liveongames.liveon.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.domain.model.Education
import com.liveongames.domain.model.EducationLevel
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.AllGameThemes
import com.liveongames.liveon.viewmodel.EducationViewModel
import com.liveongames.liveon.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@Composable
fun EducationScreen(
    viewModel: EducationViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onEducationCompleted: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val educations by viewModel.educations.collectAsState()
    val selectedThemeIndex by settingsViewModel.selectedThemeIndex.collectAsState()
    val currentTheme = AllGameThemes.getOrElse(selectedThemeIndex) { AllGameThemes[0] }
    val playerGPA = 3.2 // This should come from your character data
    var showEnrollDialog by remember { mutableStateOf<Education?>(null) }
    var showCompletionDialog by remember { mutableStateOf<Education?>(null) }
    var showActiveEducationDialog by remember { mutableStateOf<Education?>(null) }
    var activeEducation by remember { mutableStateOf<Education?>(null) }

    // Find the currently active education (non-certification)
    LaunchedEffect(educations) {
        activeEducation = educations.find { it.isActive && it.level != EducationLevel.CERTIFICATION }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onDismiss() }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .background(currentTheme.surface, RoundedCornerShape(20.dp))
                .clickable(enabled = false) { }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Education",
                        style = MaterialTheme.typography.headlineMedium,
                        color = currentTheme.text,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                            contentDescription = "Close",
                            tint = currentTheme.text
                        )
                    }
                }

                // Active Education Header
                activeEducation?.let { education ->
                    ActiveEducationHeader(
                        education = education,
                        theme = currentTheme,
                        onViewDetails = { showActiveEducationDialog = education }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Elementary School
                    EducationCategorySection(
                        title = "Elementary School",
                        description = "Foundational learning for young minds",
                        educations = getElementaryEducations(),
                        theme = currentTheme,
                        playerGPA = playerGPA,
                        onEnroll = { education -> showEnrollDialog = education },
                        isActiveEducation = activeEducation != null
                    )

                    // Middle School
                    EducationCategorySection(
                        title = "Middle School",
                        description = "Transition period and skill development",
                        educations = getMiddleSchoolEducations(),
                        theme = currentTheme,
                        playerGPA = playerGPA,
                        onEnroll = { education -> showEnrollDialog = education },
                        isActiveEducation = activeEducation != null
                    )

                    // High School
                    EducationCategorySection(
                        title = "High School",
                        description = "Secondary education and preparation",
                        educations = getHighSchoolEducations(),
                        theme = currentTheme,
                        playerGPA = playerGPA,
                        onEnroll = { education -> showEnrollDialog = education },
                        isActiveEducation = activeEducation != null
                    )

                    // College Degrees
                    EducationCategorySection(
                        title = "College Degrees",
                        description = "Higher education opportunities",
                        educations = getCollegeEducations(),
                        theme = currentTheme,
                        playerGPA = playerGPA,
                        onEnroll = { education -> showEnrollDialog = education },
                        isActiveEducation = activeEducation != null
                    )

                    // Professional Certifications
                    EducationCategorySection(
                        title = "Professional Certifications",
                        description = "Specialized skill development",
                        educations = getCertificationEducations(),
                        theme = currentTheme,
                        playerGPA = playerGPA,
                        onEnroll = { education -> showEnrollDialog = education },
                        isActiveEducation = false // Certifications can be taken alongside other education
                    )

                    Text(
                        text = "Diplomas, Certifications, and Licenses",
                        style = MaterialTheme.typography.titleMedium,
                        color = currentTheme.text,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
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
                            if (educations.isEmpty()) {
                                Text(
                                    text = "📚 No earned credentials yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                educations.forEachIndexed { index, education ->
                                    EducationStatusEntry(
                                        education = education,
                                        theme = currentTheme,
                                        onComplete = { showCompletionDialog = education },
                                        onManage = { showActiveEducationDialog = education }
                                    )
                                    if (index < educations.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            thickness = 1.dp,
                                            color = currentTheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    showEnrollDialog?.let { education ->
        EnrollmentDialog(
            education = education,
            playerGPA = playerGPA,
            theme = currentTheme,
            onEnroll = {
                viewModel.enrollInEducation(education)
                showEnrollDialog = null
            },
            onDismiss = { showEnrollDialog = null }
        )
    }

    showCompletionDialog?.let { education ->
        CompletionDialog(
            education = education,
            theme = currentTheme,
            onComplete = {
                viewModel.completeEducation(education)
                showCompletionDialog = null
                onEducationCompleted()
            },
            onDismiss = { showCompletionDialog = null }
        )
    }

    showActiveEducationDialog?.let { education ->
        ActiveEducationDialog(
            education = education,
            theme = currentTheme,
            onAttendClass = { viewModel.attendClass(education.id) },
            onDoHomework = { viewModel.doHomework(education.id) },
            onStudy = { viewModel.study(education.id) },
            onDismiss = { showActiveEducationDialog = null }
        )
    }
}

@Composable
fun ActiveEducationHeader(
    education: Education,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    onViewDetails: () -> Unit
) {
    val format = DecimalFormat("0.00")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = theme.primary.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Currently Enrolled",
                    style = MaterialTheme.typography.titleMedium,
                    color = theme.primary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onViewDetails) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_continue),
                        contentDescription = "View Details",
                        tint = theme.primary
                    )
                }
            }

            Text(
                text = education.name,
                style = MaterialTheme.typography.bodyLarge,
                color = theme.text,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "GPA: ${format.format(education.currentGPA)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.primary
                )
                Text(
                    text = "Years Remaining: ${education.duration}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.accent
                )
            }
        }
    }
}

@Composable
fun ActiveEducationDialog(
    education: Education,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    onAttendClass: () -> Unit,
    onDoHomework: () -> Unit,
    onStudy: () -> Unit,
    onDismiss: () -> Unit
) {
    val format = DecimalFormat("0.00")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .background(theme.surface)
                .align(Alignment.BottomCenter)
        ) {
            // Header with close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = education.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = theme.text,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDismiss
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = "Close",
                        tint = theme.text
                    )
                }
            }

            // Current stats display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_education),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = theme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Current Stats",
                            style = MaterialTheme.typography.titleSmall,
                            color = theme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CompactEducationStat("GPA", format.format(education.currentGPA), R.drawable.ic_brain, theme.primary, theme)
                            CompactEducationStat("Years Remaining", education.duration.toString(), R.drawable.ic_yearbook, theme.primary, theme)
                        }
                    }
                }
            }

            // Menu options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Education Options:",
                    style = MaterialTheme.typography.titleMedium,
                    color = theme.text,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val menuItems = listOf(
                    MenuItemData(
                        iconResId = R.drawable.ic_people,
                        title = "Attend Class",
                        description = "Participate in lectures to improve your GPA",
                        theme = theme
                    ) {
                        onAttendClass()
                    },
                    MenuItemData(
                        iconResId = android.R.drawable.ic_menu_manage,
                        title = "Do Homework",
                        description = "Complete assignments to boost your academic performance",
                        theme = theme
                    ) {
                        onDoHomework()
                    },
                    MenuItemData(
                        iconResId = R.drawable.ic_brain,
                        title = "Study",
                        description = "Dedicate time to learning and retention",
                        theme = theme
                    ) {
                        onStudy()
                    }
                )

                LazyColumn(
                    modifier = Modifier.weight(1.0f)
                ) {
                    items(menuItems) { item ->
                        MenuItemRow(item = item)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 3.dp),
                            thickness = 1.dp,
                            color = theme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactEducationStat(
    label: String,
    value: String,
    iconId: Int,
    color: androidx.compose.ui.graphics.Color,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme
) {
    Row(
        modifier = Modifier
            .width(150.dp)
            .padding(horizontal = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = theme.text
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = theme.accent,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

data class MenuItemData(
    val iconResId: Int,
    val title: String,
    val description: String,
    val theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    val onClick: () -> Unit
)

@Composable
fun MenuItemRow(item: MenuItemData) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(vertical = 5.dp),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = item.theme.primary.copy(alpha = 0.1f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = item.iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = item.theme.primary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = item.theme.text,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = item.theme.accent
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_continue),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = item.theme.accent
            )
        }
    }
}

@Composable
fun EducationCategorySection(
    title: String,
    description: String,
    educations: List<Education>,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    playerGPA: Double,
    onEnroll: (Education) -> Unit = {},
    isActiveEducation: Boolean // To prevent enrolling in multiple non-cert programs
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = theme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = theme.accent,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            educations.forEach { education ->
                EducationButton(
                    education = education,
                    onClick = { onEnroll(education) },
                    theme = theme,
                    playerGPA = playerGPA,
                    isDisabled = isActiveEducation && education.level != EducationLevel.CERTIFICATION
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun EducationButton(
    education: Education,
    onClick: () -> Unit,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    playerGPA: Double,
    isDisabled: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    val meetsRequirements = education.requiredGPA <= playerGPA
    val isClickable = meetsRequirements && !isDisabled
    val format = DecimalFormat("0.00")

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = isClickable,
                onClick = {
                    isPressed = true
                    onClick()
                    CoroutineScope(Dispatchers.Main).launch {
                        kotlinx.coroutines.delay(150)
                        isPressed = false
                    }
                }
            )
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .alpha(if (isClickable) 1f else 0.5f),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 2.dp else 6.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isClickable) theme.surface else theme.surface.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = education.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isClickable) theme.text else theme.accent,
                        fontWeight = FontWeight.Medium
                    )

                    if (!meetsRequirements) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_lock_idle_lock),
                            contentDescription = "Locked",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Red
                        )
                    } else if (isDisabled) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_lock_idle_lock),
                            contentDescription = "Disabled",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                    }
                }

                Text(
                    text = education.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isClickable) theme.accent else theme.accent.copy(alpha = 0.5f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "💰 $${education.cost}",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.primary
                    )
                    Text(
                        text = "⏰ ${education.duration} years",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.primary
                    )
                }

                if (education.requiredGPA > 0) {
                    Text(
                        text = "🎓 Required GPA: ${format.format(education.requiredGPA)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (meetsRequirements) Color(0xFF4CAF50) else Color.Red,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (isDisabled) {
                    Text(
                        text = "Finish current education first",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EducationStatusEntry(
    education: Education,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    onComplete: (Education) -> Unit,
    onManage: (Education) -> Unit
) {
    val format = DecimalFormat("0.00")

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = education.name,
                style = MaterialTheme.typography.bodyMedium,
                color = theme.text,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (education.completionDate != null) "Completed" else "In Progress",
                style = MaterialTheme.typography.bodySmall,
                color = if (education.completionDate != null) Color(0xFF4CAF50) else Color(0xFFFF9800)
            )
        }

        Text(
            text = education.level.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = theme.accent
        )

        if (education.completionDate != null) {
            Text(
                text = "Completed GPA: ${format.format(education.currentGPA)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun EnrollmentDialog(
    education: Education,
    playerGPA: Double,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    onEnroll: () -> Unit,
    onDismiss: () -> Unit
) {
    val format = DecimalFormat("0.00")
    val meetsRequirements = education.requiredGPA <= playerGPA

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.surface,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_info_details),
                    contentDescription = null,
                    tint = theme.primary,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = education.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = theme.text,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = education.level.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.accent,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = education.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.accent
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "💰 Cost: $${education.cost}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.primary
                    )
                    Text(
                        text = "⏰ Duration: ${education.duration} years",
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (education.requiredGPA > 0) {
                    Text(
                        text = "🎓 Required GPA: ${format.format(education.requiredGPA)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (meetsRequirements) Color(0xFF4CAF50) else Color.Red
                    )
                    Text(
                        text = "Your GPA: ${format.format(playerGPA)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.accent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = if (meetsRequirements)
                        "You meet all requirements for enrollment!"
                    else
                        "You don't meet the GPA requirements for this program.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (meetsRequirements) Color(0xFF4CAF50) else Color.Red
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (meetsRequirements) {
                        onEnroll()
                    } else {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (meetsRequirements) theme.primary else Color.Gray,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = meetsRequirements
            ) {
                Text(
                    text = if (meetsRequirements) "ENROLL NOW" else "REQUIREMENTS NOT MET",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, theme.primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = theme.primary
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CompletionDialog(
    education: Education,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    onComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    val format = DecimalFormat("0.00")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.surface,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_save),
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "🎓 Education Completed!",
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
                    text = "Congratulations on completing ${education.name}!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.accent,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Final GPA: ${format.format(education.currentGPA)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "You've gained valuable knowledge and skills!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onComplete,
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

// Helper functions for education data with realistic durations
fun getElementaryEducations(): List<Education> {
    return listOf(
        Education(
            id = "elem_basic",
            name = "Elementary School",
            description = "Foundational education covering reading, writing, and arithmetic",
            level = EducationLevel.BASIC,
            cost = 0,
            duration = 6, // 6 years
            requiredGPA = 0.0
        )
    )
}

fun getMiddleSchoolEducations(): List<Education> {
    return listOf(
        Education(
            id = "middle_basic",
            name = "Middle School",
            description = "Transition period focusing on core subjects and study skills",
            level = EducationLevel.BASIC,
            cost = 200,
            duration = 3, // 3 years
            requiredGPA = 1.0
        )
    )
}

fun getHighSchoolEducations(): List<Education> {
    return listOf(
        Education(
            id = "hs_basic",
            name = "High School",
            description = "General high school education covering core subjects",
            level = EducationLevel.HIGH_SCHOOL,
            cost = 500,
            duration = 4, // 4 years (realistic)
            requiredGPA = 0.0
        )
    )
}

fun getCollegeEducations(): List<Education> {
    return listOf(
        Education(
            id = "college_associate",
            name = "Community College",
            description = "Associate degree program focusing on foundational knowledge",
            level = EducationLevel.ASSOCIATE,
            cost = 2000,
            duration = 2, // 2 years
            requiredGPA = 2.0
        ),
        Education(
            id = "college_bachelor",
            name = "University",
            description = "Bachelor's degree program in your chosen major",
            level = EducationLevel.BACHELOR,
            cost = 8000,
            duration = 4, // 4 years
            requiredGPA = 2.5
        ),
        Education(
            id = "college_master",
            name = "Graduate School",
            description = "Master's degree program for advanced specialization",
            level = EducationLevel.MASTER,
            cost = 15000,
            duration = 2, // 2 years
            requiredGPA = 3.0
        )
    )
}

fun getCertificationEducations(): List<Education> {
    return listOf(
        Education(
            id = "cert_tech",
            name = "Technical Certification",
            description = "Certification in computer technology and programming",
            level = EducationLevel.CERTIFICATION,
            cost = 1000,
            duration = 1, // 1 year
            requiredGPA = 1.5
        ),
        Education(
            id = "cert_business",
            name = "Business Certificate",
            description = "Certificate program in business management",
            level = EducationLevel.CERTIFICATION,
            cost = 1500,
            duration = 1, // 1 year
            requiredGPA = 2.0
        ),
        Education(
            id = "cert_health",
            name = "Healthcare Certification",
            description = "Certification in healthcare services",
            level = EducationLevel.CERTIFICATION,
            cost = 2000,
            duration = 1, // 1 year
            requiredGPA = 2.2
        )
    )
}