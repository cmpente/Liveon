// app/src/main/java/com/liveongames/liveon/ui/screens/CrimeScreen.kt
package com.liveongames.liveon.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.domain.model.CrimeRecordEntry
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LocalLiveonTheme
import com.liveongames.liveon.viewmodel.CrimeViewModel
import kotlinx.coroutines.delay
import kotlin.math.pow
import com.liveongames.liveon.util.getCrimeName

/* =========================================================================================
 * Crime screen — aligned with Education:
 *  - Criminal card at top (avatar + rank), notoriety hidden.
 *  - Accordion of categories (first open by default, only one open at a time).
 *  - In‑row crime progression (phase lines + centered ambient + progress + cancel).
 *  - Dramatic outcome reveal (no notoriety shown).
 *  - Criminal Record section below the accordion.
 * ========================================================================================= */

@Composable
fun CrimeScreen(
    onDismiss: () -> Unit,
    viewModel: CrimeViewModel = hiltViewModel(),
    onCrimeCommitted: () -> Unit = {}   // <- add this
) {

    val t = LocalLiveonTheme.current

    val notoriety by viewModel.playerNotoriety.collectAsState()
    val cooldownUntil by viewModel.cooldownUntil.collectAsState()
    val runState by viewModel.runState.collectAsState()
    val lastOutcomeVm by viewModel.lastOutcome.collectAsState()
    val records by viewModel.criminalRecords.collectAsState()

    var revealedOutcome by remember { mutableStateOf<CrimeViewModel.OutcomeEvent?>(null) }
    var policeFlash by remember { mutableStateOf(false) }

    LaunchedEffect(lastOutcomeVm) {
        lastOutcomeVm?.let { out ->
            if (!out.success || out.wasCaught) { policeFlash = true; delay(1400); policeFlash = false }
            revealedOutcome = out
            onCrimeCommitted()            // <- notify caller to refresh stats
            viewModel.consumeOutcome()
        }
    }

    val isLocked = (cooldownUntil ?: 0L) > System.currentTimeMillis()

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
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(t.surface)
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {},
            verticalArrangement = Arrangement.Top
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Crime",
                    color = t.text,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(R.drawable.ic_collapse),
                        tint = t.text.copy(alpha = 0.85f),
                        contentDescription = "Close"
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // ---- Criminal Card (mirrors Education's student card) ----
            Surface(
                color = t.surfaceElevated,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(t.primary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_person),
                            contentDescription = null,
                            tint = t.primary
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            rankForNotoriety(notoriety),
                            color = t.text,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "Criminal Record",
                            color = t.text.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    if (isLocked) {
                        val secs = ((((cooldownUntil ?: 0L) - System.currentTimeMillis()) / 1000).toInt()).coerceAtLeast(0)
                        Text("Locked ${secs}s", color = t.text.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ---- Accordion of Crime Categories (single-open; first open) ----
            val catalog = remember { buildCrimeCatalog() }
            var expandedCategory by rememberSaveable { mutableStateOf(catalog.firstOrNull()?.title) }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items = catalog, key = { it.title }) { cat ->
                    val expanded = expandedCategory == cat.title
                    Accordion(
                        title = cat.title,
                        subTitle = cat.subtitle,
                        expanded = expanded,
                        onToggle = { expandedCategory = if (expanded) null else cat.title },
                        containerColor = t.surfaceElevated,
                        textColor = t.text,
                        accent = t.accent
                    ) {
                        cat.subs.forEachIndexed { sIdx, sub ->
                            SubCategoryHeader(sub.title, t.text)
                            sub.items.forEachIndexed { iIdx, item ->
                                val accentColor = accentForCrime(item.type)
                                CrimeListItem(
                                    type = item.type,
                                    enabled = !isLocked,
                                    textColor = t.text,
                                    accent = accentColor,
                                    runState = if (runState?.type == item.type) runState else null,
                                    outcome = revealedOutcome?.takeIf { it.type == item.type },
                                    onContinue = { viewModel.beginCrime(item.type) },
                                    onCancel = viewModel::cancelCrime,
                                    onDismissOutcome = { revealedOutcome = null }
                                )
                                if (iIdx != sub.items.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        color = t.surfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            if (sIdx != cat.subs.lastIndex) Spacer(Modifier.height(10.dp))
                        }
                    }
                }

                // ----- Criminal Record Section -----
                item {
                    Spacer(Modifier.height(6.dp))
                    SectionHeader("Criminal Record", t.text, t.accent)
                }
                items(items = records.take(25), key = { it.id }) { rec ->
                    CrimeRecordRow(rec)
                }

                item { Spacer(Modifier.height(12.dp)) }
            }
        }

        // Police flash overlay on failure / caught
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
    expanded: Boolean,
    onToggle: () -> Unit,
    containerColor: Color,
    textColor: Color,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
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
                    painter = painterResource(id = if (expanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more),
                    contentDescription = null, tint = textColor
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
                title, color = textColor, style = MaterialTheme.typography.titleMedium,
                maxLines = 1, overflow = TextOverflow.Clip
            )
            Text(
                subtitle, color = textColor.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium,
                maxLines = 1, overflow = TextOverflow.Clip
            )
        }
    }
}

