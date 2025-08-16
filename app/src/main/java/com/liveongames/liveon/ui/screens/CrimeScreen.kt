// app/src/main/java/com/liveongames/liveon/ui/screens/CrimeScreen.kt
package com.liveongames.liveon.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liveongames.domain.model.CrimeRecordEntry
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LocalLiveonTheme
import com.liveongames.liveon.util.getCrimeDesc
import com.liveongames.liveon.util.getCrimeDescShort
import com.liveongames.liveon.util.getCrimeIconRes
import com.liveongames.liveon.util.getCrimeName
import com.liveongames.liveon.util.rankForNotoriety
import com.liveongames.liveon.viewmodel.CrimeViewModel
import com.liveongames.liveon.viewmodel.CrimeViewModel.CrimeType
import kotlin.math.min

/* kept for future per-phase weighting if you want it later */
private data class RevealPlan(val setupEnd: Float, val execEnd: Float, val climaxEnd: Float)
private val RevealPlanSaver: Saver<RevealPlan, FloatArray> = Saver(
    save = { floatArrayOf(it.setupEnd, it.execEnd, it.climaxEnd) },
    restore = { a -> RevealPlan(a[0], a[1], a[2]) }
)

/* ================================= Screen ================================= */
@Composable
fun CrimeScreen(
    onDismiss: () -> Unit,
    viewModel: CrimeViewModel = hiltViewModel(),
    onCrimeCommitted: () -> Unit = {}
) {
    val t = LocalLiveonTheme.current

    val notoriety by viewModel.playerNotoriety.collectAsStateWithLifecycle()
    val cooldownUntil by viewModel.cooldownUntil.collectAsStateWithLifecycle()
    val runState by viewModel.runState.collectAsStateWithLifecycle()
    val lastOutcomeVm by viewModel.lastOutcome.collectAsStateWithLifecycle()
    val records by viewModel.criminalRecords.collectAsStateWithLifecycle()

    var revealedOutcome by remember { mutableStateOf<CrimeViewModel.OutcomeEvent?>(null) }
    var flashOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(lastOutcomeVm) {
        lastOutcomeVm?.let {
            flashOverlay = true
            kotlinx.coroutines.delay(60)
            flashOverlay = false
            revealedOutcome = it
            onCrimeCommitted()
            viewModel.consumeOutcome()
        }
    }

    val isGlobalLock = (cooldownUntil ?: 0L) > System.currentTimeMillis()
    val activeType = runState?.type ?: revealedOutcome?.type

    val catalog = remember { buildCrimeCatalog() }
    var expandedCategory by rememberSaveable { mutableStateOf(catalog.firstOrNull()?.title) }

    val sortedRecords = remember(records) { records.sortedByDescending { it.timestamp } }

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
                .graphicsLayer { shape = RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp); clip = true }
                .background(t.surface)
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { /* consume */ },
            verticalArrangement = Arrangement.Top
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Crime", color = t.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(painter = painterResource(R.drawable.ic_collapse), contentDescription = "Close", tint = t.text.copy(alpha = 0.85f)) }
            }
            Spacer(Modifier.height(6.dp))

            // 1) Criminal Card (no notoriety here)
            Surface(color = t.surfaceElevated, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).background(t.primary.copy(alpha = 0.18f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(painter = painterResource(R.drawable.ic_person), contentDescription = null, tint = t.primary)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(rankForNotoriety(notoriety), color = t.text, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Criminal Profile", color = t.text.copy(alpha = 0.6f), style = MaterialTheme.typography.labelMedium)
                    }
                    if (isGlobalLock) {
                        val secs = ((((cooldownUntil ?: 0L) - System.currentTimeMillis()) / 1000).toInt()).coerceAtLeast(0)
                        Text("Locked ${secs}s", color = t.text.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // 2) Accordion + 3) In-row progression
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
                        textColor = t.text
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
                                    HorizontalDivider(Modifier.padding(horizontal = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                                }
                            }
                            if (sIdx != cat.subs.lastIndex) Spacer(Modifier.height(10.dp))
                        }
                    }
                }

                // 5) Criminal Record
                item { Spacer(Modifier.height(6.dp)); SectionHeader("Criminal Record", t.text) }
                items(items = sortedRecords, key = { it.id }) { rec -> CrimeRecordRow(rec) }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }

        // 4) Dramatic outcome flash
        val flashColor = revealedOutcome?.let { outcomeColor(it) } ?: Color.White
        AnimatedVisibility(visible = flashOverlay, enter = fadeIn(tween(60)), exit = fadeOut(tween(60))) {
            Box(Modifier.fillMaxSize().background(flashColor.copy(alpha = 0.25f)))
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
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(2.dp),
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
        Text(label, color = textColor.copy(alpha = 0.8f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
    }
}

/* ============================== Rows & Flow ============================== */
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
    val alpha by animateFloatAsState(if (enabled) 1f else 0.55f, label = "row-alpha")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp)
            .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(iconRes), contentDescription = null, tint = accent, modifier = Modifier.size(25.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = textColor, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Clip)
            Text(subtitle, color = textColor.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Clip)
        }
    }
}

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
        ) { if (runState == null && outcome == null) onStart() }

        AnimatedVisibility(
            visible = runState != null || outcome != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                when {
                    runState != null -> {
                        val runKey = runState.startedAtMs

                        // Main narrative: mask-reveal + smooth right-edge scroll tied to overall progress
                        NarrativeMarquee(
                            runKey = runKey,
                            progress = runState.progress,
                            script = runState.scriptAll,
                            isClimax = runState.phase == CrimeViewModel.Phase.CLIMAX,
                            textColor = textColor
                        )

                        Spacer(Modifier.height(6.dp))

                        // Ambient (italic, forward-only, reserved height)
                        AmbientTicker(
                            runKey = runKey,
                            ambient = runState.ambientLines,
                            totalDurationMs = runState.durationMs,
                            textColor = textColor
                        )

                        Spacer(Modifier.height(6.dp))

                        // Progress bar only (no phase labels)
                        SmoothProgressBar(
                            startedAt = runState.startedAtMs,
                            durationMs = runState.durationMs,
                            track = textColor.copy(alpha = 0.15f),
                            bar = accent
                        )

                        Spacer(Modifier.height(10.dp))

                        // Cancel: fades in once execution begins (no phase text)
                        val showCancel = runState.phase != CrimeViewModel.Phase.SETUP
                        AnimatedVisibility(visible = showCancel, enter = fadeIn(tween(180)), exit = fadeOut(tween(140))) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Spacer(Modifier.weight(1f))
                                HoldToCancelButton(label = "Cancel", onConfirmed = onCancel, textColor = textColor)
                            }
                        }
                    }
                    outcome != null -> {
                        OutcomeReveal(
                            outcome = outcome,
                            onClose = onDismissOutcome,
                            onRetry = onStart,
                            textColor = textColor
                        )
                    }
                }
            }
        }
    }
}

