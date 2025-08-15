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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.liveongames.liveon.viewmodel.CrimeViewModel.CrimeType
import kotlinx.coroutines.delay
import kotlin.math.pow
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.key
import androidx.compose.ui.text.TextStyle

// Labels & icons reused from Education-style util file
import com.liveongames.liveon.util.getCrimeName
import com.liveongames.liveon.util.getCrimeDesc
import com.liveongames.liveon.util.getCrimeDescShort
import com.liveongames.liveon.util.getCrimeIconRes
import com.liveongames.liveon.util.rankForNotoriety
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

/* =========================================================================================
 * Crime screen — aligned with Education:
 * 1) Criminal card at top (avatar + rank), notoriety hidden.
 * 2) Accordion of categories; tap row to start a crime (no Continue/Backout).
 * 3) In-row progression: phase lines + centered ambient (fixed height) + progress + Cancel.
 * 4) Dramatic outcome reveal after the run (Success/Fail/Caught), no notoriety here.
 * 5) Criminal Record at bottom: name, outcome, category, notoriety delta (here only), newest first.
 * Only one crime can be active at a time; other items visually disabled.
 * ========================================================================================= */

@Composable
fun CrimeScreen(
    onDismiss: () -> Unit,
    viewModel: CrimeViewModel = hiltViewModel(),
    onCrimeCommitted: () -> Unit = {}
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
            if (!out.success || out.wasCaught) {
                policeFlash = true
                delay(1400)
                policeFlash = false
            }
            revealedOutcome = out
            onCrimeCommitted()
            viewModel.consumeOutcome()
        }
    }

    val isGlobalLock = (cooldownUntil ?: 0L) > System.currentTimeMillis()
    val activeType = runState?.type ?: revealedOutcome?.type

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

            // 1) Criminal Card (matches Education's student card)
            Surface(
                color = t.surfaceElevated,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
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
                            "Criminal Profile",
                            color = t.text.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    if (isGlobalLock) {
                        val secs = ((((cooldownUntil ?: 0L) - System.currentTimeMillis()) / 1000).toInt()).coerceAtLeast(0)
                        Text("Locked ${secs}s", color = t.text.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // 2) Accordion of Crime Categories (single-open; first open)
            val catalog = remember { buildCrimeCatalog() }
            var expandedCategory by rememberSaveable { mutableStateOf(catalog.firstOrNull()?.title) }
            val sortedRecords = remember(records) { records.sortedByDescending { it.timestamp } }

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
                                val thisIsActive = activeType == item.type
                                val canStartThis = !isGlobalLock && activeType == null
                                val rowEnabled = canStartThis || thisIsActive

                                CrimeListItem(
                                    type = item.type,
                                    enabled = rowEnabled,
                                    textColor = t.text,
                                    accent = accentColor,
                                    runState = if (runState?.type == item.type) runState else null,
                                    outcome = revealedOutcome?.takeIf { it.type == item.type },
                                    onStart = { if (canStartThis) viewModel.beginCrime(item.type) },
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

                // 5) Criminal Record (Bottom)
                item {
                    Spacer(Modifier.height(6.dp))
                    SectionHeader("Criminal Record", t.text, t.accent)
                }

                items(items = sortedRecords, key = { it.id }) { rec ->
                    CrimeRecordRow(rec)
                }

                item { Spacer(Modifier.height(12.dp)) }
            }
        }

        // Light police flash overlay on failure / caught
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

/* ============================== Accordion ============================== */

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

/* ============================== Row + In-row flow ============================== */

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

/**
 * One line item that:
 *  - Starts immediately on row tap when idle (no continue/backout).
 *  - While running: shows stacked phase text, centered ambient (fixed height),
 *    smooth progress bar, and Cancel button.
 *  - After finish: shows dramatic outcome card (no notoriety), with Try again / Close.
 */
@Composable
private fun CrimeListItem(
    type: CrimeType,
    enabled: Boolean,
    textColor: Color,
    accent: Color,
    runState: CrimeViewModel.CrimeRunState?,
    outcome: CrimeViewModel.OutcomeEvent?,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onDismissOutcome: () -> Unit
) {
    val title = getCrimeName(type)
    val subtitle = getCrimeDescShort(getCrimeDesc(type))

    Column {
        CrimeRow(
            iconRes = getCrimeIconRes(type),
            title = title,
            subtitle = subtitle,
            enabled = enabled,
            textColor = textColor,
            accent = accent
        ) {
            // Tap row to initiate when idle; if running or outcome present, do nothing here.
            if (runState == null && outcome == null) onStart()
        }

        // Expanded area appears ONLY during run or outcome reveal.
        AnimatedVisibility(
            visible = runState != null || outcome != null,
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
                        // 3) In progress: stacked narration + centered ambient + progress + cancel
                        PhaseNarration(
                            phase = runState.phase,
                            lines = runState.phaseLines,
                            phaseStartMs = runState.phaseStartMs,
                            phaseEndMs = runState.phaseEndMs,
                            textColor = textColor,
                            windowSize = 4
                        )

                        Spacer(Modifier.height(4.dp))

                        AmbientTicker(
                            ambient = runState.ambientLines,
                            startedAt = runState.startedAtMs,
                            totalDurationMs = runState.durationMs,
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
                        // 4) Dramatic outcome reveal (no notoriety)
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
                                    // Notoriety intentionally hidden here
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = onDismissOutcome) { Text("Close") }
                            Spacer(Modifier.weight(1f))
                            Button(onClick = onStart) { Text("Try again") }
                        }
                    }
                }
            }
        }
    }
}

