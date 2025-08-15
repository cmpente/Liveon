// app/src/main/java/com/liveongames/liveon/ui/screens/CrimeScreen.kt
package com.liveongames.liveon.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.domain.model.RiskTier
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LocalLiveonTheme
import com.liveongames.liveon.viewmodel.CrimeViewModel
import kotlinx.coroutines.delay

/* =========================================================================================
 * Crime screen — inline action flow (no popup dialog), styled to match Education
 * ========================================================================================= */

// Sheet + grid constants (match Education)
private val SheetRadius = 24.dp
private val SheetShadow = 22.dp
private val SheetHPad = 14.dp

// Row layout
private val IconSize = 25.dp           // icons ~25% larger
private val GapAfterIcon = 12.dp
private val ExpandedIndent = SheetHPad + IconSize + GapAfterIcon // aligns expanded content under title column

@Composable
fun CrimeScreen(
    viewModel: CrimeViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {},
    onCrimeCommitted: () -> Unit = {}
) {
    val t = LocalLiveonTheme.current
    val notoriety by viewModel.playerNotoriety.collectAsState()
    val cooldownUntil by viewModel.cooldownUntil.collectAsState()
    val runState by viewModel.runState.collectAsState()
    val lastOutcome by viewModel.lastOutcome.collectAsState()

    var policeFlash by remember { mutableStateOf(false) }

    // Police flash on failure; consume one-shot
    LaunchedEffect(lastOutcome) {
        lastOutcome?.let { out ->
            if (!out.success) {
                policeFlash = true
                delay(1400)
                policeFlash = false
            }
            onCrimeCommitted()
            viewModel.consumeOutcome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .shadow(SheetShadow, RoundedCornerShape(topStart = SheetRadius, topEnd = SheetRadius))
                .clip(RoundedCornerShape(topStart = SheetRadius, topEnd = SheetRadius))
                .background(t.surface)
                .padding(horizontal = SheetHPad, vertical = 10.dp)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { },
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Text("Crimes", color = t.text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)

            // Designation card + cooldown badge (right)
            Surface(
                color = t.surfaceElevated,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SheetHPad, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Designation", color = t.text.copy(alpha = 0.8f), fontSize = 12.sp)
                        val info = nextRankInfo(notoriety)
                        val line = if (info != null) {
                            val (rankName, toNext) = info
                            "${rankForNotoriety(notoriety)} • ${if (toNext > 0) "$toNext to $rankName" else "Peak standing"}"
                        } else {
                            "${rankForNotoriety(notoriety)} • Peak standing"
                        }
                        Text(line, color = t.text, fontSize = 14.sp)
                    }
                    CooldownBadge(cooldownUntil)
                }
            }

            // Catalog
            val cats = remember { buildCrimeCatalog() }
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cats, key = { it.title }) { cat ->
                    Accordion(
                        title = cat.title,
                        subTitle = cat.subtitle,
                        containerColor = t.surfaceElevated,
                        textColor = t.text
                    ) {
                        for ((sIdx, sub) in cat.subs.withIndex()) {
                            SubCategoryHeader(sub.title, t.text)
                            for ((iIdx, item) in sub.items.withIndex()) {
                                val tier = getCrimeRiskTier(item.type)
                                val disabled = cooldownUntil?.let { it > System.currentTimeMillis() } == true
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
                                    runState = if (runState?.type == item.type) runState else null,
                                    onContinue = { viewModel.beginCrime(item.type) },
                                    onCancel = { viewModel.cancelCrime() }
                                )
                                if (iIdx != sub.items.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = ExpandedIndent, end = SheetHPad),
                                        color = t.surfaceVariant
                                    )
                                }
                            }
                            if (sIdx != cat.subs.lastIndex) Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }

        // Police flash overlay on failure
        AnimatedVisibility(visible = policeFlash, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(rememberSirenBrush(true))
                    .alpha(0.22f)
            )
        }
    }
}

/* ============================== Accordion / Rows ============================== */