/* ============== Narrative: mask reveal + smooth scroll (no typewriter) ============== */
@Composable
private fun NarrativeMarquee(
    runKey: Long,
    progress: Float,          // total crime progress 0..1
    script: String,           // frozen paragraph (setup+execution)
    isClimax: Boolean,
    textColor: Color
) {
    val style = MaterialTheme.typography.bodyMedium
    val measurer = rememberTextMeasurer()
    val lineHeight = with(LocalDensity.current) { style.lineHeight.toDp() }
    val reservedHeight = lineHeight * 1.2f

    // Measure FULL paragraph once per change
    val layout = remember(runKey, script) {
        measurer.measure(
            AnnotatedString(script),
            style = style,
            constraints = Constraints(maxWidth = Int.MAX_VALUE)
        )
    }
    val textWidthPx = layout.size.width.toFloat()

    // Reveal width grows linearly with progress; never less than a hair once progress > 0
    val revealPxRaw = textWidthPx * progress.coerceIn(0f, 1f)
    val revealPx = if (progress > 0f) maxOf(revealPxRaw, 2f) else 0f

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(reservedHeight)
    ) {
        val viewport = with(LocalDensity.current) { maxWidth.toPx() }

        // Scroll keeps RIGHT edge of the reveal in view
        val targetScroll = (revealPx - viewport).coerceAtLeast(0f)
        val scroll by animateFloatAsState(
            targetValue = targetScroll,
            animationSpec = tween(durationMillis = 100, easing = LinearEasing),
            label = "narrative-scroll"
        )

        // BACK layer (dim, full paragraph — readability)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(-scroll, 0f),
                        color = textColor.copy(alpha = 0.25f)
                    )
                }
        )

        // FRONT layer (mask-revealed up to 'revealPx')
        val leftFade = with(LocalDensity.current) { 16.dp.toPx() }
        val rightFade = with(LocalDensity.current) { 24.dp.toPx() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    // Clip to the reveal boundary in viewport space
                    val clipRight = min(viewport, (revealPx - scroll).coerceAtLeast(0f))
                    if (clipRight > 0f) {
                        clipRect(left = 0f, top = 0f, right = clipRight, bottom = size.height) {
                            drawText(
                                textLayoutResult = layout,
                                topLeft = Offset(-scroll, 0f),
                                color = textColor
                            )
                        }
                    }

                    // Edge fades (older text vanishes left, new text ghosts right)
                    // Left fade
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startX = 0f, endX = leftFade
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size(leftFade, size.height),
                        blendMode = BlendMode.DstIn
                    )
                    // Right fade
                    val startX = (size.width - rightFade).coerceAtLeast(0f)
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Black, Color.Transparent),
                            startX = startX, endX = size.width
                        ),
                        topLeft = Offset(startX, 0f),
                        size = Size(size.width - startX, size.height),
                        blendMode = BlendMode.DstIn
                    )
                }
        )

        // Climax tension indicator at far right (tightening ellipsis)
        if (isClimax) {
            val ls = (lerp(6f, 0f, progress.coerceIn(0f, 1f))).sp
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                Text("…", color = textColor.copy(alpha = 0.9f), style = style.copy(letterSpacing = ls), maxLines = 1)
            }
        }
    }
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