/* ============================== Phase narration ============================== */

@Composable
private fun PhaseNarration(
    phase: CrimeViewModel.Phase,
    lines: List<String>,
    phaseStartMs: Long,
    phaseEndMs: Long,
    textColor: Color,
    windowSize: Int = 4 // visible rolling window (3–5 recommended)
) {
    val safe = if (lines.isEmpty()) listOf("") else lines
    val totalMs = (phaseEndMs - phaseStartMs).coerceAtLeast(1L)

    // Pace each line evenly within the phase; clamp for readability
    val perLineMs = (totalMs / safe.size.coerceAtLeast(1)).coerceIn(900L, 2500L)

    var now by remember(phaseStartMs, phaseEndMs) { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(phaseStartMs, phaseEndMs) {
        while (true) withFrameNanos { now = System.currentTimeMillis() }
    }

    val elapsed = (now - phaseStartMs).coerceIn(0L, totalMs)
    val currentIdx = (elapsed / perLineMs).toInt().coerceAtMost(safe.lastIndex)
    val revealedCountExpected = currentIdx + 1

    // Monotonic reveal count: never decreases (prevents any flip/jitter)
    var revealedCount by remember(phase, phaseStartMs) { mutableStateOf(0) }
    revealedCount = revealedCount.coerceAtLeast(revealedCountExpected)

    // Reserve a stable, readable area: windowSize × line height (prevents layout jumps)
    val lineHeightDp = with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.lineHeight.toDp() }
    val reservedHeight = lineHeightDp * windowSize + 8.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(reservedHeight) // fixed space; content animates inside
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(Modifier.fillMaxWidth()) {
            // Iterate up to revealedCount so items leaving the window get exit animations.
            for (i in 0 until revealedCount) {
                val inWindow = i >= (revealedCount - windowSize).coerceAtLeast(0) && i <= (revealedCount - 1)
                val isNewest = i == currentIdx

                key(i) {
                    AnimatedVisibility(
                        visible = inWindow,
                        // Enter: rise + fade; newest slightly stronger
                        enter =
                            slideInVertically(animationSpec = tween(if (isNewest) 280 else 200)) { fullHeight: Int -> fullHeight / 3 } +
                                    fadeIn(animationSpec = tween(if (isNewest) 280 else 200)),
                        // Exit: drift upward + fade (no snap)
                        exit =
                            slideOutVertically(animationSpec = tween(220)) { fullHeight: Int -> -fullHeight / 3 } +
                                    fadeOut(animationSpec = tween(220)),
                        label = "phase-line-$i"
                    ) {
                        if (isNewest) {
                            // Ghost-write ONLY the active line; older lines are solid text
                            val lineStart = phaseStartMs + i * perLineMs
                            val perCharMs = (perLineMs / safe[i].length.coerceAtLeast(8)).coerceIn(14L, 120L)
                            TypewriterText(
                                fullText = safe[i],
                                lineStartMs = lineStart,
                                perCharMs = perCharMs,
                                nowProvider = { now }, // shares the same clock
                                color = textColor,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            )
                        } else {
                            Text(
                                text = safe[i],
                                color = textColor.copy(alpha = 0.88f),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypewriterText(
    fullText: String,
    lineStartMs: Long,
    perCharMs: Long,
    nowProvider: () -> Long,
    color: Color,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    // Monotonic char counter — never goes backward and never exceeds text length
    var charCount by remember(lineStartMs, fullText) { mutableStateOf(0) }
    val now = nowProvider()
    val elapsed = (now - lineStartMs).coerceAtLeast(0L)
    val expected = (elapsed / perCharMs).toInt() + 1
    charCount = charCount.coerceAtLeast(expected.coerceAtMost(fullText.length))

    Text(
        text = fullText.take(charCount),
        color = color,
        style = style,
        textAlign = TextAlign.Start,
        modifier = modifier
    )
}

/* ============================== Ambient ticker (fixed height + centered) ============================== */

@Composable
private fun AmbientTicker(
    ambient: List<String>,
    startedAt: Long,
    totalDurationMs: Long,
    textColor: Color
) {
    val lineHeightDp = with(LocalDensity.current) { MaterialTheme.typography.bodySmall.lineHeight.toDp() }
    val boxHeight = (lineHeightDp * 1.2f) // fixed single-line reserve

    if (ambient.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(boxHeight))
        return
    }

    // Evenly pace ambient across the entire run; no loop; stop on last line
    val perMsgMs = (totalDurationMs / ambient.size.coerceAtLeast(1)).coerceIn(1500L, 6000L)

    var now by remember(startedAt, totalDurationMs) { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(startedAt, totalDurationMs) {
        while (true) withFrameNanos { now = System.currentTimeMillis() }
    }

    val elapsed = (now - startedAt).coerceAtLeast(0L)
    val expectedIdx = (elapsed / perMsgMs).toInt().coerceAtMost(ambient.lastIndex)

    // Monotonic index; never reverse; finish at last line
    var shownIdx by remember(startedAt) { mutableStateOf(0) }
    shownIdx = shownIdx.coerceAtLeast(expectedIdx)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(boxHeight),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = shownIdx,
            transitionSpec = {
                (slideInVertically(animationSpec = tween(220)) { fullHeight: Int -> fullHeight / 4 } + fadeIn(tween(220))) togetherWith
                        (slideOutVertically(animationSpec = tween(220)) { fullHeight: Int -> -fullHeight / 4 } + fadeOut(tween(200)))
            },
            label = "ambient-crossfade"
        ) { idx ->
            Text(
                text = ambient[idx],
                color = textColor.copy(alpha = 0.72f),
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

    // Derive type & labels
    val type = parseCrimeType(rec.typeKey)
    val name = type?.let { getCrimeName(it) } ?: rec.typeKey
    val category = type?.let { categoryLabelFor(it) } ?: "—"
    val outcome = when {
        rec.caught -> "Caught"
        rec.success -> "Success"
        else -> "Failed"
    }
    val notorietyDelta = type?.let { estimateNotorietyDelta(it, rec.success, rec.caught) }
    val notoText = notorietyDelta?.let { (sign, abs) ->
        if (abs == 0) null else "Notoriety ${if (sign > 0) "+" else "−"}$abs"
    }

    Surface(
        color = t.surfaceElevated,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("$name • $outcome", color = t.text, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(2.dp))
            Text("Category: $category", color = t.text.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)

            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (rec.money > 0) {
                    Text("+$${rec.money}", color = t.text.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
                }
                if (rec.jailDays > 0) {
                    if (rec.money > 0) Spacer(Modifier.width(12.dp))
                    Text("Jail: ${rec.jailDays}d", color = t.text.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
                }
                if (notoText != null) {
                    if (rec.money > 0 || rec.jailDays > 0) Spacer(Modifier.width(12.dp))
                    Text(notoText, color = t.text.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
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

/* ============================== Catalog model & helpers ============================== */

private data class CrimeUiItem(val type: CrimeType)
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
                        CrimeUiItem(CrimeType.PICKPOCKETING),
                        CrimeUiItem(CrimeType.SHOPLIFTING),
                        CrimeUiItem(CrimeType.VANDALISM),
                        CrimeUiItem(CrimeType.PETTY_SCAM)
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
                        CrimeUiItem(CrimeType.MUGGING),
                        CrimeUiItem(CrimeType.BREAKING_AND_ENTERING),
                        CrimeUiItem(CrimeType.DRUG_DEALING),
                        CrimeUiItem(CrimeType.COUNTERFEIT_GOODS)
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
                        CrimeUiItem(CrimeType.BURGLARY),
                        CrimeUiItem(CrimeType.FRAUD),
                        CrimeUiItem(CrimeType.ARMS_SMUGGLING),
                        CrimeUiItem(CrimeType.DRUG_TRAFFICKING)
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
                        CrimeUiItem(CrimeType.ARMED_ROBBERY),
                        CrimeUiItem(CrimeType.EXTORTION),
                        CrimeUiItem(CrimeType.KIDNAPPING_FOR_RANSOM),
                        CrimeUiItem(CrimeType.PONZI_SCHEME),
                        CrimeUiItem(CrimeType.CONTRACT_KILLING),
                        CrimeUiItem(CrimeType.DARK_WEB_SALES),
                        CrimeUiItem(CrimeType.ART_THEFT),
                        CrimeUiItem(CrimeType.DIAMOND_HEIST)
                    )
                )
            )
        )
    )
}

/** Accent color per crime type */
private fun accentForCrime(type: CrimeType): Color = when (type) {
    // Lower risk-ish
    CrimeType.PICKPOCKETING,
    CrimeType.SHOPLIFTING,
    CrimeType.VANDALISM,
    CrimeType.PETTY_SCAM -> Color(0xFF2ECC71)
    // Medium
    CrimeType.MUGGING,
    CrimeType.BREAKING_AND_ENTERING,
    CrimeType.DRUG_DEALING,
    CrimeType.COUNTERFEIT_GOODS -> Color(0xFFFFC107)
    // High
    CrimeType.BURGLARY,
    CrimeType.FRAUD,
    CrimeType.ARMS_SMUGGLING,
    CrimeType.DRUG_TRAFFICKING -> Color(0xFFFF7043)
    // Extreme / Mastermind
    else -> Color(0xFFE53935)
}

/* --------- Record derivations (category + notoriety estimate) ---------- */

private fun parseCrimeType(typeKey: String): CrimeType? =
    runCatching { CrimeType.valueOf(typeKey) }.getOrNull()

private fun categoryLabelFor(type: CrimeType): String = when (type) {
    CrimeType.PICKPOCKETING, CrimeType.SHOPLIFTING, CrimeType.VANDALISM, CrimeType.PETTY_SCAM -> "Street Crimes"
    CrimeType.MUGGING, CrimeType.BREAKING_AND_ENTERING, CrimeType.DRUG_DEALING, CrimeType.COUNTERFEIT_GOODS -> "Robbery"
    CrimeType.BURGLARY, CrimeType.FRAUD, CrimeType.ARMS_SMUGGLING, CrimeType.DRUG_TRAFFICKING -> "Heists & Smuggling"
    else -> "Mastermind Tier"
}

/**
 * UI-only notoriety estimate (used for record display only).
 * Mirrors VM baselines so players can see the *intended* notoriety change here.
 */
private fun estimateNotorietyDelta(
    type: CrimeType,
    success: Boolean,
    caught: Boolean
): Pair<Int /*sign*/, Int /*abs*/>? {
    val (gain, loss) = when (type) {
        // LOW
        CrimeType.PICKPOCKETING, CrimeType.SHOPLIFTING, CrimeType.VANDALISM, CrimeType.PETTY_SCAM -> 1 to 1
        // MED
        CrimeType.MUGGING, CrimeType.BREAKING_AND_ENTERING, CrimeType.DRUG_DEALING, CrimeType.COUNTERFEIT_GOODS -> 2 to 2
        // HIGH
        CrimeType.BURGLARY, CrimeType.FRAUD, CrimeType.ARMS_SMUGGLING -> 4 to 2
        CrimeType.DRUG_TRAFFICKING -> 5 to 2
        // EXTREME
        CrimeType.ARMED_ROBBERY, CrimeType.EXTORTION, CrimeType.KIDNAPPING_FOR_RANSOM,
        CrimeType.PONZI_SCHEME, CrimeType.CONTRACT_KILLING, CrimeType.ART_THEFT, CrimeType.DIAMOND_HEIST -> 10 to 3
        CrimeType.DARK_WEB_SALES -> 8 to 3
    }
    val abs = if (success && !caught) gain else loss
    val sign = if (success && !caught) +1 else -1
    return sign to abs
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
