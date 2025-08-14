// app/src/main/java/com/liveongames/liveon/ui/screens/CrimeScreen.kt
package com.liveongames.liveon.ui.screens

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.domain.model.Crime
import com.liveongames.domain.model.RiskTier
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LocalLiveonTheme
import com.liveongames.liveon.viewmodel.CrimeViewModel
import com.liveongames.liveon.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.draw.alpha

/**
 * Crime screen styled to MATCH EducationSheet:
 * - Same bottom-sheet container, paddings, rounded radius, typography scale
 * - Panel sections for header/stats and each risk tier
 * - Dense list rows (same height/spacing as Education activities)
 */
@Composable
fun CrimeScreen(
    viewModel: CrimeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(), // keep DI happy
    onCrimeCommitted: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val t = LocalLiveonTheme.current
    val crimes by viewModel.crimes.collectAsState()
    val notoriety by viewModel.playerNotoriety.collectAsState()

    var confirm by remember { mutableStateOf<CrimeViewModel.CrimeType?>(null) }
    var result by remember { mutableStateOf<CrimeResult?>(null) }
    var pendingType by remember { mutableStateOf<CrimeViewModel.CrimeType?>(null) }

    // Global cooldown state (derived from vm.cooldownUntil)
    val (cooldownActive, cooldownSecs) = rememberCooldownState(viewModel)

    // When a crime record appears after committing, show result dialog
    LaunchedEffect(crimes) {
        if (pendingType != null && crimes.isNotEmpty()) {
            val recent = crimes.filter { System.currentTimeMillis() - it.timestamp < 5_000 }
            val match = recent.find { c -> pendingType?.let { getCrimeName(it) } == c.name }
                ?: recent.maxByOrNull { it.timestamp }

            match?.let { c ->
                pendingType?.let { type ->
                    result = CrimeResult.fromRecord(type, c)
                    pendingType = null
                }
            }
        }
    }

    // --- Scrim like EducationSheet ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
    ) {
        // --- Bottom sheet container (same sizing as Education) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(t.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* block outside-dismiss */ },
            verticalArrangement = Arrangement.Top
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Criminal Activities",
                    color = t.text,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(R.drawable.ic_collapse),
                        contentDescription = "Close",
                        tint = t.text.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Content (same spacing pattern as Education)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Notoriety / status panel
                item {
                    PanelCard {
                        Text(
                            "Notoriety",
                            color = t.text.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { notoriety / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            trackColor = t.surfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "$notoriety/100",
                                color = t.text,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(Modifier.weight(1f))

                            // Global cooldown badge
                            val badgeBrush = rememberSirenBrush(cooldownActive)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(badgeBrush)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                val mm = cooldownSecs / 60
                                val ss = cooldownSecs % 60
                                Text(
                                    if (cooldownActive) "Cooldown ${"%d:%02d".format(mm, ss)}" else "Ready",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                // Low risk
                item {
                    CrimeSection(
                        title = "Low Risk Crimes",
                        subtitle = "Safe but modest gains",
                        color = t.primary,
                        crimes = listOf(
                            CrimeViewModel.CrimeType.PICKPOCKETING,
                            CrimeViewModel.CrimeType.SHOPLIFTING,
                            CrimeViewModel.CrimeType.VANDALISM,
                            CrimeViewModel.CrimeType.PETTY_SCAM
                        ),
                        cooldownActive = cooldownActive,
                        onCrimeClick = { type -> confirm = type }
                    )
                }

                // Medium risk
                item {
                    CrimeSection(
                        title = "Medium Risk Crimes",
                        subtitle = "Bigger rewards, higher danger",
                        color = t.accent,
                        crimes = listOf(
                            CrimeViewModel.CrimeType.MUGGING,
                            CrimeViewModel.CrimeType.BREAKING_AND_ENTERING,
                            CrimeViewModel.CrimeType.DRUG_DEALING,
                            CrimeViewModel.CrimeType.COUNTERFEIT_GOODS
                        ),
                        cooldownActive = cooldownActive,
                        onCrimeClick = { type -> confirm = type }
                    )
                }

                // High risk
                item {
                    CrimeSection(
                        title = "High Risk Crimes",
                        subtitle = "High stakes, serious consequences",
                        color = Color(0xFFFFB74D),
                        crimes = listOf(
                            CrimeViewModel.CrimeType.BURGLARY,
                            CrimeViewModel.CrimeType.FRAUD,
                            CrimeViewModel.CrimeType.ARMS_SMUGGLING,
                            CrimeViewModel.CrimeType.DRUG_TRAFFICKING,
                            CrimeViewModel.CrimeType.ARMED_ROBBERY,
                            CrimeViewModel.CrimeType.EXTORTION,
                            CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM,
                            CrimeViewModel.CrimeType.PONZI_SCHEME
                        ),
                        cooldownActive = cooldownActive,
                        onCrimeClick = { type -> confirm = type }
                    )
                }

                // Extreme risk
                item {
                    CrimeSection(
                        title = "Extreme Risk Crimes",
                        subtitle = "Elite jobs with massive risk",
                        color = Color(0xFFFF6E6E),
                        crimes = listOf(
                            CrimeViewModel.CrimeType.CONTRACT_KILLING,
                            CrimeViewModel.CrimeType.DARK_WEB_SALES,
                            CrimeViewModel.CrimeType.ART_THEFT,
                            CrimeViewModel.CrimeType.DIAMOND_HEIST
                        ),
                        cooldownActive = cooldownActive,
                        onCrimeClick = { type -> confirm = type }
                    )
                }
            }
        }
    }

    // --- Confirm dialog ---
    confirm?.let { type ->
        val tier = getCrimeRiskTier(type)
        val min = getCrimePayoutMin(type)
        val max = getCrimePayoutMax(type)
        val amount = "(${fmt(min)}–${fmt(max)})"

        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text(getCrimeName(type)) },
            text = {
                Text(
                    "Attempt this ${tierLabel(tier)} job? $amount\n${getCrimeDesc(type)}",
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    enabled = !cooldownActive,
                    onClick = {
                        confirm = null
                        pendingType = type
                        viewModel.commitCrime(type)
                        // start global cooldown based on tier
                        viewModel.startGlobalCooldown(CrimeViewModel.cooldownForTier(tier))
                    }
                ) { Text(if (cooldownActive) "On Cooldown…" else "Do it") }
            },
            dismissButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    onClick = { confirm = null }
                ) { Text("Cancel") }
            }
        )
    }

    // --- Result dialog ---
    result?.let { r ->
        AlertDialog(
            onDismissRequest = { result = null },
            title = { Text(r.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(r.description)
                    if (r.moneyGained != 0) Text("Money: ${fmtSigned(r.moneyGained)}")
                    if (r.moneySeized != 0) Text("Seized: ${fmtSigned(r.moneySeized)}")
                    if (r.jailTime > 0) Text("Jail Time: ${r.jailTime} months")
                }
            },
            confirmButton = {
                Button(onClick = { result = null }) { Text("Okay") }
            }
        )
    }
}

