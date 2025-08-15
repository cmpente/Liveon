// app/src/main/java/com/liveongames/liveon/ui/screens/CrimeScreen.kt
package com.liveongames.liveon.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.liveongames.domain.model.RiskTier
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LocalLiveonTheme
import com.liveongames.liveon.viewmodel.CrimeViewModel
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* =========================================================================================
 * Crime screen — inline action flow (no popup dialog)
 * ========================================================================================= */

@Composable
fun CrimeScreen(
    viewModel: CrimeViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {},
    onCrimeCommitted: () -> Unit = {}
) {
    val t = LocalLiveonTheme.current
    val notoriety by viewModel.playerNotoriety.collectAsState()
    val (cooldownActive, _) = rememberCooldownState(viewModel)
    val lastOutcome by viewModel.lastOutcome.collectAsState()
    var policeFlash by remember { mutableStateOf(false) }

    // Brief police flash overlay on failed outcome
    LaunchedEffect(lastOutcome) {
        lastOutcome?.let { outcome ->
            if (!outcome.success) {
                policeFlash = true
                delay(1400)
                policeFlash = false
            }
            viewModel.consumeOutcome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
    ) {
        // Bottom sheet
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
                ) { /* block propagation */ },
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
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = t.text.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Designation panel
                item {
                    PanelCard {
                        Text(
                            "Criminal Designation",
                            color = t.text.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            rankForNotoriety(notoriety),
                            color = t.text,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        val next = nextRankInfo(notoriety)
                        if (next != null) {
                            Text(
                                "Next: ${next.first} in ${next.second} notoriety",
                                color = t.text.copy(alpha = 0.75f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Accordion sections
                items(buildCrimeCatalog()) { cat ->
                    AccordionSection(
                        title = cat.title,
                        subTitle = cat.subtitle,
                        initiallyExpanded = false,
                        containerColor = t.surfaceElevated,
                        textColor = t.text,
                        accent = t.primary
                    ) {
                        cat.subs.forEachIndexed { sIdx, sub ->
                            if (sub.title.isNotBlank()) {
                                SubCategoryHeader(label = sub.title, textColor = t.text)
                            }
                            Surface(
                                color = t.surface,
                                shape = RoundedCornerShape(16.dp),
                                tonalElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(vertical = 4.dp)) {
                                    sub.items.forEachIndexed { iIdx, item ->
                                        val tier = getCrimeRiskTier(item.type)
                                        val locked = notoriety < getCrimeNotorietyRequired(tier)
                                        val disabled = cooldownActive || locked

                                        CrimeListItem(
                                            type = item.type,
                                            enabled = !disabled,
                                            textColor = t.text,
                                            accent = when (tier) {
                                                RiskTier.LOW_RISK -> Color(0xFF2ECC71)
                                                RiskTier.MEDIUM_RISK -> Color(0xFFFFC107)
                                                RiskTier.HIGH_RISK -> Color(0xFFFF7043)
                                                RiskTier.EXTREME_RISK -> Color(0xFFE53935)
                                            },
                                            tier = tier,
                                            onStart = { durationMs ->
                                                // Start the crime + cooldown
                                                viewModel.commitCrime(item.type)
                                                viewModel.startGlobalCooldown(durationMs)
                                                onCrimeCommitted()
                                            },
                                            previewScenario = { viewModel.previewScenario(item.type) }
                                        )

                                        if (iIdx != sub.items.lastIndex) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 12.dp),
                                                color = t.surfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                            if (sIdx != cat.subs.lastIndex) Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }

        // Police flash overlay on failure (use alpha() instead of Brush.copy(...))
        AnimatedVisibility(
            visible = policeFlash,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(rememberSirenBrush(true))
                    .alpha(0.22f)
            )
        }
    }
}

/* ============================== Row + Inline Action ============================== */

@Composable
private fun CrimeListItem(
    type: CrimeViewModel.CrimeType,
    enabled: Boolean,
    textColor: Color,
    accent: Color,
    tier: RiskTier,
    onStart: (durationMs: Long) -> Unit,
    previewScenario: () -> String
) {
    val title = getCrimeName(type)
    val subtitle = getCrimeDescShort(getCrimeDesc(type))

    var expanded by rememberSaveable(type) { mutableStateOf(false) }
    var inProgress by rememberSaveable(type) { mutableStateOf(false) }
    var progress by rememberSaveable(type) { mutableStateOf(0f) }
    var scenarioLine by rememberSaveable(type) { mutableStateOf<String?>(null) }

    val durationMs = CrimeViewModel.cooldownForTier(tier)
    val scope = rememberCoroutineScope()

    Column {
        CrimeRow(
            iconRes = getCrimeIconRes(type),
            title = title,
            subtitle = subtitle,
            enabled = enabled,
            textColor = textColor,
            accent = accent
        ) {
            if (enabled) expanded = !expanded
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (!inProgress) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                scenarioLine = previewScenario()
                                inProgress = true
                                progress = 0f
                                expanded = true

                                onStart(durationMs)

                                scope.launch {
                                    val start = System.currentTimeMillis()
                                    while (true) {
                                        val now = System.currentTimeMillis()
                                        val frac = ((now - start)
                                            .coerceAtLeast(0L)
                                            .toFloat() / durationMs.toFloat())
                                            .coerceIn(0f, 1f)
                                        progress = frac
                                        if (frac >= 1f) break
                                        delay(50)
                                    }
                                    inProgress = false
                                }
                            },
                            enabled = enabled
                        ) { Text("Continue") }

                        Spacer(Modifier.width(8.dp))

                        TextButton(onClick = { expanded = false }) {
                            Text("Back out")
                        }
                    }
                } else {
                    // In-progress UI
                    scenarioLine?.let {
                        Text(
                            it,
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${(progress * 100).toInt()}%",
                        color = textColor.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

/* ============================== UI pieces ============================== */

@Composable
private fun PanelCard(content: @Composable ColumnScope.() -> Unit) {
    val t = LocalLiveonTheme.current
    Surface(
        color = t.surfaceElevated,
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun AccordionSection(
    title: String,
    subTitle: String? = null,
    initiallyExpanded: Boolean = false,
    containerColor: Color,
    textColor: Color,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by rememberSaveable(title) { mutableStateOf(initiallyExpanded) }
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (subTitle != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(subTitle, color = textColor.copy(alpha = 0.70f), fontSize = 13.sp)
                    }
                }
                Icon(
                    painter = painterResource(
                        id = if (expanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
                    ),
                    contentDescription = null,
                    tint = textColor
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)),
                exit = fadeOut() + shrinkVertically(animationSpec = tween(180, easing = FastOutSlowInEasing))
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), content = content)
            }
        }
    }
}

@Composable
private fun SubCategoryHeader(label: String, textColor: Color) {
    if (label.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = textColor.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CrimeRow(
    iconRes: Int,
    title: String,
    subtitle: String,
    enabled: Boolean,
    textColor: Color,
    accent: Color,
    onClick: () -> Unit
) {
    val rowAlpha = if (enabled) 1f else 0.55f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(25.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(rowAlpha)
        ) {
            Text(
                title,
                color = textColor,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                subtitle,
                color = textColor.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        // Chevron intentionally removed from each crime row
    }
}

/* ============================== Catalog (renamed categories) ============================== */

private data class CrimeUiItem(val type: CrimeViewModel.CrimeType)
private data class CrimeSubcat(val title: String, val items: List<CrimeUiItem>)
private data class CrimeCat(val title: String, val subtitle: String, val subs: List<CrimeSubcat>)

private fun buildCrimeCatalog(): List<CrimeCat> {
    return listOf(
        CrimeCat(
            title = "Street Crimes",
            subtitle = "Safe but modest gains",
            subs = listOf(
                CrimeSubcat(
                    title = "", // blank per request
                    items = listOf(
                        CrimeUiItem(CrimeViewModel.CrimeType.PICKPOCKETING),
                        CrimeUiItem(CrimeViewModel.CrimeType.SHOPLIFTING),
                        CrimeUiItem(CrimeViewModel.CrimeType.VANDALISM),
                        CrimeUiItem(CrimeViewModel.CrimeType.PETTY_SCAM)
                    )
                )
            )
        ),
        CrimeCat(
            title = "Robbery",
            subtitle = "Bigger rewards, higher danger",
            subs = listOf(
                CrimeSubcat(
                    title = "", // blank per request
                    items = listOf(
                        CrimeUiItem(CrimeViewModel.CrimeType.MUGGING),
                        CrimeUiItem(CrimeViewModel.CrimeType.BREAKING_AND_ENTERING),
                        CrimeUiItem(CrimeViewModel.CrimeType.DRUG_DEALING),
                        CrimeUiItem(CrimeViewModel.CrimeType.COUNTERFEIT_GOODS)
                    )
                )
            )
        ),
        CrimeCat(
            title = "Heists & Smuggling",
            subtitle = "High stakes, serious time",
            subs = listOf(
                CrimeSubcat(
                    title = "", // blank per request
                    items = listOf(
                        CrimeUiItem(CrimeViewModel.CrimeType.BURGLARY),
                        CrimeUiItem(CrimeViewModel.CrimeType.FRAUD),
                        CrimeUiItem(CrimeViewModel.CrimeType.ARMS_SMUGGLING),
                        CrimeUiItem(CrimeViewModel.CrimeType.DRUG_TRAFFICKING),
                        CrimeUiItem(CrimeViewModel.CrimeType.ARMED_ROBBERY),
                        CrimeUiItem(CrimeViewModel.CrimeType.EXTORTION),
                        CrimeUiItem(CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM),
                        CrimeUiItem(CrimeViewModel.CrimeType.PONZI_SCHEME)
                    )
                )
            )
        ),
        CrimeCat(
            title = "Mastermind Tier",
            subtitle = "Elite jobs, massive risk",
            subs = listOf(
                CrimeSubcat(
                    title = "", // blank per request
                    items = listOf(
                        CrimeUiItem(CrimeViewModel.CrimeType.CONTRACT_KILLING),
                        CrimeUiItem(CrimeViewModel.CrimeType.DARK_WEB_SALES),
                        CrimeUiItem(CrimeViewModel.CrimeType.ART_THEFT),
                        CrimeUiItem(CrimeViewModel.CrimeType.DIAMOND_HEIST)
                    )
                )
            )
        )
    )
}

/* ============================== Cooldown & siren brush ============================== */

@Composable
private fun rememberCooldownState(vm: CrimeViewModel): Pair<Boolean, Int> {
    val until by vm.cooldownUntil.collectAsState()
    val now = System.currentTimeMillis()
    return if (until != null && until!! > now) {
        true to (((until!! - now) / 1000).toInt().coerceAtLeast(0))
    } else false to 0
}

@Composable
private fun rememberSirenBrush(enabled: Boolean): Brush {
    if (!enabled) {
        return Brush.linearGradient(
            colors = listOf(Color(0xFF3A9BDC), Color(0xFF9ED1FF), Color(0xFF3A9BDC)),
            start = Offset.Zero,
            end = Offset(300f, 0f)
        )
    }
    val transition = rememberInfiniteTransition(label = "siren-transition")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "siren-shift"
    )
    val start = Offset(shift * 400f - 200f, 0f)
    val end = Offset(shift * 400f, 200f)
    return Brush.linearGradient(
        colors = listOf(
            Color(0xFF3A9BDC), Color(0xFF9ED1FF), Color(0xFF3A9BDC),
            Color(0xFFFF4C4C), Color(0xFFFFA3A3), Color(0xFFFF4C4C),
            Color(0xFF3A9BDC)
        ),
        start = start,
        end = end
    )
}

/* ============================== Label/Value helpers (fully implemented) ============================== */

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
    CrimeViewModel.CrimeType.PICKPOCKETING -> "Lift a wallet or phone from an unsuspecting mark."
    CrimeViewModel.CrimeType.SHOPLIFTING -> "Swipe small items from a retail store."
    CrimeViewModel.CrimeType.VANDALISM -> "Deface property to make a statement."
    CrimeViewModel.CrimeType.PETTY_SCAM -> "Run a small con for quick cash."
    CrimeViewModel.CrimeType.MUGGING -> "Threaten and rob a mark."
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> "Slip into a home or shop."
    CrimeViewModel.CrimeType.DRUG_DEALING -> "Move product to street buyers."
    CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> "Sell knock-offs to eager buyers."
    CrimeViewModel.CrimeType.BURGLARY -> "Hit higher-value targets with planning."
    CrimeViewModel.CrimeType.FRAUD -> "Forge, skim, and siphon funds."
    CrimeViewModel.CrimeType.ARMS_SMUGGLING -> "Move illegal weapons between buyers."
    CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> "Transport larger shipments for cartels."
    CrimeViewModel.CrimeType.ARMED_ROBBERY -> "High-stakes robbery with force."
    CrimeViewModel.CrimeType.EXTORTION -> "Coerce payment with threats."
    CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM -> "Abduct and negotiate payment."
    CrimeViewModel.CrimeType.PONZI_SCHEME -> "Pay old investors with new money."
    CrimeViewModel.CrimeType.CONTRACT_KILLING -> "Eliminate a target for a fee."
    CrimeViewModel.CrimeType.DARK_WEB_SALES -> "Move illegal goods through online markets."
    CrimeViewModel.CrimeType.ART_THEFT -> "Steal priceless works of art."
    CrimeViewModel.CrimeType.DIAMOND_HEIST -> "Rob vaults and transports for diamonds."
}

private fun getCrimeDescShort(full: String): String =
    if (full.length <= 36) full else full.take(33) + "…"

private fun getCrimeRiskTier(type: CrimeViewModel.CrimeType): RiskTier = when (type) {
    // STREET CRIMES
    CrimeViewModel.CrimeType.PICKPOCKETING,
    CrimeViewModel.CrimeType.SHOPLIFTING,
    CrimeViewModel.CrimeType.VANDALISM,
    CrimeViewModel.CrimeType.PETTY_SCAM -> RiskTier.LOW_RISK

    // ROBBERY CRIMES
    CrimeViewModel.CrimeType.MUGGING,
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING,
    CrimeViewModel.CrimeType.DRUG_DEALING,
    CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> RiskTier.MEDIUM_RISK

    // HEISTS & SMUGGLING
    CrimeViewModel.CrimeType.BURGLARY,
    CrimeViewModel.CrimeType.FRAUD,
    CrimeViewModel.CrimeType.ARMS_SMUGGLING,
    CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> RiskTier.HIGH_RISK

    // MASTERMIND
    CrimeViewModel.CrimeType.ARMED_ROBBERY,
    CrimeViewModel.CrimeType.EXTORTION,
    CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM,
    CrimeViewModel.CrimeType.PONZI_SCHEME,
    CrimeViewModel.CrimeType.CONTRACT_KILLING,
    CrimeViewModel.CrimeType.DARK_WEB_SALES,
    CrimeViewModel.CrimeType.ART_THEFT,
    CrimeViewModel.CrimeType.DIAMOND_HEIST -> RiskTier.EXTREME_RISK
}

private fun getCrimeNotorietyRequired(tier: RiskTier): Int = when (tier) {
    RiskTier.LOW_RISK -> 0
    RiskTier.MEDIUM_RISK -> 10
    RiskTier.HIGH_RISK -> 28
    RiskTier.EXTREME_RISK -> 52
}

private fun getCrimePayoutMin(type: CrimeViewModel.CrimeType): Int = when (type) {
    // STREET CRIMES
    CrimeViewModel.CrimeType.PICKPOCKETING -> 20
    CrimeViewModel.CrimeType.SHOPLIFTING -> 30
    CrimeViewModel.CrimeType.VANDALISM -> 0
    CrimeViewModel.CrimeType.PETTY_SCAM -> 30

    // ROBBERY CRIMES
    CrimeViewModel.CrimeType.MUGGING -> 80
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> 120
    CrimeViewModel.CrimeType.DRUG_DEALING -> 150
    CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> 120

    // HEISTS & SMUGGLING
    CrimeViewModel.CrimeType.BURGLARY -> 600
    CrimeViewModel.CrimeType.FRAUD -> 500
    CrimeViewModel.CrimeType.ARMS_SMUGGLING -> 1000
    CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> 1200

    // MASTERMIND
    CrimeViewModel.CrimeType.ARMED_ROBBERY -> 2000
    CrimeViewModel.CrimeType.EXTORTION -> 1500
    CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM -> 5000
    CrimeViewModel.CrimeType.PONZI_SCHEME -> 3000
    CrimeViewModel.CrimeType.CONTRACT_KILLING -> 8000
    CrimeViewModel.CrimeType.DARK_WEB_SALES -> 1200
    CrimeViewModel.CrimeType.ART_THEFT -> 5000
    CrimeViewModel.CrimeType.DIAMOND_HEIST -> 10000
}

private fun getCrimePayoutMax(type: CrimeViewModel.CrimeType): Int = when (type) {
    // STREET CRIMES
    CrimeViewModel.CrimeType.PICKPOCKETING -> 180
    CrimeViewModel.CrimeType.SHOPLIFTING -> 220
    CrimeViewModel.CrimeType.VANDALISM -> 80
    CrimeViewModel.CrimeType.PETTY_SCAM -> 250

    // ROBBERY CRIMES
    CrimeViewModel.CrimeType.MUGGING -> 500
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> 800
    CrimeViewModel.CrimeType.DRUG_DEALING -> 1200
    CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> 900

    // HEISTS & SMUGGLING
    CrimeViewModel.CrimeType.BURGLARY -> 3500
    CrimeViewModel.CrimeType.FRAUD -> 4000
    CrimeViewModel.CrimeType.ARMS_SMUGGLING -> 6000
    CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> 7000

    // MASTERMIND
    CrimeViewModel.CrimeType.ARMED_ROBBERY -> 12000
    CrimeViewModel.CrimeType.EXTORTION -> 9000
    CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM -> 30000
    CrimeViewModel.CrimeType.PONZI_SCHEME -> 20000
    CrimeViewModel.CrimeType.CONTRACT_KILLING -> 50000
    CrimeViewModel.CrimeType.DARK_WEB_SALES -> 15000
    CrimeViewModel.CrimeType.ART_THEFT -> 40000
    CrimeViewModel.CrimeType.DIAMOND_HEIST -> 100000
}

private fun fmt(v: Int): String =
    NumberFormat.getCurrencyInstance(Locale.US).format(v)

private fun rankForNotoriety(n: Int): String = when {
    n < 10 -> "Rookie"
    n < 28 -> "Street Hustler"
    n < 40 -> "Robber"
    n < 52 -> "Runner"
    n < 66 -> "Crew Boss"
    n < 75 -> "Fixer"
    else -> "Mastermind"
}

private fun nextRankInfo(n: Int): Pair<String, Int>? = when {
    n < 10 -> "Street Hustler" to (10 - n)
    n < 28 -> "Robber" to (28 - n)
    n < 40 -> "Runner" to (40 - n)
    n < 52 -> "Crew Boss" to (52 - n)
    n < 66 -> "Fixer" to (66 - n)
    n < 75 -> "Mastermind" to (75 - n)
    else -> null
}

// Replace the whole function with this:
private fun getCrimeIconRes(type: CrimeViewModel.CrimeType): Int = when (type) {
    // STREET CRIMES
    CrimeViewModel.CrimeType.PICKPOCKETING         -> R.drawable.ic_pickpocket
    CrimeViewModel.CrimeType.SHOPLIFTING           -> R.drawable.ic_shoplifting   // (note: file is 'shoplifting' spelled 'shoplifting'? you have ic_shoplifting.xml)
    CrimeViewModel.CrimeType.VANDALISM             -> R.drawable.ic_vandalism
    CrimeViewModel.CrimeType.PETTY_SCAM            -> R.drawable.ic_petty_scam

    // ROBBERY CRIMES
    CrimeViewModel.CrimeType.MUGGING               -> R.drawable.ic_mugging
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> R.drawable.ic_break_and_enter
    CrimeViewModel.CrimeType.DRUG_DEALING          -> R.drawable.ic_drug_deal
    CrimeViewModel.CrimeType.COUNTERFEIT_GOODS     -> R.drawable.ic_counterfeit_goods

    // HEISTS & SMUGGLING
    CrimeViewModel.CrimeType.BURGLARY              -> R.drawable.ic_burglary
    CrimeViewModel.CrimeType.FRAUD                 -> R.drawable.ic_fraud
    CrimeViewModel.CrimeType.ARMS_SMUGGLING        -> R.drawable.ic_arms_smuggling
    CrimeViewModel.CrimeType.DRUG_TRAFFICKING      -> R.drawable.ic_drug_trafficking

    // MASTERMIND
    CrimeViewModel.CrimeType.ARMED_ROBBERY         -> R.drawable.ic_armed_robbery
    CrimeViewModel.CrimeType.EXTORTION             -> R.drawable.ic_extortion
    CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM -> R.drawable.ic_kidnapping
    CrimeViewModel.CrimeType.PONZI_SCHEME          -> R.drawable.ic_ponzi
    CrimeViewModel.CrimeType.CONTRACT_KILLING      -> R.drawable.ic_contract_killing
    CrimeViewModel.CrimeType.DARK_WEB_SALES        -> R.drawable.ic_dark_web
    CrimeViewModel.CrimeType.ART_THEFT             -> R.drawable.ic_art_theft
    CrimeViewModel.CrimeType.DIAMOND_HEIST         -> R.drawable.ic_diamond_heist
}