/* Row with inline “continue/back out” -> in-progress OR outcome reveal */
@Composable
private fun CrimeListItem(
    type: CrimeViewModel.CrimeType,
    enabled: Boolean,
    textColor: Color,
    accent: Color,
    runState: CrimeViewModel.CrimeRunState?,
    outcome: CrimeViewModel.OutcomeEvent?,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    onDismissOutcome: () -> Unit
) {
    val title = getCrimeName(type)
    val subtitle = getCrimeDescShort(getCrimeDesc(type))

    var expanded by rememberSaveable(type) { mutableStateOf(false) }
    LaunchedEffect(runState?.type) { if (runState?.type == type) expanded = true }
    LaunchedEffect(outcome?.type) { if (outcome?.type == type) expanded = true }

    Column {
        CrimeRow(
            iconRes = getCrimeIconRes(type),
            title = title,
            subtitle = subtitle,
            enabled = enabled || runState != null || outcome != null,
            textColor = textColor,
            accent = accent
        ) {
            if (enabled || runState != null || outcome != null) expanded = !expanded
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
                when {
                    runState != null -> {
                        // ===== In progress: stacked narration + centered ambient + smooth progress =====
                        PhaseNarration(
                            phase = runState.phase,
                            lines = runState.phaseLines,
                            phaseStartMs = runState.phaseStartMs,
                            phaseEndMs = runState.phaseEndMs,
                            textColor = textColor
                        )

                        Spacer(Modifier.height(4.dp))

                        AmbientTicker(
                            ambient = runState.ambientLines,
                            startedAt = runState.startedAtMs,
                            cadenceMs = 4_000L,
                            textColor = textColor
                        )

                        Spacer(Modifier.height(6.dp))

                        SmoothProgressBar(
                            startedAt = runState.startedAtMs,
                            durationMs = runState.durationMs,
                            track = textColor.copy(alpha = 0.15f),
                            bar = accent
                        )

                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            val label = when (runState.phase) {
                                CrimeViewModel.Phase.SETUP -> "Setting up…"
                                CrimeViewModel.Phase.EXECUTION -> "In motion…"
                                CrimeViewModel.Phase.CLIMAX -> "Climax…"
                            }
                            Text(label, color = textColor.copy(alpha = 0.8f), fontSize = 12.sp)
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = onCancel) { Text("Cancel") }
                        }
                    }
                    outcome != null -> {
                        // ===== Dramatic outcome reveal (no notoriety text) =====
                        AnimatedVisibility(
                            visible = true,
                            enter = scaleIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            ElevatedCard(
                                colors = CardDefaults.elevatedCardColors(containerColor = textColor.copy(alpha = 0.06f))
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(
                                        outcome.climaxLine.ifBlank { "It’s done." },
                                        color = textColor,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    val resultLabel = when {
                                        outcome.wasCaught -> "Caught"
                                        outcome.success && outcome.moneyGained > 0 -> "Success"
                                        outcome.success -> "Partial Success"
                                        else -> "Failed"
                                    }
                                    Text(resultLabel, fontWeight = FontWeight.SemiBold, color = textColor)
                                    Spacer(Modifier.height(6.dp))
                                    if (outcome.moneyGained > 0)
                                        Text("Payout: \$${outcome.moneyGained}", color = textColor.copy(alpha = 0.9f))
                                    if (outcome.jailDays > 0)
                                        Text("Jail time: ${outcome.jailDays} day(s)", color = textColor.copy(alpha = 0.9f))
                                    // Notoriety is intentionally hidden from the player
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = onDismissOutcome) { Text("Close") }
                            Spacer(Modifier.weight(1f))
                            Button(onClick = onContinue) { Text("Try again") }
                        }
                    }
                    else -> {
                        // Not started yet — Continue / Back out
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Button(onClick = onContinue, enabled = enabled) { Text("Continue") }
                            Spacer(Modifier.width(12.dp))
                            TextButton(onClick = { expanded = false }, enabled = enabled) { Text("Back out") }
                        }
                    }
                }
            }
        }
    }
}

/* ============================== Stacked narration (no ambient here) ============================== */