/* ----------------------------- UI Pieces ----------------------------- */

@Composable
private fun PanelCard(
    content: @Composable ColumnScope.() -> Unit
) {
    val t = LocalLiveonTheme.current
    Surface(
        color = t.surfaceElevated,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun CrimeSection(
    title: String,
    subtitle: String,
    color: Color,
    crimes: List<CrimeViewModel.CrimeType>,
    cooldownActive: Boolean,
    onCrimeClick: (CrimeViewModel.CrimeType) -> Unit
) {
    val t = LocalLiveonTheme.current
    Surface(
        color = t.surfaceElevated,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                color = color,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                color = t.text.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(12.dp))
            crimes.forEachIndexed { idx, type ->
                CrimeRow(
                    type = type,
                    disabled = cooldownActive,
                    onClick = { onCrimeClick(type) }
                )
                if (idx != crimes.lastIndex) {
                    Divider(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                        color = t.surfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CrimeRow(
    type: CrimeViewModel.CrimeType,
    disabled: Boolean,
    onClick: () -> Unit
) {
    val t = LocalLiveonTheme.current
    val tier = getCrimeRiskTier(type)
    val name = getCrimeName(type)
    val desc = getCrimeDesc(type)
    val min = getCrimePayoutMin(type)
    val max = getCrimePayoutMax(type)

    val rowAlpha = if (disabled) 0.6f else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp) // same density as Education activity rows
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface.copy(alpha = 0.25f))
            .clickable(enabled = !disabled) { onClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bullet icon
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    when (tier) {
                        RiskTier.LOW_RISK -> Color(0xFF2ECC71)
                        RiskTier.MEDIUM_RISK -> Color(0xFFFFC107)
                        RiskTier.HIGH_RISK -> Color(0xFFFF7043)
                        RiskTier.EXTREME_RISK -> Color(0xFFE53935)
                    }
                )
        )
        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(rowAlpha)
        ) {
            Text(
                name,
                color = t.text,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${getCrimeDescShort(desc)} • ${fmt(min)}–${fmt(max)}",
                color = t.text.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(12.dp))

        Icon(
            painter = painterResource(R.drawable.ic_scope),
            contentDescription = null,
            tint = t.accent.copy(alpha = if (disabled) 0.4f else 1f)
        )
    }
}

/* ----------------------------- Helpers ----------------------------- */

@Composable
fun rememberCooldownState(viewModel: CrimeViewModel): Pair<Boolean, Int> {
    val until by viewModel.cooldownUntil.collectAsState()
    val now = System.currentTimeMillis()
    return if (until != null && until!! > now) {
        true to (((until!! - now) / 1000).toInt().coerceAtLeast(0))
    } else {
        false to 0
    }
}

@Composable
fun rememberSirenBrush(enabled: Boolean): Brush {
    val colors = listOf(
        Color(0xFF3A9BDC), // blue
        Color(0xFF9ED1FF),
        Color(0xFF3A9BDC),
        Color(0xFFFF4C4C), // red
        Color(0xFFFFA3A3),
        Color(0xFFFF4C4C),
        Color(0xFF3A9BDC)
    )
    if (!enabled) return Brush.linearGradient(colors, Offset.Zero, Offset(100f, 100f))

    val t = rememberInfiniteTransition(label = "siren")
    val off by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "sirenOff"
    )
    return Brush.linearGradient(colors, start = Offset(off * 200 - 200, 0f), end = Offset(off * 200, 200f))
}

// Result mapping
private data class CrimeResult(
    val title: String,
    val description: String,
    val moneyGained: Int,
    val moneySeized: Int,
    val isSuccess: Boolean,
    val wasCaught: Boolean,
    val jailTime: Int
) {
    companion object {
        fun fromRecord(type: CrimeViewModel.CrimeType, c: Crime): CrimeResult {
            val title = when {
                c.success == true && c.caught == true -> "Messy Job!"
                c.success == true && c.caught == false -> "Clean Getaway!"
                else -> "Busted!"
            }
            val desc = when {
                c.success == true && c.caught == true -> "You got the money, but not without attracting unwanted attention."
                c.success == true && c.caught == false -> "Everything went according to plan — no one noticed a thing."
                else -> "You slipped up — now you’re paying the price."
            }
            return CrimeResult(
                title = title,
                description = desc,
                moneyGained = c.moneyGained ?: 0,
                moneySeized = if (c.caught == true && c.success == false) -(c.moneyGained ?: 0) else 0,
                isSuccess = c.success ?: false,
                wasCaught = c.caught ?: false,
                jailTime = c.actualJailTime ?: 0
            )
        }
    }
}

/* ----------- Labels / payouts / risk helpers (kept local to this file) ----------- */

private fun getCrimeName(type: CrimeViewModel.CrimeType): String = when (type) {
    CrimeViewModel.CrimeType.PICKPOCKETING -> "Pickpocketing"
    CrimeViewModel.CrimeType.SHOPLIFTING -> "Shoplifting"
    CrimeViewModel.CrimeType.VANDALISM -> "Vandalism"
    CrimeViewModel.CrimeType.PETTY_SCAM -> "Petty Scam"
    CrimeViewModel.CrimeType.MUGGING -> "Mugging"
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> "Breaking & Entering"
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

private fun getCrimeDesc(type: CrimeViewModel.CrimeType): String = when (type) {
    CrimeViewModel.CrimeType.PICKPOCKETING -> "Steal wallets in crowded places."
    CrimeViewModel.CrimeType.SHOPLIFTING -> "Lift small items from stores."
    CrimeViewModel.CrimeType.VANDALISM -> "Damage property for kicks."
    CrimeViewModel.CrimeType.PETTY_SCAM -> "Run a tiny con for quick cash."
    CrimeViewModel.CrimeType.MUGGING -> "Rob unsuspecting victims."
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> "Slip into locked places quietly."
    CrimeViewModel.CrimeType.DRUG_DEALING -> "Sell small batches on the street."
    CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> "Move knock-offs for a profit."
    CrimeViewModel.CrimeType.BURGLARY -> "Clean out a residence or office."
    CrimeViewModel.CrimeType.FRAUD -> "Paper crime with big upside."
    CrimeViewModel.CrimeType.ARMS_SMUGGLING -> "Move illegal weapons through borders."
    CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> "Move narcotics at scale."
    CrimeViewModel.CrimeType.ARMED_ROBBERY -> "Go loud and take the haul."
    CrimeViewModel.CrimeType.EXTORTION -> "Force payment through threats."
    CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM -> "High stakes abduction."
    CrimeViewModel.CrimeType.PONZI_SCHEME -> "Recruit, promise, collapse."
    CrimeViewModel.CrimeType.CONTRACT_KILLING -> "Eliminate a target."
    CrimeViewModel.CrimeType.DARK_WEB_SALES -> "Sell illicit wares online."
    CrimeViewModel.CrimeType.ART_THEFT -> "Steal priceless art."
    CrimeViewModel.CrimeType.DIAMOND_HEIST -> "Grand-scale jewel job."
}

private fun getCrimeDescShort(full: String): String =
    if (full.length <= 36) full else full.take(33) + "…"

private fun getCrimeRiskTier(type: CrimeViewModel.CrimeType): RiskTier = when (type) {
    CrimeViewModel.CrimeType.PICKPOCKETING,
    CrimeViewModel.CrimeType.SHOPLIFTING,
    CrimeViewModel.CrimeType.VANDALISM,
    CrimeViewModel.CrimeType.PETTY_SCAM -> RiskTier.LOW_RISK

    CrimeViewModel.CrimeType.MUGGING,
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING,
    CrimeViewModel.CrimeType.DRUG_DEALING,
    CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> RiskTier.MEDIUM_RISK

    CrimeViewModel.CrimeType.BURGLARY,
    CrimeViewModel.CrimeType.FRAUD,
    CrimeViewModel.CrimeType.ARMS_SMUGGLING,
    CrimeViewModel.CrimeType.DRUG_TRAFFICKING,
    CrimeViewModel.CrimeType.ARMED_ROBBERY,
    CrimeViewModel.CrimeType.EXTORTION,
    CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM,
    CrimeViewModel.CrimeType.PONZI_SCHEME -> RiskTier.HIGH_RISK

    CrimeViewModel.CrimeType.CONTRACT_KILLING,
    CrimeViewModel.CrimeType.DARK_WEB_SALES,
    CrimeViewModel.CrimeType.ART_THEFT,
    CrimeViewModel.CrimeType.DIAMOND_HEIST -> RiskTier.EXTREME_RISK
}

private fun getCrimePayoutMin(type: CrimeViewModel.CrimeType): Int = when (type) {
    CrimeViewModel.CrimeType.PICKPOCKETING -> 20
    CrimeViewModel.CrimeType.SHOPLIFTING -> 50
    CrimeViewModel.CrimeType.VANDALISM -> 10
    CrimeViewModel.CrimeType.PETTY_SCAM -> 30
    CrimeViewModel.CrimeType.MUGGING -> 100
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> 500
    CrimeViewModel.CrimeType.DRUG_DEALING -> 200
    CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> 300
    CrimeViewModel.CrimeType.BURGLARY -> 1200
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

private fun getCrimePayoutMax(type: CrimeViewModel.CrimeType): Int = when (type) {
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
    CrimeViewModel.CrimeType.ART_THEFT -> 4_800_000
    CrimeViewModel.CrimeType.DIAMOND_HEIST -> 9_500_000
}

private fun tierLabel(t: RiskTier) = when (t) {
    RiskTier.LOW_RISK -> "low-risk"
    RiskTier.MEDIUM_RISK -> "medium-risk"
    RiskTier.HIGH_RISK -> "high-risk"
    RiskTier.EXTREME_RISK -> "extreme"
}

private fun fmt(v: Int): String =
    NumberFormat.getCurrencyInstance(Locale.US).format(v)

private fun fmtSigned(v: Int): String =
    (if (v >= 0) "+" else "−") + NumberFormat.getCurrencyInstance(Locale.US).format(kotlin.math.abs(v))
