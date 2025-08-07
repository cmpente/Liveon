// app/src/main/java/com/liveongames/liveon/ui/screens/CrimeScreen.kt
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
import com.liveongames.domain.model.CrimeType
import com.liveongames.liveon.ui.theme.AllGameThemes
import com.liveongames.liveon.viewmodel.CrimeViewModel
import com.liveongames.liveon.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlin.random.Random

@Composable
fun CrimeScreen(
    viewModel: CrimeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onCrimeCommitted: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val crimes by viewModel.crimes.collectAsState()
    val selectedThemeIndex by settingsViewModel.selectedThemeIndex.collectAsState()
    val currentTheme = AllGameThemes.getOrElse(selectedThemeIndex) { AllGameThemes[0] }
    var showClearDialog by remember { mutableStateOf(false) }
    var showCrimeDialog by remember { mutableStateOf<CrimeDialogData?>(null) }
    var showCrimeResult by remember { mutableStateOf<CrimeResult?>(null) }

    Log.d("CrimeScreen", "CrimeScreen recomposed, crimes count: ${crimes.size}")

    // Full screen modal overlay for crime screen
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
                        text = "Criminal Activities",
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

                // Scrollable content area
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Crime Categories with descriptions
                    CrimeCategorySection(
                        title = " Petty Crimes",
                        description = "Low risk, low reward offenses",
                        crimes = listOf(
                            CrimeType.THEFT to "Quick grab for cash (+$100-$1000)",
                            CrimeType.VANDALISM to "Damage property for fun (+$50-$500)"
                        ),
                        viewModel = viewModel,
                        theme = currentTheme,
                        onCrimeSelected = { crimeType ->
                            showCrimeDialog = CrimeDialogData(
                                type = crimeType,
                                title = getCrimeDialogTitle(crimeType),
                                message = getCrimeDialogMessage(crimeType),
                                confirmText = "Proceed",
                                cancelText = "Nevermind"
                            )
                        }
                    )

                    CrimeCategorySection(
                        title = " Serious Crimes",
                        description = "Higher risk with bigger payoffs",
                        crimes = listOf(
                            CrimeType.ASSAULT to "Physical confrontation (+$200)",
                            CrimeType.FRAUD to "Deceive others for profit (+$500-$5000)",
                            CrimeType.EXTORTION to "Threaten people for money (+$200-$2000)"
                        ),
                        viewModel = viewModel,
                        theme = currentTheme,
                        onCrimeSelected = { crimeType ->
                            showCrimeDialog = CrimeDialogData(
                                type = crimeType,
                                title = getCrimeDialogTitle(crimeType),
                                message = getCrimeDialogMessage(crimeType),
                                confirmText = "Proceed",
                                cancelText = "Nevermind"
                            )
                        }
                    )

                    CrimeCategorySection(
                        title = " Drug Crimes",
                        description = "Substance-related offenses",
                        crimes = listOf(
                            CrimeType.DRUG_POSSESSION to "Possess illegal substances (+$100)",
                            CrimeType.DRUG_DEALING to "Sell drugs for big money (+$1000-$10000)"
                        ),
                        viewModel = viewModel,
                        theme = currentTheme,
                        onCrimeSelected = { crimeType ->
                            showCrimeDialog = CrimeDialogData(
                                type = crimeType,
                                title = getCrimeDialogTitle(crimeType),
                                message = getCrimeDialogMessage(crimeType),
                                confirmText = "Proceed",
                                cancelText = "Nevermind"
                            )
                        }
                    )

                    CrimeCategorySection(
                        title = "Capital Crimes",
                        description = "Extremely dangerous and high-stakes",
                        crimes = listOf(
                            CrimeType.MURDER to "Take a life - no turning back (+$5000)"
                        ),
                        viewModel = viewModel,
                        theme = currentTheme,
                        onCrimeSelected = { crimeType ->
                            showCrimeDialog = CrimeDialogData(
                                type = crimeType,
                                title = getCrimeDialogTitle(crimeType),
                                message = getCrimeDialogMessage(crimeType),
                                confirmText = "Proceed",
                                cancelText = "Nevermind"
                            )
                        }
                    )

                    // Criminal History Section
                    Text(
                        text = "Criminal History",
                        style = MaterialTheme.typography.headlineSmall,
                        color = currentTheme.text,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            if (crimes.isEmpty()) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp)
                                ) {
                                    Text(
                                        text = "✨ Clean Record ✨",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "No criminal history to display",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = currentTheme.accent,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            } else {
                                // Stats summary
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${crimes.size} Crime${if (crimes.size > 1) "s" else ""}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = currentTheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Severity: ${crimes.maxOfOrNull { it.severity } ?: 0}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = when {
                                            crimes.maxOfOrNull { it.severity } ?: 0 >= 8 -> Color.Red
                                            crimes.maxOfOrNull { it.severity } ?: 0 >= 5 -> Color(0xFFFF9800)
                                            else -> Color(0xFF4CAF50)
                                        }
                                    )
                                }

                                // Crime entries (limited to 5 most recent)
                                val recentCrimes = crimes.takeLast(5)
                                recentCrimes.forEachIndexed { index, crime ->
                                    CrimeHistoryEntry(
                                        crime = crime,
                                        theme = currentTheme,
                                        isLast = index == recentCrimes.size - 1
                                    )
                                }

                                if (crimes.size > 5) {
                                    Text(
                                        text = "...and ${crimes.size - 5} more crimes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = currentTheme.accent,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Clear Record Button
                    OutlinedButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                    ) {
                        Text("Clear Criminal Record ($5000)")
                    }
                }
            }
        }
    }

    // Enhanced Crime Confirmation Dialog
    showCrimeDialog?.let { dialogData ->
        AlertDialog(
            onDismissRequest = { showCrimeDialog = null },
            containerColor = currentTheme.surface,
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Crime icon
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = getCrimeIcon(dialogData.type)),
                        contentDescription = null,
                        tint = getCrimeSeverityColor(dialogData.type, currentTheme),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Crime title with severity indicator
                    Text(
                        text = dialogData.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = currentTheme.text,
                        fontWeight = FontWeight.Bold
                    )

                    // Severity badge
                    Text(
                        text = "Severity: ${getCrimeSeverity(dialogData.type)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = getCrimeSeverityColor(dialogData.type, currentTheme),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(
                                getCrimeSeverityColor(dialogData.type, currentTheme).copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            },
            text = {
                Column {
                    // Enhanced scenario description
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Scenario: ")
                            }
                            append(dialogData.message)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = currentTheme.accent
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Potential rewards
                    val rewardRange = getCrimeRewardRange(dialogData.type)
                    if (rewardRange.first > 0) {
                        Text(
                            text = "💰 Potential Earnings: $${rewardRange.first}-${rewardRange.second}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4CAF50)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Risk factors
                    val riskText = when (dialogData.type) {
                        CrimeType.THEFT -> "Low risk of detection"
                        CrimeType.VANDALISM -> "Moderate risk of being caught"
                        CrimeType.ASSAULT -> "High risk - physical confrontation"
                        CrimeType.FRAUD -> "Moderate risk - can leave digital traces"
                        CrimeType.DRUG_POSSESSION -> "High risk - police raids likely"
                        CrimeType.EXTORTION -> "Very high risk - serious charges"
                        CrimeType.DRUG_DEALING -> "Extremely high risk - federal charges possible"
                        CrimeType.MURDER -> "100% chance of life sentence"
                    }

                    Text(
                        text = "⚠️ Risk: $riskText",
                        style = MaterialTheme.typography.bodySmall,
                        color = when (dialogData.type) {
                            CrimeType.MURDER -> Color.Red
                            CrimeType.DRUG_DEALING, CrimeType.EXTORTION -> Color(0xFFFF9800)
                            CrimeType.DRUG_POSSESSION, CrimeType.ASSAULT -> Color(0xFFFF5722)
                            CrimeType.FRAUD -> Color(0xFFFFEB3B)
                            else -> Color(0xFF8BC34A)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Warning text
                    Text(
                        text = "This action cannot be undone. Proceed with caution.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9800)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.commitCrime(dialogData.type)
                        showCrimeDialog = null
                        CoroutineScope(Dispatchers.Main).launch {
                            kotlinx.coroutines.delay(1000)
                            val result = generateEnhancedCrimeResult(dialogData.type)
                            showCrimeResult = result
                            onCrimeCommitted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getCrimeSeverityColor(dialogData.type, currentTheme),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = dialogData.confirmText.uppercase(),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCrimeDialog = null },
                    border = BorderStroke(2.dp, currentTheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = currentTheme.primary
                    )
                ) {
                    Text(dialogData.cancelText)
                }
            }
        )
    }

    // Enhanced Crime Result Dialog
    showCrimeResult?.let { result ->
        AlertDialog(
            onDismissRequest = { showCrimeResult = null },
            containerColor = currentTheme.surface,
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Result icon
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(
                            id = if (result.isSuccess)
                                android.R.drawable.ic_menu_save
                            else
                                android.R.drawable.ic_menu_close_clear_cancel
                        ),
                        contentDescription = null,
                        tint = if (result.isSuccess) Color(0xFF4CAF50) else Color.Red,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (result.isSuccess) Color(0xFF4CAF50) else Color.Red,
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

                    if (result.moneyGained != 0) {
                        Text(
                            text = if (result.moneyGained > 0)
                                "💰 Gained $${result.moneyGained}"
                            else
                                "💸 Lost $${-result.moneyGained}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (result.moneyGained > 0) Color(0xFF4CAF50) else Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (result.wasCaught) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "🔒 YOU WERE CAUGHT!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "This crime has been added to your record",
                            style = MaterialTheme.typography.bodySmall,
                            color = currentTheme.accent
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCrimeResult = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (result.isSuccess) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Enhanced Clear Record Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = currentTheme.surface,
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_delete),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Clear Criminal Record?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = currentTheme.text,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "This will permanently erase your criminal history for $5000!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = currentTheme.accent
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (crimes.isNotEmpty()) {
                        Text(
                            text = "❌ ${crimes.size} crime${if (crimes.size > 1) "s" else ""} will be forgotten",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = "Are you sure you want to proceed?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = currentTheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearRecord()
                        showClearDialog = false
                        CoroutineScope(Dispatchers.Main).launch {
                            kotlinx.coroutines.delay(500)
                            onCrimeCommitted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    )
                ) {
                    Text("Clear Record", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearDialog = false },
                    border = BorderStroke(1.dp, currentTheme.primary)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Data classes
data class CrimeDialogData(
    val type: CrimeType,
    val title: String,
    val message: String,
    val confirmText: String,
    val cancelText: String
)

data class CrimeResult(
    val title: String,
    val description: String,
    val moneyGained: Int,
    val isSuccess: Boolean,
    val wasCaught: Boolean
)

// Enhanced UI Components
@Composable
fun CrimeCategorySection(
    title: String,
    description: String,
    crimes: List<Pair<CrimeType, String>>,
    viewModel: CrimeViewModel,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    onCrimeSelected: (CrimeType) -> Unit = {}
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

            crimes.forEach { (crimeType, crimeDescription) ->
                CrimeTypeCard(
                    crimeType = crimeType,
                    description = crimeDescription,
                    onClick = {
                        Log.d("CrimeScreen", "Crime ${crimeType.name} clicked")
                        onCrimeSelected(crimeType)
                    },
                    theme = theme
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CrimeTypeCard(
    crimeType: CrimeType,
    description: String,
    onClick: () -> Unit,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme
) {
    var isPressed by remember { mutableStateOf(false) }

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
            .clickable {
                isPressed = true
                onClick()
                CoroutineScope(Dispatchers.Main).launch {
                    kotlinx.coroutines.delay(150)
                    isPressed = false
                }
            }
            .graphicsLayer(scaleX = scale, scaleY = scale),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 2.dp else 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = crimeType.name.replace("_", " ").lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    style = MaterialTheme.typography.titleMedium,
                    color = theme.text,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.accent
                )
            }

            Icon(
                painter = androidx.compose.ui.res.painterResource(id = getCrimeIcon(crimeType)),
                contentDescription = null,
                tint = getCrimeSeverityColor(crimeType, theme),
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
            )
        }
    }
}

@Composable
fun CrimeHistoryEntry(
    crime: com.liveongames.domain.model.Crime,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    isLast: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (crime.severity >= 8)
                Color.Red.copy(alpha = 0.1f)
            else if (crime.severity >= 5)
                Color(0xFFFF9800).copy(alpha = 0.1f)
            else
                Color(0xFF4CAF50).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
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
                    text = crime.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = theme.text,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Severity ${crime.severity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = getSeverityColor(crime.severity),
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (crime.fine > 0) {
                    Text(
                        text = "Fine: $${crime.fine}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red
                    )
                }
                if (crime.jailTime > 0) {
                    Text(
                        text = "Jail: ${crime.jailTime} days",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9800)
                    )
                }
            }

            Text(
                text = crime.description,
                style = MaterialTheme.typography.bodySmall,
                color = theme.accent,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// Helper Functions
fun getCrimeSeverityColor(crimeType: CrimeType, theme: com.liveongames.liveon.ui.theme.LiveonTheme): Color {
    return when (getCrimeSeverity(crimeType)) {
        in 8..10 -> Color.Red
        in 5..7 -> Color(0xFFFF9800) // Orange
        in 3..4 -> Color(0xFFFFEB3B) // Yellow
        else -> Color(0xFF4CAF50) // Green
    }
}

fun getSeverityColor(severity: Int): Color {
    return when (severity) {
        in 8..10 -> Color.Red
        in 5..7 -> Color(0xFFFF9800) // Orange
        in 3..4 -> Color(0xFFFFEB3B) // Yellow
        else -> Color(0xFF4CAF50) // Green
    }
}

fun getCrimeSeverity(crimeType: CrimeType): Int {
    return when (crimeType) {
        CrimeType.THEFT -> 3
        CrimeType.VANDALISM -> 2
        CrimeType.ASSAULT -> 6
        CrimeType.FRAUD -> 5
        CrimeType.DRUG_POSSESSION -> 4
        CrimeType.EXTORTION -> 7
        CrimeType.DRUG_DEALING -> 8
        CrimeType.MURDER -> 10
    }
}

fun getCrimeRewardRange(crimeType: CrimeType): Pair<Int, Int> {
    return when (crimeType) {
        CrimeType.THEFT -> Pair(100, 1000)
        CrimeType.VANDALISM -> Pair(50, 500)
        CrimeType.ASSAULT -> Pair(200, 200)
        CrimeType.FRAUD -> Pair(500, 5000)
        CrimeType.DRUG_POSSESSION -> Pair(100, 100)
        CrimeType.EXTORTION -> Pair(200, 2000)
        CrimeType.DRUG_DEALING -> Pair(1000, 10000)
        CrimeType.MURDER -> Pair(5000, 5000)
    }
}

fun getCrimeIcon(crimeType: CrimeType): Int {
    return when (crimeType) {
        CrimeType.THEFT -> android.R.drawable.ic_menu_compass
        CrimeType.ASSAULT -> android.R.drawable.ic_menu_view
        CrimeType.FRAUD -> android.R.drawable.ic_menu_edit
        CrimeType.DRUG_POSSESSION -> android.R.drawable.ic_menu_preferences
        CrimeType.DRUG_DEALING -> android.R.drawable.ic_menu_upload
        CrimeType.MURDER -> android.R.drawable.ic_menu_close_clear_cancel
        CrimeType.EXTORTION -> android.R.drawable.ic_lock_power_off
        CrimeType.VANDALISM -> android.R.drawable.ic_menu_delete
    }
}

fun getCrimeDialogTitle(crimeType: CrimeType): String {
    return when (crimeType) {
        CrimeType.THEFT -> "Theft Opportunity"
        CrimeType.ASSAULT -> "Assault Target"
        CrimeType.FRAUD -> "Fraud Scheme"
        CrimeType.DRUG_POSSESSION -> "Drug Possession"
        CrimeType.DRUG_DEALING -> "Drug Dealing Operation"
        CrimeType.MURDER -> "Murder Plan"
        CrimeType.EXTORTION -> "Extortion Scheme"
        CrimeType.VANDALISM -> "Vandalize Property"
    }
}

fun getCrimeDialogMessage(crimeType: CrimeType): String {
    val victims = listOf(
        "a wealthy businessman", "an elderly woman", "a street vendor",
        "a college student", "a tourist", "a gang member", "a police officer",
        "a corrupt politician", "a rival gang leader", "an unsuspecting passerby"
    )

    val locations = listOf(
        "in a dark alley", "at the marketplace", "near the docks",
        "in the subway", "at a nightclub", "in the park", "outside a bank",
        "in a parking garage", "behind a warehouse", "on a quiet street corner"
    )

    val victim = victims.random()
    val location = locations.random()

    return when (crimeType) {
        CrimeType.THEFT -> "You spot $victim carrying a fat wallet $location. They seem distracted and vulnerable. Do you try to pickpocket them?"
        CrimeType.ASSAULT -> "You encounter $victim $location who looks weak and alone. You could assault them for their cash and belongings."
        CrimeType.FRAUD -> "You've identified $victim $location as a potential target. You could run an elaborate scam to steal their money."
        CrimeType.DRUG_POSSESSION -> "A shady dealer offers you drugs $location. The police are known to patrol this area frequently."
        CrimeType.DRUG_DEALING -> "A major buyer wants to meet $location to purchase a large shipment. This could be very profitable but extremely risky."
        CrimeType.MURDER -> "You have the perfect opportunity to eliminate $victim $location. Nobody will witness this and you can cover your tracks. This is irreversible."
        CrimeType.EXTORTION -> "You can threaten $victim $location to hand over money. They look like they have something to hide."
        CrimeType.VANDALISM -> "You see an expensive car owned by $victim $location. You could damage it for fun while they're away."
    }
}

fun generateEnhancedCrimeResult(crimeType: CrimeType): CrimeResult {
    val baseSuccessRate = when (crimeType) {
        CrimeType.THEFT -> 0.8
        CrimeType.VANDALISM -> 0.9
        CrimeType.ASSAULT -> 0.6
        CrimeType.FRAUD -> 0.7
        CrimeType.DRUG_POSSESSION -> 0.5
        CrimeType.EXTORTION -> 0.4
        CrimeType.DRUG_DEALING -> 0.3
        CrimeType.MURDER -> 0.9
    }

    val isSuccess = Random.nextDouble() < baseSuccessRate
    val wasCaught = Random.nextDouble() < (1.0 - baseSuccessRate) * 1.2 // Higher chance of being caught for high severity crimes

    val moneyGained = if (isSuccess) {
        when (crimeType) {
            CrimeType.THEFT -> Random.nextInt(100, 1001)
            CrimeType.ASSAULT -> 200
            CrimeType.FRAUD -> Random.nextInt(500, 5001)
            CrimeType.DRUG_POSSESSION -> 100
            CrimeType.DRUG_DEALING -> Random.nextInt(1000, 10001)
            CrimeType.MURDER -> 5000
            CrimeType.EXTORTION -> Random.nextInt(200, 2001)
            CrimeType.VANDALISM -> Random.nextInt(50, 501)
        }
    } else {
        0
    }

    val descriptions = when {
        isSuccess && !wasCaught -> listOf(
            "Perfect execution! Nobody saw a thing.",
            "Smooth operation, clean getaway.",
            "That went better than expected!",
            "Nailed it! Time to enjoy the spoils.",
            "Flawless! The money is now yours.",
            "Success! You're getting good at this."
        )
        isSuccess && wasCaught -> listOf(
            "You got the money but they called the cops!",
            "Success, but you left evidence behind.",
            "Good haul, but you're probably on camera.",
            "They got a good look at you - better lay low.",
            "The money is yours but at what cost?",
            "You pulled it off, but witnesses saw everything!"
        )
        !isSuccess && !wasCaught -> listOf(
            "You messed up but at least nobody noticed.",
            "Failed attempt, but no witnesses.",
            "That didn't work out, luckily nobody saw.",
            "Operation botched, but you escaped cleanly.",
            "Everything went wrong, but no one saw.",
            "Failed miserably, but managed to escape."
        )
        else -> listOf(
            "Complete failure and you got caught!",
            "Everything went wrong and they saw your face!",
            "Disaster! You failed and got arrested.",
            "Mission failed! Police are on their way.",
            "You failed and left clear evidence behind.",
            "Catastrophe! Witnesses saw everything!"
        )
    }

    val resultTitle = when {
        isSuccess && !wasCaught -> "Success!"
        isSuccess && wasCaught -> "Partial Success"
        !isSuccess && !wasCaught -> "Operation Failed"
        else -> "Caught!"
    }

    return CrimeResult(
        title = resultTitle,
        description = descriptions.random(),
        moneyGained = if (isSuccess) moneyGained else 0,
        isSuccess = isSuccess,
        wasCaught = wasCaught
    )
}