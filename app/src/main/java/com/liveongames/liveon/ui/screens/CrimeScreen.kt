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
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import java.util.*
import androidx.compose.ui.draw.alpha

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
    var pendingCrimeType by remember { mutableStateOf<CrimeViewModel.CrimeType?>(null) }

    Log.d("CrimeScreen", "CrimeScreen recomposed, crimes count: ${crimes.size}")

    // Global cooldown state
    val (cooldownActive, cooldownSeconds) = rememberCooldownState(viewModel)

    // Place this LaunchedEffect right after your variable declarations:
    LaunchedEffect(crimes) {
        if (pendingCrimeType != null && crimes.isNotEmpty()) {
            // Find the crime we just committed (match by type and recent timestamp)
            val recentCrimes = crimes.filter {
                System.currentTimeMillis() - it.timestamp < 5000 // Within last 5 seconds
            }

            val matchingCrime = recentCrimes.find { crime ->
                val crimeType = pendingCrimeType
                crimeType != null && getCrimeName(crimeType) == crime.name
            } ?: recentCrimes.maxByOrNull { it.timestamp }

            if (matchingCrime != null) {
                val crimeType = pendingCrimeType
                if (crimeType != null) {
                    showCrimeResult = CrimeResult(
                        title = when {
                            matchingCrime.success == true && matchingCrime.caught == true -> "Messy Job!"
                            matchingCrime.success == true && matchingCrime.caught == false -> "Clean Getaway!"
                            else -> "Busted!"
                        },
                        description = when {
                            matchingCrime.success == true && matchingCrime.caught == true -> "You got the money, but not without attracting unwanted attention."
                            matchingCrime.success == true && matchingCrime.caught == false -> "Everything went according to plan — no one will know until it's too late."
                            else -> "You slipped up — now you're paying the price."
                        },
                        moneyGained = matchingCrime.moneyGained ?: 0,
                        moneySeized = if (matchingCrime.caught == true && matchingCrime.success == false)
                            -(matchingCrime.moneyGained ?: 0) else 0,
                        isSuccess = matchingCrime.success ?: false,
                        wasCaught = matchingCrime.caught ?: false,
                        jailTime = matchingCrime.actualJailTime ?: 0,
                        notorietyChange = if (matchingCrime.success == true) {
                            when (getCrimeRiskTier(crimeType)) {
                                RiskTier.LOW_RISK -> 2
                                RiskTier.MEDIUM_RISK -> 4
                                RiskTier.HIGH_RISK -> 7
                                RiskTier.EXTREME_RISK -> 10
                            }
                        } else {
                            when (getCrimeRiskTier(crimeType)) {
                                RiskTier.LOW_RISK -> 0
                                RiskTier.MEDIUM_RISK -> -1
                                RiskTier.HIGH_RISK -> -2
                                RiskTier.EXTREME_RISK -> -3
                            }
                        }
                    )
                    pendingCrimeType = null
                }
            }
        }
    }

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

                // Cooldown banner
                if (cooldownActive) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_lock_idle_lock),
                                contentDescription = "Cooldown active",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Cooling down — lay low for ${String.format(Locale.getDefault(), "0:%02d", cooldownSeconds)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                            progress = { (playerNotoriety.coerceIn(0, 100)) / 100f },
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
                        cooldownActive = cooldownActive,
                        cooldownSeconds = cooldownSeconds,
                        onCrimeSelected = { crimeType ->
                            if (canCommitCrime(crimeType, playerNotoriety)) {
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
                        cooldownActive = cooldownActive,
                        cooldownSeconds = cooldownSeconds,
                        onCrimeSelected = { crimeType ->
                            if (canCommitCrime(crimeType, playerNotoriety)) {
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
                        cooldownActive = cooldownActive,
                        cooldownSeconds = cooldownSeconds,
                        onCrimeSelected = { crimeType ->
                            if (canCommitCrime(crimeType, playerNotoriety)) {
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
                        cooldownActive = cooldownActive,
                        cooldownSeconds = cooldownSeconds,
                        onCrimeSelected = { crimeType ->
                            if (canCommitCrime(crimeType, playerNotoriety)) {
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
                        if (canCommitCrime(dialogData.type, dialogData.playerNotoriety)) {
                            pendingCrimeType = dialogData.type
                            viewModel.commitCrime(dialogData.type)
                            showCrimeDialog = null
                            // Show result after a delay to allow DB update
                            CoroutineScope(Dispatchers.Main).launch {
                                kotlinx.coroutines.delay(1500)
                                onCrimeCommitted()
                            }
                        }
                    },
                    enabled = canCommitCrime(dialogData.type, dialogData.playerNotoriety),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canCommitCrime(dialogData.type, dialogData.playerNotoriety))
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
                        text = if (!canCommitCrime(dialogData.type, dialogData.playerNotoriety))
                            "Need More Notoriety"
                        else
                            dialogData.confirmText.uppercase(Locale.getDefault()),
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

    // Crime Result Dialog - THIS IS NOW A STANDALONE DIALOG
    showCrimeResult?.let { result ->
        AlertDialog(
            onDismissRequest = { showCrimeResult = null },
            containerColor = currentTheme.surface,
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
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

                    if (result.moneySeized != 0) {
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

// Helper function to determine if a crime can be committed
fun canCommitCrime(crimeType: CrimeViewModel.CrimeType, playerNotoriety: Int): Boolean {
    val requiredNotoriety = getCrimeNotorietyRequired(crimeType)

    // Allow low-risk crimes even with negative notoriety (but not too negative)
    if (getCrimeRiskTier(crimeType) == RiskTier.LOW_RISK) {
        return playerNotoriety >= -10
    }

    // For other crimes, check normal requirement
    return requiredNotoriety <= playerNotoriety
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
    val payoutMax: Int,
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
                text = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(Date(crime.timestamp)),
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
    cooldownActive: Boolean,
    cooldownSeconds: Int,
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
                CrimeButton(
                    text = "${getCrimeName(crimeType)} - ${crimeDescription}",
                    onClick = {
                        Log.d("CrimeScreen", "Crime ${getCrimeName(crimeType)} clicked")
                        onCrimeSelected(crimeType)
                    },
                    cooldownActive = cooldownActive,
                    cooldownSeconds = cooldownSeconds,
                    riskTier = getCrimeRiskTier(crimeType),
                    theme = theme,
                    isLocked = !canCommitCrime(crimeType, playerNotoriety)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
@Composable
fun CrimeButton(
    text: String,
    onClick: () -> Unit,
    cooldownActive: Boolean,
    cooldownSeconds: Int,
    riskTier: RiskTier,
    theme: com.liveongames.liveon.ui.theme.LiveonTheme,
    isLocked: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    val sirenBrush = rememberSirenBrush(cooldownActive)

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100), label = ""
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isPressed) 1.3f else 1f,
        animationSpec = tween(durationMillis = 100), label = ""
    )

    // Police light flashing animation
    val infiniteTransition = rememberInfiniteTransition(label = "policeLights")
    val flashAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0f at 0
                1f at 500
                0f at 1000
            },
            repeatMode = RepeatMode.Restart
        ), label = "flash"
    )

    // Determine if button should be enabled
    val isEnabled = !isLocked && !cooldownActive

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = isEnabled,
                onClick = {
                    if (isEnabled) {
                        isPressed = true
                        onClick()
                        CoroutineScope(Dispatchers.Main).launch {
                            kotlinx.coroutines.delay(150)
                            isPressed = false
                        }
                    }
                }
            )
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 2.dp else 6.dp
        )
    ) {
        // Background handling
        val backgroundColor = when {
            !isEnabled -> theme.surface.copy(alpha = 0.5f)
            else -> theme.surface
        }

        val useAnimatedBackground = cooldownActive && !isLocked

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (useAnimatedBackground) {
                        Modifier.background(brush = sirenBrush, shape = RoundedCornerShape(14.dp))
                    } else {
                        Modifier.background(color = backgroundColor, shape = RoundedCornerShape(14.dp))
                    }
                )
                // Police light flashing effect - only apply when in cooldown
                .alpha(if (cooldownActive) 0.7f + 0.3f * flashAnimation else 1f)
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
                            text = text.substringBefore(" - "),
                            style = MaterialTheme.typography.titleMedium,
                            color = when {
                                isLocked -> theme.accent.copy(alpha = 0.7f)
                                cooldownActive -> theme.accent.copy(alpha = 0.8f)
                                else -> theme.text
                            },
                            fontWeight = FontWeight.Medium
                        )
                        when {
                            isLocked -> {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_lock_idle_lock),
                                    contentDescription = "Locked",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(start = 4.dp),
                                    tint = Color.Red
                                )
                            }
                            cooldownActive -> {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_lock_idle_lock),
                                    contentDescription = "Cooldown",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(start = 4.dp),
                                    tint = Color(0xFFFF9800)
                                )
                            }
                        }
                    }
                    Text(
                        text = text.substringAfter(" - "),
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            isLocked || cooldownActive -> theme.accent.copy(alpha = 0.5f)
                            else -> theme.accent
                        }
                    )

                    // Risk tier badge
                    Text(
                        text = riskTier.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = getRiskTierColor(riskTier),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(
                                getRiskTierColor(riskTier).copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (cooldownActive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_lock_idle_lock),
                            contentDescription = "Cooldown active - $cooldownSeconds seconds remaining",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFFFF9800)
                        )
                        Text(
                            text = String.format(Locale.getDefault(), ":%02d", cooldownSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                } else {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_compass),
                        contentDescription = null,
                        tint = getRiskTierColor(riskTier),
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                    )
                }
            }
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
    return when (getCrimeRiskTier(crimeType)) {
        RiskTier.LOW_RISK -> 0
        RiskTier.MEDIUM_RISK -> 5
        RiskTier.HIGH_RISK -> 20
        RiskTier.EXTREME_RISK -> 40
    }
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

