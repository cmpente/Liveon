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
import com.liveongames.domain.model.Crime
import com.liveongames.domain.model.RiskTier
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
import kotlin.random.Random

@Composable
fun CrimeScreen(
    viewModel: CrimeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onCrimeCommitted: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val crimes by viewModel.crimes.collectAsState()
    val playerNotoriety by viewModel.playerNotoriety.collectAsState()
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
                                text = "Notoriety: $playerNotoriety/100",
                                style = MaterialTheme.typography.titleMedium,
                                color = currentTheme.text,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // Progress bar for notoriety
                        LinearProgressIndicator(
                            progress = { playerNotoriety / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            color = when {
                                playerNotoriety >= 80 -> Color.Red
                                playerNotoriety >= 50 -> Color(0xFFFF9800)
                                playerNotoriety >= 20 -> Color(0xFFFFEB3B)
                                else -> Color(0xFF4CAF50)
                            }
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
                        title = "Low Risk Crimes",
                        description = "Safe but modest gains",
                        crimes = listOf(
                            CrimeViewModel.CrimeType.PICKPOCKETING to "Steal wallets ($20-$200)",
                            CrimeViewModel.CrimeType.SHOPLIFTING to "Shoplift goods ($50-$300)",
                            CrimeViewModel.CrimeType.VANDALISM to "Damage property ($10-$150)",
                            CrimeViewModel.CrimeType.PETTY_SCAM to "Small scams ($30-$250)"
                        ),
                        theme = currentTheme,
                        playerNotoriety = playerNotoriety,
                        onCrimeSelected = { crimeType ->
                            showCrimeDialog = CrimeDialogData(
                                type = crimeType,
                                title = getCrimeName(crimeType),
                                message = getCrimeScenario(crimeType),
                                confirmText = "Proceed",
                                cancelText = "Nevermind",
                                riskTier = getCrimeRiskTier(crimeType),
                                notorietyRequired = getCrimeNotorietyRequired(crimeType),
                                playerNotoriety = playerNotoriety,
                                successChance = getCrimeSuccessChance(crimeType),
                                payoutMin = getCrimePayoutMin(crimeType),
                                payoutMax = getCrimePayoutMax(crimeType)
                            )
                        }
                    )

                    CrimeCategorySection(
                        title = "Medium Risk Crimes",
                        description = "Bigger rewards, higher danger",
                        crimes = listOf(
                            CrimeViewModel.CrimeType.MUGGING to "Rob victims ($100-$800)",
                            CrimeViewModel.CrimeType.BREAKING_AND_ENTERING to "B&E homes ($500-$2k)",
                            CrimeViewModel.CrimeType.DRUG_DEALING to "Sell drugs ($200-$1.8k)",
                            CrimeViewModel.CrimeType.COUNTERFEIT_GOODS to "Fake goods ($300-$1.5k)"
                        ),
                        theme = currentTheme,
                        playerNotoriety = playerNotoriety,
                        onCrimeSelected = { crimeType ->
                            showCrimeDialog = CrimeDialogData(
                                type = crimeType,
                                title = getCrimeName(crimeType),
                                message = getCrimeScenario(crimeType),
                                confirmText = "Proceed",
                                cancelText = "Nevermind",
                                riskTier = getCrimeRiskTier(crimeType),
                                notorietyRequired = getCrimeNotorietyRequired(crimeType),
                                playerNotoriety = playerNotoriety,
                                successChance = getCrimeSuccessChance(crimeType),
                                payoutMin = getCrimePayoutMin(crimeType),
                                payoutMax = getCrimePayoutMax(crimeType)
                            )
                        }
                    )

                    CrimeCategorySection(
                        title = "High Risk Crimes",
                        description = "Serious crimes with serious consequences",
                        crimes = listOf(
                            CrimeViewModel.CrimeType.BURGLARY to "Break into homes ($1k-$8k)",
                            CrimeViewModel.CrimeType.FRAUD to "Financial scams ($2k-$12k)",
                            CrimeViewModel.CrimeType.ARMS_SMUGGLING to "Weapon trafficking ($5k-$22k)",
                            CrimeViewModel.CrimeType.DRUG_TRAFFICKING to "Drug distribution ($10k-$45k)"
                        ),
                        theme = currentTheme,
                        playerNotoriety = playerNotoriety,
                        onCrimeSelected = { crimeType ->
                            showCrimeDialog = CrimeDialogData(
                                type = crimeType,
                                title = getCrimeName(crimeType),
                                message = getCrimeScenario(crimeType),
                                confirmText = "Proceed",
                                cancelText = "Nevermind",
                                riskTier = getCrimeRiskTier(crimeType),
                                notorietyRequired = getCrimeNotorietyRequired(crimeType),
                                playerNotoriety = playerNotoriety,
                                successChance = getCrimeSuccessChance(crimeType),
                                payoutMin = getCrimePayoutMin(crimeType),
                                payoutMax = getCrimePayoutMax(crimeType)
                            )
                        }
                    )

                    CrimeCategorySection(
                        title = "Extreme Risk Crimes",
                        description = "Extremely dangerous with life-changing stakes",
                        crimes = listOf(
                            CrimeViewModel.CrimeType.ARMED_ROBBERY to "Armed heists ($20k-$120k)",
                            CrimeViewModel.CrimeType.EXTORTION to "Threaten for money ($5k-$40k)",
                            CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM to "Kidnap victims ($50k-$400k)",
                            CrimeViewModel.CrimeType.PONZI_SCHEME to "Big fraud ($90k-$900k)"
                        ),
                        theme = currentTheme,
                        playerNotoriety = playerNotoriety,
                        onCrimeSelected = { crimeType ->
                            showCrimeDialog = CrimeDialogData(
                                type = crimeType,
                                title = getCrimeName(crimeType),
                                message = getCrimeScenario(crimeType),
                                confirmText = "Proceed",
                                cancelText = "Nevermind",
                                riskTier = getCrimeRiskTier(crimeType),
                                notorietyRequired = getCrimeNotorietyRequired(crimeType),
                                playerNotoriety = playerNotoriety,
                                successChance = getCrimeSuccessChance(crimeType),
                                payoutMin = getCrimePayoutMin(crimeType),
                                payoutMax = getCrimePayoutMax(crimeType)
                            )
                        }
                    )

                    // Criminal Record Section
                    Text(
                        text = "Criminal Record",
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
                            if (crimes.isEmpty()) {
                                Text(
                                    text = "✨ Clean Record - No criminal history ✨",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            } else {
                                // Compact stats
                                Text(
                                    text = "${crimes.size} crime${if (crimes.size > 1) "s" else ""} • Notoriety: $playerNotoriety",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = currentTheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // Scrollable crime history (most recent at top)
                                val scrollState = rememberScrollState()
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .verticalScroll(scrollState)
                                ) {
                                    // Sort crimes by timestamp (newest first)
                                    crimes.sortedByDescending { it.timestamp }.forEachIndexed { index, crime ->
                                        CompactCrimeHistoryEntry(
                                            crime = crime,
                                            theme = currentTheme
                                        )
                                        // Add divider except for last item
                                        if (index < crimes.size - 1) {
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

                    // Clear Record Button - more compact
                    OutlinedButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .height(40.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text("Clear Record ($5000)", style = MaterialTheme.typography.bodySmall)
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
                    // Crime icon (using simple placeholder for now)
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_compass),
                        contentDescription = null,
                        tint = getRiskTierColor(dialogData.riskTier),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Crime title with risk tier indicator
                    Text(
                        text = dialogData.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = currentTheme.text,
                        fontWeight = FontWeight.Bold
                    )

                    // Risk tier badge
                    Text(
                        text = dialogData.riskTier.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = currentTheme.text,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(
                                getRiskTierColor(dialogData.riskTier).copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            },
            text = {
                Column {
                    // Scenario
                    Text(
                        text = "Scenario: ${dialogData.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = currentTheme.accent
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Potential rewards
                    Text(
                        text = "💰 Potential Earnings: $${dialogData.payoutMin}-${dialogData.payoutMax}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Notoriety requirement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Notoriety Required: ${dialogData.notorietyRequired}",
                            style = MaterialTheme.typography.bodySmall,
                            color = currentTheme.primary
                        )
                        Text(
                            text = "Your Notoriety: ${dialogData.playerNotoriety}",
                            style = MaterialTheme.typography.bodySmall,
                            color = currentTheme.accent
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Success chance
                    Text(
                        text = "Success Chance: ${(dialogData.successChance * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            dialogData.successChance >= 0.7 -> Color(0xFF4CAF50)
                            dialogData.successChance >= 0.5 -> Color(0xFFFFEB3B)
                            else -> Color.Red
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
                        if (dialogData.notorietyRequired <= dialogData.playerNotoriety) {
                            viewModel.commitCrime(dialogData.type)
                            showCrimeDialog = null
                            // Show result after a delay
                            CoroutineScope(Dispatchers.Main).launch {
                                kotlinx.coroutines.delay(1000)
                                val result = generateCrimeResult(dialogData.type)
                                showCrimeResult = result
                                onCrimeCommitted()
                            }
                        }
                    },
                    enabled = dialogData.notorietyRequired <= dialogData.playerNotoriety,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (dialogData.notorietyRequired <= dialogData.playerNotoriety)
                            getRiskTierColor(dialogData.riskTier)
                        else
                            Color.Gray,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (dialogData.notorietyRequired > dialogData.playerNotoriety)
                            "Need More Notoriety"
                        else
                            dialogData.confirmText.uppercase(),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCrimeDialog = null },
                    border = BorderStroke(1.dp, currentTheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = currentTheme.primary
                    )
                ) {
                    Text(dialogData.cancelText)
                }
            }
        )
    }

    // Crime Result Dialog
    showCrimeResult?.let { result ->
        AlertDialog(
            onDismissRequest = { showCrimeResult = null },
            containerColor = currentTheme.surface,
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Result icon based on success
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(
                            id = when {
                                result.isSuccess && !result.wasCaught -> android.R.drawable.ic_menu_save
                                result.isSuccess && result.wasCaught -> android.R.drawable.ic_menu_today
                                else -> android.R.drawable.ic_menu_close_clear_cancel
                            }
                        ),
                        contentDescription = null,
                        tint = when {
                            result.isSuccess && !result.wasCaught -> Color(0xFF4CAF50)
                            result.isSuccess && result.wasCaught -> Color(0xFFFFEB3B)
                            else -> Color.Red
                        },
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = when {
                            result.isSuccess && !result.wasCaught -> Color(0xFF4CAF50)
                            result.isSuccess && result.wasCaught -> Color(0xFFFFEB3B)
                            else -> Color.Red
                        },
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

                    if (result.notorietyChange != 0) {
                        Text(
                            text = if (result.notorietyChange > 0)
                                "📈 Notoriety +${result.notorietyChange}"
                            else
                                "📉 Notoriety ${result.notorietyChange}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (result.notorietyChange > 0)
                                Color(0xFF4CAF50)
                            else
                                Color.Red,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (result.jailTime > 0) {
                        Text(
                            text = "🔒 Jail Time: ${result.jailTime} days",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (result.wasCaught && result.moneySeized > 0) {
                        Text(
                            text = "💰 Money Seized: $${result.moneySeized}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCrimeResult = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            result.isSuccess && !result.wasCaught -> Color(0xFF4CAF50)
                            result.isSuccess && result.wasCaught -> Color(0xFFFFEB3B)
                            else -> Color(0xFFFF9800)
                        },
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
    val type: CrimeViewModel.CrimeType,
    val title: String,
    val message: String,
    val confirmText: String,
    val cancelText: String,
    val riskTier: RiskTier,
    val notorietyRequired: Int,
    val playerNotoriety: Int,
    val successChance: Double,
    val payoutMin: Int,
    val payoutMax: Int
)

data class CrimeResult(
    val title: String,
    val description: String,
    val moneyGained: Int,
    val moneySeized: Int,
    val isSuccess: Boolean,
    val wasCaught: Boolean,
    val jailTime: Int,
    val notorietyChange: Int
)

// Compact Crime History Entry
@Composable
fun CompactCrimeHistoryEntry(
    crime: Crime,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Crime details in one line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = crime.name,
                style = MaterialTheme.typography.bodyMedium,
                color = theme.text,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = when {
                    crime.success == true && crime.caught == true -> "Messy Job"
                    crime.success == true && crime.caught == false -> "Success"
                    crime.success == false -> "Failed"
                    else -> "Unknown"
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    crime.success == true && crime.caught == true -> Color(0xFFFFEB3B)
                    crime.success == true && crime.caught == false -> Color(0xFF4CAF50)
                    crime.success == false -> Color.Red
                    else -> theme.accent
                },
                fontWeight = FontWeight.Bold
            )
        }

        // Date and payout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                    .format(java.util.Date(crime.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = theme.accent
            )

            val moneyGainedValue = crime.moneyGained ?: 0
            if (moneyGainedValue > 0) {
                Text(
                    text = "Gained $$moneyGainedValue",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
            } else if (crime.success == false) {
                Text(
                    text = "No gain",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red
                )
            }
        }

        // Jail time if applicable
        val actualJailTimeValue = crime.actualJailTime ?: 0
        if (actualJailTimeValue > 0) {
            Text(
                text = "Jail: ${actualJailTimeValue} days",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFF9800)
            )
        }

        // Scenario snippet (first 30 chars)
        Text(
            text = "${crime.scenario.take(30)}...",
            style = MaterialTheme.typography.bodySmall,
            color = theme.accent.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

// Crime Category Section
@Composable
fun CrimeCategorySection(
    title: String,
    description: String,
    crimes: List<Pair<CrimeViewModel.CrimeType, String>>,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    playerNotoriety: Int,
    onCrimeSelected: (CrimeViewModel.CrimeType) -> Unit = {}
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
                        Log.d("CrimeScreen", "Crime ${getCrimeName(crimeType)} clicked")
                        onCrimeSelected(crimeType)
                    },
                    theme = theme,
                    isLocked = getCrimeNotorietyRequired(crimeType) > playerNotoriety
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CrimeTypeCard(
    crimeType: CrimeViewModel.CrimeType,
    description: String,
    onClick: () -> Unit,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    isLocked: Boolean = false
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
            .clickable(
                enabled = !isLocked,
                onClick = {
                    isPressed = true
                    onClick()
                    CoroutineScope(Dispatchers.Main).launch {
                        kotlinx.coroutines.delay(150)
                        isPressed = false
                    }
                }
            )
            .graphicsLayer(scaleX = scale, scaleY = scale),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) theme.surface.copy(alpha = 0.5f) else theme.surface
        ),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getCrimeName(crimeType),
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
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLocked) theme.accent.copy(alpha = 0.5f) else theme.accent
                )

                // Risk tier badge
                Text(
                    text = getCrimeRiskTier(crimeType).displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = getRiskTierColor(getCrimeRiskTier(crimeType)),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(
                            getRiskTierColor(getCrimeRiskTier(crimeType)).copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Icon(
                painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_compass),
                contentDescription = null,
                tint = getRiskTierColor(getCrimeRiskTier(crimeType)),
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
            )
        }
    }
}

// Helper functions for crime data
fun getRiskTierColor(riskTier: RiskTier): androidx.compose.ui.graphics.Color {
    return when (riskTier) {
        RiskTier.LOW_RISK -> Color(0xFF4CAF50) // Green
        RiskTier.MEDIUM_RISK -> Color(0xFFFFEB3B) // Yellow
        RiskTier.HIGH_RISK -> Color(0xFFFF9800) // Orange
        RiskTier.EXTREME_RISK -> Color(0xFFF44336) // Red
    }
}

fun getCrimeName(crimeType: CrimeViewModel.CrimeType): String {
    return when (crimeType) {
        CrimeViewModel.CrimeType.PICKPOCKETING -> "Pickpocketing"
        CrimeViewModel.CrimeType.SHOPLIFTING -> "Shoplifting"
        CrimeViewModel.CrimeType.VANDALISM -> "Vandalism"
        CrimeViewModel.CrimeType.PETTY_SCAM -> "Petty Scam"
        CrimeViewModel.CrimeType.MUGGING -> "Mugging"
        CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> "Breaking and Entering"
        CrimeViewModel.CrimeType.DRUG_DEALING -> "Drug Dealing"
        CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> "Counterfeit Goods"
        CrimeViewModel.CrimeType.BURGLARY -> "Burglary"
        CrimeViewModel.CrimeType.FRAUD -> "Fraud"
        CrimeViewModel.CrimeType.ARMS_SMUGGLING -> "Arms Smuggling"
        CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> "Drug Trafficking"
        CrimeViewModel.CrimeType.ARMED_ROBBERY -> "Armed Robbery"
        CrimeViewModel.CrimeType.EXTORTION -> "Extortion"
        CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM -> "Kidnapping for Ransom"
        CrimeViewModel.CrimeType.PONZI_SCHEME -> "Ponzi Scheme"
        CrimeViewModel.CrimeType.CONTRACT_KILLING -> "Contract Killing"
        CrimeViewModel.CrimeType.DARK_WEB_SALES -> "Dark Web Sales"
        CrimeViewModel.CrimeType.ART_THEFT -> "Art Theft"
        CrimeViewModel.CrimeType.DIAMOND_HEIST -> "Diamond Heist"
    }
}

fun getCrimeScenario(crimeType: CrimeViewModel.CrimeType): String {
    val scenarios = when (crimeType) {
        CrimeViewModel.CrimeType.PICKPOCKETING -> listOf(
            "You spot a distracted shopper fumbling for their phone.",
            "A tourist stops to take a photo, bag open on their shoulder.",
            "A gambler counts his winnings openly in the street."
        )
        CrimeViewModel.CrimeType.SHOPLIFTING -> listOf(
            "You notice a blind spot in the store's camera coverage.",
            "The fitting rooms are unattended.",
            "Someone else triggers a loud commotion."
        )
        CrimeViewModel.CrimeType.VANDALISM -> listOf(
            "A rival gang's mural taunts your crew.",
            "A politician's poster becomes your canvas.",
            "You tag over a rival's graffiti."
        )
        CrimeViewModel.CrimeType.PETTY_SCAM -> listOf(
            "You \"find\" a gold ring and offer to sell it cheap.",
            "You sell fake raffle tickets at a busy market.",
            "You pose as a charity collector."
        )
        CrimeViewModel.CrimeType.MUGGING -> listOf(
            "You corner a lone businessman in a dark alley.",
            "A jogger stops to catch their breath, headphones in.",
            "A tourist wanders into the wrong neighborhood."
        )
        CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> listOf(
            "You spot a home with lights off and mail piling up.",
            "A back window is left unlocked.",
            "A shopkeeper leaves the rear door ajar."
        )
        CrimeViewModel.CrimeType.DRUG_DEALING -> listOf(
            "A regular customer asks for a bigger order than usual.",
            "You meet a new buyer at a busy park.",
            "A bar patron discreetly approaches you."
        )
        CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> listOf(
            "A flea market vendor agrees to move your goods.",
            "Tourists crowd around your street stall.",
            "A nightclub promoter buys bulk for giveaways."
        )
        CrimeViewModel.CrimeType.BURGLARY -> listOf(
            "You disable a small shop's alarm system.",
            "A mansion is left unattended for the weekend.",
            "You find a warehouse with lax security."
        )
        CrimeViewModel.CrimeType.FRAUD -> listOf(
            "You set up a fake charity donation site.",
            "You skim credit cards at a gas station.",
            "You forge a cashier's check."
        )
        CrimeViewModel.CrimeType.ARMS_SMUGGLING -> listOf(
            "You move a shipment through a border checkpoint.",
            "You sell to a biker gang out of state.",
            "You load crates into a cargo van at night."
        )
        CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> listOf(
            "You drive a van across the state line.",
            "A shipment arrives hidden in produce crates.",
            "You use a fishing boat to transport packages."
        )
        CrimeViewModel.CrimeType.ARMED_ROBBERY -> listOf(
            "You storm a jewelry store during peak hours.",
            "You hit an armored truck in transit.",
            "You rob a high-stakes poker game."
        )
        CrimeViewModel.CrimeType.EXTORTION -> listOf(
            "You threaten to leak sensitive photos.",
            "You demand \"protection\" money from a nightclub.",
            "You blackmail a corporate executive."
        )
        CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM -> listOf(
            "You grab a wealthy child outside a school.",
            "You abduct a celebrity's assistant.",
            "You take a local politician's spouse."
        )
        CrimeViewModel.CrimeType.PONZI_SCHEME -> listOf(
            "You launch a fake investment firm.",
            "You promise impossible returns to investors.",
            "You use new deposits to pay earlier victims."
        )
        CrimeViewModel.CrimeType.CONTRACT_KILLING -> listOf(
            "You accept a hit on a rival gang leader.",
            "You take out a cheating spouse's lover.",
            "You ambush a target in a parking garage."
        )
        CrimeViewModel.CrimeType.DARK_WEB_SALES -> listOf(
            "You sell stolen bank credentials.",
            "You auction off hacking tools.",
            "You ship counterfeit passports overseas."
        )
        CrimeViewModel.CrimeType.ART_THEFT -> listOf(
            "You steal a masterpiece during an exhibition.",
            "You swap a gallery piece for a replica.",
            "You break into a private collection."
        )
        CrimeViewModel.CrimeType.DIAMOND_HEIST -> listOf(
            "You rob a diamond exchange vault.",
            "You hit a guarded transport truck.",
            "You infiltrate a mining company's storage."
        )
    }
    return scenarios.random()
}

fun getCrimeRiskTier(crimeType: CrimeViewModel.CrimeType): RiskTier {
    return when (crimeType) {
        // LOW RISK
        CrimeViewModel.CrimeType.PICKPOCKETING,
        CrimeViewModel.CrimeType.SHOPLIFTING,
        CrimeViewModel.CrimeType.VANDALISM,
        CrimeViewModel.CrimeType.PETTY_SCAM -> RiskTier.LOW_RISK

        // MEDIUM RISK
        CrimeViewModel.CrimeType.MUGGING,
        CrimeViewModel.CrimeType.BREAKING_AND_ENTERING,
        CrimeViewModel.CrimeType.DRUG_DEALING,
        CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> RiskTier.MEDIUM_RISK

        // HIGH RISK
        CrimeViewModel.CrimeType.BURGLARY,
        CrimeViewModel.CrimeType.FRAUD,
        CrimeViewModel.CrimeType.ARMS_SMUGGLING,
        CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> RiskTier.HIGH_RISK

        // EXTREME RISK
        CrimeViewModel.CrimeType.ARMED_ROBBERY,
        CrimeViewModel.CrimeType.EXTORTION,
        CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM,
        CrimeViewModel.CrimeType.PONZI_SCHEME,
        CrimeViewModel.CrimeType.CONTRACT_KILLING,
        CrimeViewModel.CrimeType.DARK_WEB_SALES,
        CrimeViewModel.CrimeType.ART_THEFT,
        CrimeViewModel.CrimeType.DIAMOND_HEIST -> RiskTier.EXTREME_RISK
    }
}

fun getCrimeNotorietyRequired(crimeType: CrimeViewModel.CrimeType): Int {
    return getCrimeRiskTier(crimeType).notorietyRequired
}

fun getCrimeSuccessChance(crimeType: CrimeViewModel.CrimeType): Double {
    return when (getCrimeRiskTier(crimeType)) {
        RiskTier.LOW_RISK -> 0.7
        RiskTier.MEDIUM_RISK -> 0.55
        RiskTier.HIGH_RISK -> 0.45
        RiskTier.EXTREME_RISK -> 0.3
    }
}

fun getCrimePayoutMin(crimeType: CrimeViewModel.CrimeType): Int {
    return when (crimeType) {
        CrimeViewModel.CrimeType.PICKPOCKETING -> 20
        CrimeViewModel.CrimeType.SHOPLIFTING -> 50
        CrimeViewModel.CrimeType.VANDALISM -> 10
        CrimeViewModel.CrimeType.PETTY_SCAM -> 30
        CrimeViewModel.CrimeType.MUGGING -> 100
        CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> 500
        CrimeViewModel.CrimeType.DRUG_DEALING -> 200
        CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> 300
        CrimeViewModel.CrimeType.BURGLARY -> 1000
        CrimeViewModel.CrimeType.FRAUD -> 2000
        CrimeViewModel.CrimeType.ARMS_SMUGGLING -> 5000
        CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> 10000
        CrimeViewModel.CrimeType.ARMED_ROBBERY -> 20000
        CrimeViewModel.CrimeType.EXTORTION -> 5000
        CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM -> 50000
        CrimeViewModel.CrimeType.PONZI_SCHEME -> 90000
        CrimeViewModel.CrimeType.CONTRACT_KILLING -> 25000
        CrimeViewModel.CrimeType.DARK_WEB_SALES -> 12000
        CrimeViewModel.CrimeType.ART_THEFT -> 110000
        CrimeViewModel.CrimeType.DIAMOND_HEIST -> 500000
    }
}

fun getCrimePayoutMax(crimeType: CrimeViewModel.CrimeType): Int {
    return when (crimeType) {
        CrimeViewModel.CrimeType.PICKPOCKETING -> 200
        CrimeViewModel.CrimeType.SHOPLIFTING -> 300
        CrimeViewModel.CrimeType.VANDALISM -> 150
        CrimeViewModel.CrimeType.PETTY_SCAM -> 250
        CrimeViewModel.CrimeType.MUGGING -> 800
        CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> 2000
        CrimeViewModel.CrimeType.DRUG_DEALING -> 1800
        CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> 1500
        CrimeViewModel.CrimeType.BURGLARY -> 8000
        CrimeViewModel.CrimeType.FRAUD -> 12000
        CrimeViewModel.CrimeType.ARMS_SMUGGLING -> 22000
        CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> 45000
        CrimeViewModel.CrimeType.ARMED_ROBBERY -> 120000
        CrimeViewModel.CrimeType.EXTORTION -> 40000
        CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM -> 400000
        CrimeViewModel.CrimeType.PONZI_SCHEME -> 900000
        CrimeViewModel.CrimeType.CONTRACT_KILLING -> 450000
        CrimeViewModel.CrimeType.DARK_WEB_SALES -> 220000
        CrimeViewModel.CrimeType.ART_THEFT -> 4800000
        CrimeViewModel.CrimeType.DIAMOND_HEIST -> 9500000
    }
}

fun generateCrimeResult(crimeType: CrimeViewModel.CrimeType): CrimeResult {
    // Generate random outcomes for demonstration
    val isSuccess = Random.nextBoolean()
    val wasCaught = Random.nextBoolean()
    val moneyGained = if (isSuccess) Random.nextInt(100, 10000) else 0
    val moneySeized = if (wasCaught && isSuccess) Random.nextInt(0, moneyGained/2) else 0
    val jailTime = if (wasCaught) Random.nextInt(1, 30) else 0
    val notorietyChange = if (isSuccess) Random.nextInt(1, 5) else -Random.nextInt(1, 5)

    return CrimeResult(
        title = when {
            isSuccess && !wasCaught -> "Clean Getaway!"
            isSuccess && wasCaught -> "Messy Job!"
            else -> "Busted!"
        },
        description = when {
            isSuccess && !wasCaught -> "Everything went according to plan — no one will know until it's too late."
            isSuccess && wasCaught -> "You got the money, but not without attracting unwanted attention."
            else -> "You slipped up — now you're paying the price."
        },
        moneyGained = moneyGained,
        moneySeized = moneySeized,
        isSuccess = isSuccess,
        wasCaught = wasCaught,
        jailTime = jailTime,
        notorietyChange = notorietyChange
    )
}