@Composable
private fun Accordion(
    title: String,
    subTitle: String? = null,
    initiallyExpanded: Boolean = false,
    containerColor: Color,
    textColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    var expanded by rememberSaveable(title) { mutableStateOf(initiallyExpanded) }
    Surface(color = containerColor, shape = shape, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(title, color = textColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    if (subTitle != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(subTitle, color = textColor.copy(alpha = 0.70f), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Icon(
                    painter = painterResource(id = if (expanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more),
                    contentDescription = null,
                    tint = textColor
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)),
                exit = fadeOut() + shrinkVertically(animationSpec = tween(180, easing = FastOutSlowInEasing))
            ) {
                Column(Modifier.padding(vertical = 8.dp), content = content)
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
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = SheetHPad),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(IconSize)
        )
        Spacer(Modifier.width(GapAfterIcon))
        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(rowAlpha)
        ) {
            Text(title, color = textColor, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = textColor.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        // Chevron intentionally removed
    }
}

/* Row with inline “continue/back out” -> in-progress UI */
@Composable
private fun CrimeListItem(
    type: CrimeViewModel.CrimeType,
    enabled: Boolean,
    textColor: Color,
    accent: Color,
    tier: RiskTier,
    runState: CrimeViewModel.CrimeRunState?,
    onContinue: () -> Unit,
    onCancel: () -> Unit
) {
    val title = getCrimeName(type)
    val subtitle = getCrimeDescShort(getCrimeDesc(type)) // amounts removed

    var expanded by rememberSaveable(type) { mutableStateOf(false) }

    Column {
        CrimeRow(
            iconRes = getCrimeIconRes(type),
            title = title,
            subtitle = subtitle,
            enabled = enabled,
            textColor = textColor,
            accent = accent
        ) {
            if (enabled || runState != null) expanded = !expanded
        }

        AnimatedVisibility(visible = expanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = ExpandedIndent, end = SheetHPad, top = 8.dp, bottom = 8.dp)
            ) {
                if (runState == null) {
                    // Not started yet — Continue / Back out
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = onContinue, enabled = enabled) { Text("Continue") }
                        Spacer(Modifier.width(12.dp))
                        TextButton(onClick = { expanded = false }, enabled = enabled) { Text("Back out") }
                    }
                } else {
                    // In progress — centered narration + progress bar + Cancel
                    Text(
                        text = runState.currentMessage,
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 4.dp),
                        textAlign = TextAlign.Center,
                        softWrap = true,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = runState.progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        trackColor = textColor.copy(alpha = 0.15f),
                        color = accent
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            when (runState.phase) {
                                CrimeViewModel.Phase.SETUP -> "Setting up…"
                                CrimeViewModel.Phase.EXECUTION -> "In motion…"
                                CrimeViewModel.Phase.CLIMAX -> "Climax…"
                            },
                            color = textColor.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = onCancel) { Text("Cancel") }
                    }
                }
            }
        }
    }
}

/* ============================== Catalog (renamed categories) ============================== */

private data class CrimeUiItem(val type: CrimeViewModel.CrimeType)
private data class CrimeSubcat(val title: String, val items: List<CrimeUiItem>)
private data class CrimeCat(val title: String, val subtitle: String, val subs: List<CrimeSubcat>)

private fun buildCrimeCatalog(): List<CrimeCat> = listOf(
    CrimeCat(
        title = "Street Crimes",
        subtitle = "Safe but modest gains",
        subs = listOf(
            CrimeSubcat(
                title = "",
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
                title = "",
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
                title = "",
                items = listOf(
                    CrimeUiItem(CrimeViewModel.CrimeType.BURGLARY),
                    CrimeUiItem(CrimeViewModel.CrimeType.FRAUD),
                    CrimeUiItem(CrimeViewModel.CrimeType.ARMS_SMUGGLING),
                    CrimeUiItem(CrimeViewModel.CrimeType.DRUG_TRAFFICKING)
                )
            )
        )
    ),
    CrimeCat(
        title = "Mastermind Tier",
        subtitle = "Elite jobs, massive risk",
        subs = listOf(
            CrimeSubcat(
                title = "",
                items = listOf(
                    CrimeUiItem(CrimeViewModel.CrimeType.ARMED_ROBBERY),
                    CrimeUiItem(CrimeViewModel.CrimeType.EXTORTION),
                    CrimeUiItem(CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM),
                    CrimeUiItem(CrimeViewModel.CrimeType.PONZI_SCHEME),
                    CrimeUiItem(CrimeViewModel.CrimeType.CONTRACT_KILLING),
                    CrimeUiItem(CrimeViewModel.CrimeType.DARK_WEB_SALES),
                    CrimeUiItem(CrimeViewModel.CrimeType.ART_THEFT),
                    CrimeUiItem(CrimeViewModel.CrimeType.DIAMOND_HEIST)
                )
            )
        )
    )
)