// Cooldown helper functions
@Composable
fun rememberCooldownState(viewModel: CrimeViewModel): Pair<Boolean, Int> {
    val cooldownUntil by viewModel.cooldownUntil.collectAsState()
    val currentTime = System.currentTimeMillis()

    return if (cooldownUntil != null && cooldownUntil!! > currentTime) {
        val secondsRemaining = ((cooldownUntil!! - currentTime) / 1000).toInt()
        true to secondsRemaining.coerceAtLeast(0)
    } else {
        false to 0
    }
}

@Composable
fun rememberSirenBrush(enabled: Boolean): Brush {
    val colors = listOf(
        androidx.compose.ui.graphics.Color(0xFF3A9BDC), // metallic blue
        androidx.compose.ui.graphics.Color(0xFF9ED1FF), // highlight blue
        androidx.compose.ui.graphics.Color(0xFF3A9BDC), // metallic blue
        androidx.compose.ui.graphics.Color(0xFFFF4C4C), // metallic red
        androidx.compose.ui.graphics.Color(0xFFFFA3A3), // highlight red
        androidx.compose.ui.graphics.Color(0xFFFF4C4C), // metallic red
        androidx.compose.ui.graphics.Color(0xFF3A9BDC), // back to blue
    )

    return if (!enabled) {
        // Static gradient when disabled
        Brush.linearGradient(
            colors = colors,
            start = Offset.Zero,
            end = Offset(100f, 100f)
        )
    } else {
        // Animated sweep
        val transition = rememberInfiniteTransition(label = "siren")
        val offset by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sirenOffset"
        )

        Brush.linearGradient(
            colors = colors,
            start = Offset(offset * 200 - 200, 0f),
            end = Offset(offset * 200, 200f)
        )
    }
}