@Composable
private fun PhaseNarration(
    phase: CrimeViewModel.Phase,
    lines: List<String>,
    phaseStartMs: Long,
    phaseEndMs: Long,
    textColor: Color
) {
    val safeLines = if (lines.isEmpty()) listOf("") else lines
    val totalPhaseMs = (phaseEndMs - phaseStartMs).coerceAtLeast(1L)

    var nowMs by remember(phaseStartMs, phaseEndMs) { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(phaseStartMs, phaseEndMs) {
        while (true) withFrameNanos { nowMs = System.currentTimeMillis() }
    }

    val elapsedMs = (nowMs - phaseStartMs).coerceIn(0L, totalPhaseMs)
    val rawT = elapsedMs.toFloat() / totalPhaseMs.toFloat()

    val t = when (phase) {
        CrimeViewModel.Phase.SETUP     -> rawT.toDouble().pow(0.75).toFloat()
        CrimeViewModel.Phase.EXECUTION -> rawT.toDouble().pow(1.15).toFloat()
        CrimeViewModel.Phase.CLIMAX    -> rawT.toDouble().pow(0.95).toFloat()
    }.coerceIn(0f, 1f)

    val revealCount = (t * safeLines.size).toInt().coerceIn(1, safeLines.size)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
    ) {
        for (i in 0 until revealCount) {
            AnimatedContent(
                targetState = i,
                transitionSpec = {
                    (slideInHorizontally(animationSpec = tween(240)) { full -> -full / 4 } + fadeIn(tween(220)))
                        .togetherWith(fadeOut(tween(180)))
                        .using(SizeTransform(clip = false))
                },
                label = "phase-line-$i"
            ) { idx ->
                val isLast = idx == revealCount - 1
                Text(
                    text = safeLines[idx],
                    color = if (isLast) textColor else textColor.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = if (isLast) 2.dp else 1.dp)
                )
            }
        }
    }
}

/* ============================== Ambient ticker (fixed height + centered) ============================== */

@Composable
private fun AmbientTicker(
    ambient: List<String>,
    startedAt: Long,
    cadenceMs: Long,
    textColor: Color
) {
    // reserve a stable height ≈ one small line (prevents layout jumps)
    val lineHeightDp = with(LocalDensity.current) { MaterialTheme.typography.bodySmall.lineHeight.toDp() }
    val boxHeight = (lineHeightDp * 1.2f)

    if (ambient.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(boxHeight),
            contentAlignment = Alignment.Center
        ) { /* reserved space */ }
        return
    }

    var nowMs by remember(startedAt, cadenceMs) { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(startedAt, cadenceMs) {
        while (true) withFrameNanos { nowMs = System.currentTimeMillis() }
    }

    val bucket = ((nowMs - startedAt).coerceAtLeast(0L) / cadenceMs).toInt()
    val idx = bucket.mod(ambient.size)
    val current = ambient[idx]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(boxHeight),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = current,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "ambient-crossfade"
        ) { text ->
            Text(
                text = text,
                color = textColor.copy(alpha = 0.70f),
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/* ============================== Smooth Progress ============================== */

@Composable
private fun SmoothProgressBar(
    startedAt: Long,
    durationMs: Long,
    track: Color,
    bar: Color
) {
    var nowMs by remember(startedAt, durationMs) { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(startedAt, durationMs) {
        while (true) withFrameNanos { nowMs = System.currentTimeMillis() }
    }
    val p = ((nowMs - startedAt).coerceAtLeast(0).toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

    LinearProgressIndicator(
        progress = p,
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
        trackColor = track,
        color = bar
    )
}

/* ============================== Criminal Record UI ============================== */

@Composable
private fun CrimeRecordRow(rec: CrimeRecordEntry) {
    val t = LocalLiveonTheme.current
    Surface(
        color = t.surfaceElevated,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(rec.summary, color = t.text, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (rec.money > 0) {
                    Text("+$${rec.money}", color = t.text.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
                }
                if (rec.jailDays > 0) {
                    if (rec.money > 0) Spacer(Modifier.width(12.dp))
                    Text("Jail: ${rec.jailDays}d", color = t.text.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, text: Color, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            color = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(40.dp)
                .background(accent, RoundedCornerShape(2.dp))
        )
    }
}

/* ============================== Catalog / labels / icons / rank helpers ============================== */

private data class CrimeUiItem(val type: CrimeViewModel.CrimeType)
private data class CrimeSubcat(val title: String, val items: List<CrimeUiItem>)
private data class CrimeCat(val title: String, val subtitle: String, val subs: List<CrimeSubcat>)

private fun buildCrimeCatalog(): List<CrimeCat> {
    return listOf(
        CrimeCat(
            title = "Street Crimes",
            subtitle = "Safer, modest gains",
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
}

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

/** Accent color per crime type */
private fun accentForCrime(type: CrimeViewModel.CrimeType): Color = when (type) {
    // Lower risk-ish
    CrimeViewModel.CrimeType.PICKPOCKETING,
    CrimeViewModel.CrimeType.SHOPLIFTING,
    CrimeViewModel.CrimeType.VANDALISM,
    CrimeViewModel.CrimeType.PETTY_SCAM -> Color(0xFF2ECC71)
    // Medium
    CrimeViewModel.CrimeType.MUGGING,
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING,
    CrimeViewModel.CrimeType.DRUG_DEALING,
    CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> Color(0xFFFFC107)
    // High
    CrimeViewModel.CrimeType.BURGLARY,
    CrimeViewModel.CrimeType.FRAUD,
    CrimeViewModel.CrimeType.ARMS_SMUGGLING,
    CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> Color(0xFFFF7043)
    // Extreme / Mastermind
    else -> Color(0xFFE53935)
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

/* ============================== Police light brush ============================== */

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
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
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