/* ======================= Ambient (italic, forward-only) ======================= */
@Composable
private fun AmbientTicker(
    runKey: Long,
    ambient: List<String>,
    totalDurationMs: Long,
    textColor: Color
) {
    val baseStyle = MaterialTheme.typography.bodySmall
    val style = baseStyle.copy(fontStyle = FontStyle.Italic)
    val lineHeightDp = with(LocalDensity.current) { style.lineHeight.toDp() }
    val boxHeight = (lineHeightDp * 1.2f)

    val clean = remember(ambient) { ambient.filter { it.isNotBlank() }.distinct() }
    if (clean.isEmpty()) { Box(Modifier.fillMaxWidth().height(boxHeight)); return }

    // Show each ambient line once; cadence clamped to ~2–3s per line
    val stepMs = if (clean.size > 1) {
        (totalDurationMs.toFloat() / clean.size.toFloat()).coerceIn(2000f, 3000f).toLong()
    } else totalDurationMs

    var idx by remember(runKey) { mutableStateOf(0) }
    LaunchedEffect(runKey) {
        for (i in 1 until clean.size) {
            kotlinx.coroutines.delay(stepMs)
            idx = i
        }
    }

    Box(Modifier.fillMaxWidth().height(boxHeight), contentAlignment = Alignment.Center) {
        AnimatedContent(
            targetState = idx,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
            label = "ambient-once"
        ) { i ->
            Text(
                text = clean[i],
                color = textColor.copy(alpha = 0.72f),
                style = style,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/* ============================== Progress & Outcome ============================== */
@Composable
private fun SmoothProgressBar(startedAt: Long, durationMs: Long, track: Color, bar: Color) {
    var now by remember(startedAt, durationMs) { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(startedAt, durationMs) { while (true) withFrameNanos { now = System.currentTimeMillis() } }
    val p = ((now - startedAt).coerceAtLeast(0).toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    LinearProgressIndicator(progress = p, modifier = Modifier.fillMaxWidth().height(8.dp), trackColor = track, color = bar)
}

@Composable
private fun OutcomeReveal(
    outcome: com.liveongames.liveon.viewmodel.CrimeViewModel.OutcomeEvent,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    textColor: Color
) {
    val wash = outcomeColor(outcome).copy(alpha = 0.10f)
    var start by remember(outcome) { mutableStateOf(true) }
    val transY by animateFloatAsState(
        targetValue = if (start) with(LocalDensity.current) { 8.dp.toPx() } else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "outcome-spring"
    )
    LaunchedEffect(Unit) { start = false }

    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = textColor.copy(alpha = 0.06f))) {
        Box(Modifier.background(wash).graphicsLayer { translationY = transY }) {
            Column(
                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // outcome label (centered)
                val resultLabel = when {
                    outcome.wasCaught -> "Caught"
                    outcome.success && outcome.moneyGained > 0 -> "Success"
                    outcome.success -> "Partial Success"
                    else -> "Failed"
                }
                Text(resultLabel, fontWeight = FontWeight.SemiBold, color = outcomeColor(outcome), textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Text(outcome.climaxLine.ifBlank { "It’s done." }, color = textColor, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                if (outcome.moneyGained > 0) Text("Payout: \$${outcome.moneyGained}", color = textColor.copy(alpha = 0.9f), textAlign = TextAlign.Center)
                if (outcome.jailDays > 0) Text("Jail time: ${outcome.jailDays} day(s)", color = textColor.copy(alpha = 0.9f), textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = onClose) { Text("Close") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onRetry) { Text("Try again") }
                }
            }
        }
    }
}

private fun outcomeColor(outcome: com.liveongames.liveon.viewmodel.CrimeViewModel.OutcomeEvent): Color =
    when {
        outcome.wasCaught -> Color(0xFFE53935)
        outcome.success && outcome.moneyGained > 0 -> Color(0xFF2ECC71)
        outcome.success -> Color(0xFFFFC107)
        else -> Color(0xFFFFC107)
    }

/* ============================== Hold-to-cancel ============================== */
@Composable
private fun HoldToCancelButton(label: String, onConfirmed: () -> Unit, textColor: Color, holdMillis: Long = 800L) {
    var show by remember { mutableStateOf(false) }
    TextButton(onClick = { show = true }) { Text(label) }
    if (show) {
        HoldToConfirmDialog(onDismiss = { show = false }, onConfirmed = { show = false; onConfirmed() }, textColor = textColor, holdMillis = holdMillis)
    }
}

@Composable
private fun HoldToConfirmDialog(onDismiss: () -> Unit, onConfirmed: () -> Unit, textColor: Color, holdMillis: Long) {
    var progress by remember { mutableStateOf(0f) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Back") } },
        title = { Text("Hold to cancel", color = textColor) },
        text = {
            Column {
                Text("Press and hold to abort this crime.", color = textColor.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                HoldBar(progress = progress, onProgress = { progress = it }, onComplete = { progress = 0f; onConfirmed() }, textColor = textColor, holdMillis = holdMillis)
            }
        }
    )
}

@Composable
private fun HoldBar(progress: Float, onProgress: (Float) -> Unit, onComplete: () -> Unit, textColor: Color, holdMillis: Long) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var start by remember { mutableStateOf(0L) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(textColor.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    start = System.currentTimeMillis()
                    var done = false
                    try {
                        do {
                            withFrameNanos { now = System.currentTimeMillis() }
                            val p = ((now - start).toFloat() / holdMillis.toFloat()).coerceIn(0f, 1f)
                            onProgress(p)
                            if (p >= 1f && !done) { done = true; onComplete() }
                        } while (tryAwaitRelease() && !done)
                    } finally { if (!done) onProgress(0f) }
                })
            },
        contentAlignment = Alignment.CenterStart
    ) {
        androidx.compose.foundation.Canvas(Modifier.matchParentSize()) {
            drawRect(color = textColor.copy(alpha = 0.18f))
            val w = size.width * progress.coerceIn(0f, 1f)
            drawRect(color = textColor.copy(alpha = 0.45f), topLeft = Offset.Zero, size = Size(w, size.height))
        }
        Text(if (progress >= 1f) "Release" else "Hold…", color = textColor, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

/* ============================== Records & Header ============================== */
@Composable
private fun CrimeRecordRow(rec: CrimeRecordEntry) {
    val t = LocalLiveonTheme.current
    val type = parseCrimeType(rec.typeKey)
    val name = type?.let { getCrimeName(it) } ?: rec.typeKey
    val category = type?.let { categoryLabelFor(it) } ?: "—"
    val outcome = when { rec.caught -> "Caught"; rec.success -> "Success"; else -> "Failed" }

    Surface(color = t.surfaceElevated, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("$name • $outcome", color = t.text, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(2.dp))
            Text("Category: $category", color = t.text.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (rec.money > 0) Text("+$${rec.money}", color = t.text.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
                if (rec.jailDays > 0) { if (rec.money > 0) Spacer(Modifier.width(12.dp)); Text("Jail: ${rec.jailDays}d", color = t.text.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, text: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Box(Modifier.height(2.dp).width(40.dp).background(text.copy(alpha = 0.25f), RoundedCornerShape(2.dp)))
    }
}

/* ============================== Catalog & Helpers ============================== */
private data class CrimeUiItem(val type: CrimeType)
private data class CrimeSubcat(val title: String, val items: List<CrimeUiItem>)
private data class CrimeCat(val title: String, val subtitle: String, val subs: List<CrimeSubcat>)

private fun buildCrimeCatalog(): List<CrimeCat> = listOf(
    CrimeCat("Street Crimes", "Safer, modest gains", subs = listOf(
        CrimeSubcat("", listOf(
            CrimeUiItem(CrimeViewModel.CrimeType.PICKPOCKETING),
            CrimeUiItem(CrimeViewModel.CrimeType.SHOPLIFTING),
            CrimeUiItem(CrimeViewModel.CrimeType.VANDALISM),
            CrimeUiItem(CrimeViewModel.CrimeType.PETTY_SCAM)
        ))
    )),
    CrimeCat("Robbery", "Bigger rewards, higher danger", subs = listOf(
        CrimeSubcat("", listOf(
            CrimeUiItem(CrimeViewModel.CrimeType.MUGGING),
            CrimeUiItem(CrimeViewModel.CrimeType.BREAKING_AND_ENTERING),
            CrimeUiItem(CrimeViewModel.CrimeType.DRUG_DEALING),
            CrimeUiItem(CrimeViewModel.CrimeType.COUNTERFEIT_GOODS)
        ))
    )),
    CrimeCat("Heists & Smuggling", "High stakes, serious time", subs = listOf(
        CrimeSubcat("", listOf(
            CrimeUiItem(CrimeViewModel.CrimeType.BURGLARY),
            CrimeUiItem(CrimeViewModel.CrimeType.FRAUD),
            CrimeUiItem(CrimeViewModel.CrimeType.ARMS_SMUGGLING),
            CrimeUiItem(CrimeViewModel.CrimeType.DRUG_TRAFFICKING)
        ))
    )),
    CrimeCat("Mastermind Tier", "Elite jobs, massive risk", subs = listOf(
        CrimeSubcat("", listOf(
            // existing mastermind
            CrimeUiItem(CrimeViewModel.CrimeType.ARMED_ROBBERY),
            CrimeUiItem(CrimeViewModel.CrimeType.EXTORTION),
            CrimeUiItem(CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM),
            CrimeUiItem(CrimeViewModel.CrimeType.PONZI_SCHEME),
            CrimeUiItem(CrimeViewModel.CrimeType.CONTRACT_KILLING),
            CrimeUiItem(CrimeViewModel.CrimeType.DARK_WEB_SALES),
            CrimeUiItem(CrimeViewModel.CrimeType.ART_THEFT),
            CrimeUiItem(CrimeViewModel.CrimeType.DIAMOND_HEIST),
            CrimeUiItem(CrimeViewModel.CrimeType.BANK_HEIST),
            CrimeUiItem(CrimeViewModel.CrimeType.POLITICAL_ASSASSINATION),
            CrimeUiItem(CrimeViewModel.CrimeType.CRIME_SYNDICATE)
        ))
    ))
)

private fun accentForCrime(type: CrimeType): Color = when (type) {
    CrimeType.PICKPOCKETING, CrimeType.SHOPLIFTING, CrimeType.VANDALISM, CrimeType.PETTY_SCAM -> Color(0xFF2ECC71)
    CrimeType.MUGGING, CrimeType.BREAKING_AND_ENTERING, CrimeType.DRUG_DEALING, CrimeType.COUNTERFEIT_GOODS -> Color(0xFFFFC107)
    CrimeType.BURGLARY, CrimeType.FRAUD, CrimeType.ARMS_SMUGGLING, CrimeType.DRUG_TRAFFICKING -> Color(0xFFFF7043)
    else -> Color(0xFFE53935)
}

private fun parseCrimeType(typeKey: String): CrimeType? = runCatching { CrimeType.valueOf(typeKey) }.getOrNull()
private fun categoryLabelFor(type: CrimeType): String = when (type) {
    CrimeType.PICKPOCKETING, CrimeType.SHOPLIFTING, CrimeType.VANDALISM, CrimeType.PETTY_SCAM -> "Street Crimes"
    CrimeType.MUGGING, CrimeType.BREAKING_AND_ENTERING, CrimeType.DRUG_DEALING, CrimeType.COUNTERFEIT_GOODS -> "Robbery"
    CrimeType.BURGLARY, CrimeType.FRAUD, CrimeType.ARMS_SMUGGLING, CrimeType.DRUG_TRAFFICKING -> "Heists & Smuggling"
    else -> "Mastermind Tier"
}