/* ============================== Labels / Numbers / Icons ============================== */

private fun getCrimeName(type: CrimeViewModel.CrimeType) = when (type) {
    CrimeViewModel.CrimeType.PICKPOCKETING -> "Pickpocketing"
    CrimeViewModel.CrimeType.SHOPLIFTING -> "Shoplifting"
    CrimeViewModel.CrimeType.VANDALISM -> "Vandalism"
    CrimeViewModel.CrimeType.PETTY_SCAM -> "Petty scam"
    CrimeViewModel.CrimeType.MUGGING -> "Mugging"
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> "Breaking & entering"
    CrimeViewModel.CrimeType.DRUG_DEALING -> "Drug dealing"
    CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> "Counterfeit goods"
    CrimeViewModel.CrimeType.BURGLARY -> "Burglary"
    CrimeViewModel.CrimeType.FRAUD -> "Fraud"
    CrimeViewModel.CrimeType.ARMS_SMUGGLING -> "Arms smuggling"
    CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> "Drug trafficking"
    CrimeViewModel.CrimeType.ARMED_ROBBERY -> "Armed robbery"
    CrimeViewModel.CrimeType.EXTORTION -> "Extortion"
    CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM -> "Kidnapping for ransom"
    CrimeViewModel.CrimeType.PONZI_SCHEME -> "Ponzi scheme"
    CrimeViewModel.CrimeType.CONTRACT_KILLING -> "Contract killing"
    CrimeViewModel.CrimeType.DARK_WEB_SALES -> "Dark web sales"
    CrimeViewModel.CrimeType.ART_THEFT -> "Art theft"
    CrimeViewModel.CrimeType.DIAMOND_HEIST -> "Diamond heist"
}

private fun getCrimeDesc(type: CrimeViewModel.CrimeType) = when (type) {
    CrimeViewModel.CrimeType.PICKPOCKETING -> "Lift a wallet or phone."
    CrimeViewModel.CrimeType.SHOPLIFTING -> "Swipe small items from a store."
    CrimeViewModel.CrimeType.VANDALISM -> "Deface property to make a statement."
    CrimeViewModel.CrimeType.PETTY_SCAM -> "Run a street con."
    CrimeViewModel.CrimeType.MUGGING -> "Corner a mark and demand valuables."
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> "Slip into a building."
    CrimeViewModel.CrimeType.DRUG_DEALING -> "Move small product to buyers."
    CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> "Sell convincing fakes."
    CrimeViewModel.CrimeType.BURGLARY -> "Hit a residence or business."
    CrimeViewModel.CrimeType.FRAUD -> "Confidence games at scale."
    CrimeViewModel.CrimeType.ARMS_SMUGGLING -> "Move weapons quietly."
    CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> "Transport heavy product."
    CrimeViewModel.CrimeType.ARMED_ROBBERY -> "High-stakes, high-risk robbery."
    CrimeViewModel.CrimeType.EXTORTION -> "Money by threat or pressure."
    CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM -> "Abduct and negotiate."
    CrimeViewModel.CrimeType.PONZI_SCHEME -> "Pay old investors with new."
    CrimeViewModel.CrimeType.CONTRACT_KILLING -> "Assassination for hire."
    CrimeViewModel.CrimeType.DARK_WEB_SALES -> "Illicit marketplace hustle."
    CrimeViewModel.CrimeType.ART_THEFT -> "Steal priceless works."
    CrimeViewModel.CrimeType.DIAMOND_HEIST -> "Rob the vault."
}

private fun getCrimeDescShort(full: String): String =
    if (full.length <= 36) full else full.take(33) + "…"

private fun getCrimeRiskTier(type: CrimeViewModel.CrimeType): RiskTier = when (type) {
    // LOW
    CrimeViewModel.CrimeType.PICKPOCKETING,
    CrimeViewModel.CrimeType.SHOPLIFTING,
    CrimeViewModel.CrimeType.VANDALISM,
    CrimeViewModel.CrimeType.PETTY_SCAM -> RiskTier.LOW_RISK

    // MED
    CrimeViewModel.CrimeType.MUGGING,
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING,
    CrimeViewModel.CrimeType.DRUG_DEALING,
    CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> RiskTier.MEDIUM_RISK

    // HIGH
    CrimeViewModel.CrimeType.BURGLARY,
    CrimeViewModel.CrimeType.FRAUD,
    CrimeViewModel.CrimeType.ARMS_SMUGGLING,
    CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> RiskTier.HIGH_RISK

    // EXTREME
    else -> RiskTier.EXTREME_RISK
}

private fun rankForNotoriety(n: Int): String = when {
    n < 5 -> "New Face"
    n < 15 -> "Petty Thief"
    n < 30 -> "Street Hustler"
    n < 45 -> "Enforcer"
    n < 60 -> "Fixer"
    n < 75 -> "Shot Caller"
    n < 90 -> "Capo"
    else -> "Kingpin"
}

private fun nextRankInfo(n: Int): Pair<String, Int>? {
    val thresholds = listOf(
        5 to "Petty Thief",
        15 to "Street Hustler",
        30 to "Enforcer",
        45 to "Fixer",
        60 to "Shot Caller",
        75 to "Capo",
        90 to "Kingpin"
    )
    val next = thresholds.firstOrNull { n < it.first } ?: return null
    return rankForNotoriety(next.first) to (next.first - n)
}

private fun getCrimeIconRes(type: CrimeViewModel.CrimeType): Int = when (type) {
    // STREET
    CrimeViewModel.CrimeType.PICKPOCKETING -> R.drawable.ic_pickpocket
    CrimeViewModel.CrimeType.SHOPLIFTING -> R.drawable.ic_shoplifting
    CrimeViewModel.CrimeType.VANDALISM -> R.drawable.ic_vandalism
    CrimeViewModel.CrimeType.PETTY_SCAM -> R.drawable.ic_petty_scam
    // ROBBERY
    CrimeViewModel.CrimeType.MUGGING -> R.drawable.ic_mugging
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> R.drawable.ic_break_and_enter
    CrimeViewModel.CrimeType.DRUG_DEALING -> R.drawable.ic_drug_deal
    CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> R.drawable.ic_counterfeit_goods
    // HEISTS & SMUGGLING
    CrimeViewModel.CrimeType.BURGLARY -> R.drawable.ic_burglary
    CrimeViewModel.CrimeType.FRAUD -> R.drawable.ic_fraud
    CrimeViewModel.CrimeType.ARMS_SMUGGLING -> R.drawable.ic_arms_smuggling
    CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> R.drawable.ic_drug_trafficking
    // MASTERMIND
    CrimeViewModel.CrimeType.ARMED_ROBBERY -> R.drawable.ic_armed_robbery
    CrimeViewModel.CrimeType.EXTORTION -> R.drawable.ic_extortion
    CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM -> R.drawable.ic_kidnapping
    CrimeViewModel.CrimeType.PONZI_SCHEME -> R.drawable.ic_ponzi
    CrimeViewModel.CrimeType.CONTRACT_KILLING -> R.drawable.ic_contract_killing
    CrimeViewModel.CrimeType.DARK_WEB_SALES -> R.drawable.ic_dark_web
    CrimeViewModel.CrimeType.ART_THEFT -> R.drawable.ic_art_theft
    CrimeViewModel.CrimeType.DIAMOND_HEIST -> R.drawable.ic_diamond_heist
}

/* ============================== Badges & effects ============================== */

@Composable
private fun CooldownBadge(cooldownUntil: Long?) {
    val t = LocalLiveonTheme.current
    val active = cooldownUntil != null && cooldownUntil > System.currentTimeMillis()
    if (!active) return

    val secs = (((cooldownUntil!! - System.currentTimeMillis()) / 1000).toInt()).coerceAtLeast(0)

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = t.primary.copy(alpha = 0.15f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_hourglass),
                contentDescription = null,
                tint = t.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Locked ${secs}s",
                color = t.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
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
