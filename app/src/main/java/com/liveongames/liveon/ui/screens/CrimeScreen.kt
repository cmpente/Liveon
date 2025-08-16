// app/src/main/java/com/liveongames/liveon/ui/screens/CrimeScreen.kt

package com.liveongames.liveon.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.liveongames.liveon.viewmodel.CrimeViewModel
import com.liveongames.liveon.viewmodel.CrimeViewModel.CrimeType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min
import androidx.compose.material3.ButtonDefaults

/* ---------- file-local types & savers ---------- */

private enum class RecordFilter { ALL, SUCCESS, FAIL, CAUGHT }

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

            // 1) Criminal Card
            CriminalHeaderCard(
                notoriety = notoriety,
                cooldownUntil = cooldownUntil,
                records = records
            )

            Spacer(Modifier.height(10.dp))

            // 2) Accordion + 3) In-row progression
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(items = catalog, key = { _, it -> it.title }) { _, cat ->
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

                // 5) Rap-Sheet (contained card)
                item {
                    Spacer(Modifier.height(12.dp))
                    CriminalRecordSection(
                        records = sortedRecords,
                        deltaFor = { rec -> viewModel.effectiveNotorietyForRecord(rec) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        // 4) Dramatic outcome flash
        val flashColor = revealedOutcome?.let { outcomeColor(it) } ?: Color.White
        AnimatedVisibility(visible = flashOverlay, enter = fadeIn(tween(60)), exit = fadeOut(tween(60))) {
            Box(Modifier.fillMaxSize().background(flashColor.copy(alpha = 0.25f)))
        }
    }
}

/* ============================== Top Card ============================== */

@Composable
private fun CriminalHeaderCard(
    notoriety: Int,
    cooldownUntil: Long?,
    records: List<CrimeRecordEntry>
) {
    val t = LocalLiveonTheme.current
    val now = System.currentTimeMillis()
    val cdActive = (cooldownUntil ?: 0L) > now
    val cdSecs = if (cdActive) (((cooldownUntil ?: 0L) - now) / 1000).coerceAtLeast(0) else 0

    val alias = remember(notoriety) { aliasForNotoriety(notoriety) }
    val rankLabel = remember(notoriety) { topCardRankFor(notoriety) } // "Lookout" first tier
    val specialty: String = remember(records) { deriveSpecialty(records) }
    val streak: Int = remember(records) { computeSuccessStreak(records) }

    Surface(
        color = LocalLiveonTheme.current.surfaceElevated,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                Modifier
                    .size(48.dp)
                    .background(LocalLiveonTheme.current.primary.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(R.drawable.ic_person), contentDescription = null, tint = LocalLiveonTheme.current.primary)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                if (alias.unlocked) {
                    Text(
                        text = alias.label,
                        color = LocalLiveonTheme.current.text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = rankLabel,
                        color = LocalLiveonTheme.current.text.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = rankLabel,
                        color = LocalLiveonTheme.current.text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = alias.progress,
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        trackColor = LocalLiveonTheme.current.text.copy(alpha = 0.10f),
                        color = LocalLiveonTheme.current.primary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text("Alias: locked", color = LocalLiveonTheme.current.text.copy(alpha = 0.70f), style = MaterialTheme.typography.labelMedium)
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatBadge(
                        label = "Specialty",
                        value = if (specialty != "—") specialty else "—",
                        modifier = Modifier.weight(1f)
                    )
                    StatBadge(
                        label = "Streak",
                        value = "x$streak",
                        modifier = Modifier.widthIn(min = 72.dp)
                    )
                    StatBadge(
                        label = "Heat",
                        value = if (cdActive) "${cdSecs}s" else "Low",
                        emphasize = cdActive,
                        modifier = Modifier.widthIn(min = 64.dp)
                    )
                }
            }
        }
    }
}

private fun topCardRankFor(n: Int): String = when {
    n < 5   -> "Lookout"        // changed from "Greenhorn"
    n < 12  -> "Pickpocket"
    n < 25  -> "Hustler"
    n < 40  -> "Enforcer"
    n < 55  -> "Fixer"
    n < 72  -> "Shot Caller"
    n < 90  -> "Capo"
    else    -> "Kingpin"
}

private data class AliasInfo(val unlocked: Boolean, val label: String, val progress: Float)
private fun aliasForNotoriety(n: Int): AliasInfo {
    val tiers = listOf(
        10 to "The Red Viper",
        25 to "The Ghost",
        40 to "Iron Jackal",
        60 to "Night Magistrate",
        80 to "Kingmaker",
        100 to "The Untouchable"
    )
    val unlocked = tiers.lastOrNull { n >= it.first }?.second
    if (unlocked != null) return AliasInfo(true, unlocked, 1f)
    val next = tiers.first().first
    val p = (n.toFloat() / next.toFloat()).coerceIn(0f, 1f)
    return AliasInfo(false, "", p)
}

@Composable private fun StatBadge(
    label: String,
    value: String,
    emphasize: Boolean = false,
    modifier: Modifier = Modifier
) {
    val t = LocalLiveonTheme.current
    val bg = if (emphasize) Color(0xFFFF7043).copy(alpha = 0.16f) else t.surface.copy(alpha = 0.65f)

    Surface(
        color = bg,
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 1.dp,
        modifier = modifier.heightIn(min = 32.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = t.text.copy(alpha = 0.70f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
            Text(
                text = value,
                color = t.text,
                style = MaterialTheme.typography.labelLarge,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
                Icon(painter = painterResource(id = if (expanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more), contentDescription = null, tint = textColor)
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

    // NEW: local state to gate "initiate" confirmation UI
    var awaitingConfirm by rememberSaveable(type) { mutableStateOf(false) }

    val rowIsIdle = runState == null && outcome == null

    Column {
        CrimeRow(
            iconRes = getCrimeIconRes(type),
            title = title,
            subtitle = subtitle,
            enabled = enabled,
            textColor = textColor,
            accent = accent
        ) {
            if (enabled && rowIsIdle) {
                // Toggle the confirm panel instead of instantly starting
                awaitingConfirm = !awaitingConfirm
            }
        }

        AnimatedVisibility(
            visible = (awaitingConfirm && rowIsIdle) || runState != null || outcome != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                when {
                    // 1) Awaiting confirmation (idle)
                    awaitingConfirm && rowIsIdle -> {
                        // A compact confirm card, matching your theme
                        ElevatedCard(
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = textColor.copy(alpha = 0.06f)
                            )
                        ) {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Initiate ${title}?",
                                    color = textColor,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    getCrimeDesc(type),
                                    color = textColor.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { awaitingConfirm = false },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Close") }
                                    Button(
                                        onClick = {
                                            awaitingConfirm = false
                                            onStart() // ← beginCrime(type)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = accent,
                                            contentColor = Color.Black.copy(alpha = 0.88f)
                                        )
                                    ) { Text("Initiate") }
                                }
                            }
                        }
                    }

                    // 2) Active run
                    runState != null -> {
                        // existing in-run UI (marquee, ambient, progress, Back Out)
                        NarrativeMarquee(
                            runKey = runState.startedAtMs,
                            progress = runState.progress,
                            script = runState.scriptAll,
                            isClimax = runState.phase == CrimeViewModel.Phase.CLIMAX,
                            textColor = textColor
                        )
                        Spacer(Modifier.height(6.dp))
                        AmbientTicker(
                            runKey = runState.startedAtMs,
                            ambient = runState.ambientLines,
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
                        Spacer(Modifier.height(10.dp))
                        val showBackOut = runState.phase != CrimeViewModel.Phase.SETUP
                        AnimatedVisibility(visible = showBackOut) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Spacer(Modifier.weight(1f))
                                HoldToBackOutButton(
                                    label = "Back Out",
                                    onConfirmed = onCancel,
                                    textColor = textColor
                                )
                            }
                        }
                    }

                    // 3) Outcome
                    outcome != null -> {
                        OutcomeReveal(
                            outcome = outcome,
                            onClose = onDismissOutcome,
                            textColor = textColor
                        )
                    }
                }
            }
        }
    }
}

/* ============== Narrative: ghost-write + smooth scroll (no typewriter) ============== */
@Composable
private fun NarrativeMarquee(
    runKey: Long,
    progress: Float,
    script: String,
    isClimax: Boolean,
    textColor: Color
) {
    val style = MaterialTheme.typography.bodyMedium
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    val lineHeight = with(LocalDensity.current) { style.lineHeight.toDp() }
    val reservedHeight = lineHeight * 1.2f

    val layout = remember(runKey, script) {
        measurer.measure(
            AnnotatedString(script),
            style = style,
            constraints = Constraints(maxWidth = Int.MAX_VALUE)
        )
    }
    val textWidthPx = layout.size.width.toFloat()

    val revealPxRaw = textWidthPx * progress.coerceIn(0f, 1f)
    val revealPx = if (progress > 0f) maxOf(revealPxRaw, 2f) else 0f

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(reservedHeight)
    ) {
        val viewport = with(LocalDensity.current) { maxWidth.toPx() }
        val targetScroll = (revealPx - viewport).coerceAtLeast(0f)
        val scroll by animateFloatAsState(
            targetValue = targetScroll,
            animationSpec = tween(durationMillis = 100, easing = LinearEasing),
            label = "narrative-scroll"
        )

        // Back (dim, full paragraph)
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

        // Front (revealed portion)
        val leftFade = with(LocalDensity.current) { 16.dp.toPx() }
        val rightFade = with(LocalDensity.current) { 24.dp.toPx() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    val clipRight = min(viewport, (revealPx - scroll).coerceAtLeast(0f))
                    if (clipRight > 0f) {
                        clipRect(left = 0f, top = 0f, right = clipRight, bottom = size.height) {
                            drawText(textLayoutResult = layout, topLeft = Offset(-scroll, 0f), color = textColor)
                        }
                    }
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

        if (isClimax) {
            val ls = (lerp(6f, 0f, progress.coerceIn(0f, 1f))).sp
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                Text("…", color = textColor.copy(alpha = 0.9f), style = style.copy(letterSpacing = ls), maxLines = 1)
            }
        }
    }
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

/* ======================= Ambient (italic, forward-only, no repeats) ======================= */
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

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = textColor.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            Modifier
                .background(wash)
                .graphicsLayer { translationY = transY }
                .heightIn(min = 140.dp) // enough height to center content nicely
                .fillMaxWidth()
        ) {
            // Close icon (corner)
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Close",
                    tint = outcomeColor(outcome)
                )
            }

            // Centered content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val resultLabel = when {
                    outcome.wasCaught -> "Caught"
                    outcome.success && outcome.moneyGained > 0 -> "Success"
                    outcome.success -> "Partial Success"
                    else -> "Failed"
                }
                Text(
                    resultLabel,
                    fontWeight = FontWeight.SemiBold,
                    color = outcomeColor(outcome),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    outcome.climaxLine.ifBlank { "It’s done." },
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                if (outcome.moneyGained > 0)
                    Text("Payout: \$${outcome.moneyGained}", color = textColor.copy(alpha = 0.9f), textAlign = TextAlign.Center)
                if (outcome.jailDays > 0)
                    Text("Jail time: ${outcome.jailDays} day(s)", color = textColor.copy(alpha = 0.9f), textAlign = TextAlign.Center)
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

/* ============================== Hold-to-back-out ============================== */
@Composable
private fun HoldToBackOutButton(label: String, onConfirmed: () -> Unit, textColor: Color, holdMillis: Long = 800L) {
    var show by remember { mutableStateOf(false) }
    TextButton(onClick = { show = true }) { Text(label) }
    if (show) {
        HoldToConfirmDialog(
            title = "Hold to back out",
            message = "Press and hold to back out of this crime.",
            onDismiss = { show = false },
            onConfirmed = { show = false; onConfirmed() },
            textColor = textColor,
            holdMillis = holdMillis
        )
    }
}

@Composable
private fun HoldToConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirmed: () -> Unit,
    textColor: Color,
    holdMillis: Long
) {
    var progress by remember { mutableStateOf(0f) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Back") } },
        title = { Text(title, color = textColor) },
        text = {
            Column {
                Text(message, color = textColor.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
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

/* ============================== Records: Rap-Sheet Section ============================== */

@Composable
private fun CriminalRecordSection(
    records: List<CrimeRecordEntry>,
    deltaFor: (CrimeRecordEntry) -> Int?,
    modifier: Modifier = Modifier
) {
    val t = LocalLiveonTheme.current

    var filter by rememberSaveable { mutableStateOf(RecordFilter.ALL) }

    val filtered = remember(records, filter) {
        records.filter { r ->
            when (filter) {
                RecordFilter.ALL -> true
                RecordFilter.SUCCESS -> r.success && !r.caught
                RecordFilter.FAIL -> !r.success && !r.caught
                RecordFilter.CAUGHT -> r.caught
            }
        }.sortedByDescending { it.timestamp }
    }

    var visible by rememberSaveable { mutableStateOf(12) }
    val show = filtered.take(visible)
    val hasMore = filtered.size > show.size

    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = t.surfaceElevated)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 14.dp, top = 12.dp, end = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Criminal Record", color = t.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RecordFilterChip("All", filter == RecordFilter.ALL) { filter = RecordFilter.ALL }
                RecordFilterChip("Success", filter == RecordFilter.SUCCESS) { filter = RecordFilter.SUCCESS }
                RecordFilterChip("Fail", filter == RecordFilter.FAIL) { filter = RecordFilter.FAIL }
                RecordFilterChip("Caught", filter == RecordFilter.CAUGHT) { filter = RecordFilter.CAUGHT }
            }
        }
        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        val listState = rememberLazyListState()
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp, max = 280.dp)
        ) {
            if (show.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No entries yet", color = t.text.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(state = listState, contentPadding = PaddingValues(vertical = 6.dp)) {
                    val byYear: Map<Int, List<CrimeRecordEntry>> = show.groupBy { yearOf(it.timestamp) }
                    val years: List<Int> = byYear.keys.sortedDescending()

                    for (year in years) {
                        val yearList: List<CrimeRecordEntry> = byYear[year] ?: emptyList()
                        stickyHeaderCompat {
                            YearHeader(year = year, surfaceColor = t.surfaceElevated, textColor = t.text)
                        }
                        itemsIndexed(
                            items = yearList,
                            key = { _, rec -> rec.id }
                        ) { _, rec ->
                            RapSheetRow(rec = rec, deltaFor = deltaFor)
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        }
                    }

                    if (hasMore) {
                        item {
                            TextButton(onClick = { visible += 12 }, modifier = Modifier.fillMaxWidth().padding(6.dp)) {
                                Text("Show older…", color = t.text)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

// Compose 1.6.8 fallback: renders a normal item; swap to stickyHeader when available.
private fun LazyListScope.stickyHeaderCompat(
    content: @Composable () -> Unit
) {
    item(key = "hdr-${content.hashCode()}") { content() }
}

@Composable
private fun YearHeader(year: Int, surfaceColor: Color, textColor: Color) {
    Surface(
        color = surfaceColor.copy(alpha = 0.92f),
        tonalElevation = 2.dp,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("— $year —", color = textColor.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun RecordFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun RapSheetRow(
    rec: CrimeRecordEntry,
    deltaFor: (CrimeRecordEntry) -> Int?
) {
    val t = LocalLiveonTheme.current
    val type = parseCrimeType(rec.typeKey)
    val name = type?.let { getCrimeName(it) } ?: rec.typeKey
    val category = type?.let { categoryLabelFor(it) } ?: "—"

    val sdf = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }
    val date = remember(rec.timestamp) { sdf.format(Date(rec.timestamp)) }

    val (dispositionLabel, dispColor) = when {
        rec.caught -> "CAUGHT" to Color(0xFFE53935)
        rec.success && rec.money > 0 -> "SUCCESS" to Color(0xFF2E7D32)
        rec.success -> "PARTIAL" to Color(0xFF1976D2)
        else -> "FAIL" to Color(0xFFFFA000)
    }

    val notorietyDelta = remember(rec.id) { deltaFor(rec) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(6.dp),
            tonalElevation = 1.dp
        ) {
            Text(
                text = date,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                color = t.text.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = name,
                color = t.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = category,
                    color = t.text.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Crossfade(targetState = notorietyDelta, label = "noto-xfade") { nd ->
                    Text(
                        text = when {
                            nd == null -> "N ±—"
                            nd >= 0 -> "N +$nd"
                            else -> "N $nd"
                        },
                        color = when {
                            nd == null -> t.text.copy(alpha = 0.6f)
                            nd >= 0 -> Color(0xFF2E7D32)
                            else -> Color(0xFFE53935)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Surface(
            color = dispColor.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, dispColor.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = dispositionLabel,
                color = dispColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

/* ---------- Pure helpers ---------- */

private fun parseCrimeType(typeKey: String): CrimeType? =
    runCatching { CrimeType.valueOf(typeKey) }.getOrNull()

private fun categoryLabelFor(type: CrimeType): String = when (type) {
    CrimeType.PICKPOCKETING, CrimeType.SHOPLIFTING, CrimeType.VANDALISM, CrimeType.PETTY_SCAM -> "Street Crimes"
    CrimeType.MUGGING, CrimeType.BREAKING_AND_ENTERING, CrimeType.DRUG_DEALING, CrimeType.COUNTERFEIT_GOODS -> "Robbery"
    CrimeType.BURGLARY, CrimeType.FRAUD, CrimeType.ARMS_SMUGGLING, CrimeType.DRUG_TRAFFICKING -> "Heists & Smuggling"
    else -> "Mastermind Tier"
}

/** Returns the player’s most common category across records, as a short label. */
private fun deriveSpecialty(records: List<CrimeRecordEntry>): String {
    if (records.isEmpty()) return "—"
    val counts = mutableMapOf<String, Int>()
    for (rec in records) {
        val type = parseCrimeType(rec.typeKey) ?: continue
        val cat = categoryLabelFor(type)
        val short = when (cat) {
            "Street Crimes" -> "Street"
            "Robbery" -> "Robbery"
            "Heists & Smuggling" -> "Heists"
            "Mastermind Tier" -> "Mastermind"
            else -> cat
        }
        counts[short] = (counts[short] ?: 0) + 1
    }
    return counts.maxByOrNull { it.value }?.key ?: "—"
}

/** Counts the number of most-recent consecutive successes (not caught). */
private fun computeSuccessStreak(records: List<CrimeRecordEntry>): Int {
    if (records.isEmpty()) return 0
    val sorted = records.sortedByDescending { it.timestamp }
    var streak = 0
    for (r in sorted) {
        val ok = r.success && !r.caught
        if (ok) streak++ else break
    }
    return streak
}

private fun yearOf(ts: Long): Int {
    val cal = Calendar.getInstance().apply { timeInMillis = ts }
    return cal.get(Calendar.YEAR)
}

/* ============================== Catalog & Accents ============================== */
private data class CrimeUiItem(val type: CrimeType)
private data class CrimeSubcat(val title: String, val items: List<CrimeUiItem>)
private data class CrimeCat(val title: String, val subtitle: String, val subs: List<CrimeSubcat>)

private fun buildCrimeCatalog(): List<CrimeCat> = listOf(
    CrimeCat("Street Crimes", "Safer, modest gains", subs = listOf(
        CrimeSubcat("", listOf(
            CrimeUiItem(CrimeType.PICKPOCKETING),
            CrimeUiItem(CrimeType.SHOPLIFTING),
            CrimeUiItem(CrimeType.VANDALISM),
            CrimeUiItem(CrimeType.PETTY_SCAM)
        ))
    )),
    CrimeCat("Robbery", "Bigger rewards, higher danger", subs = listOf(
        CrimeSubcat("", listOf(
            CrimeUiItem(CrimeType.MUGGING),
            CrimeUiItem(CrimeType.BREAKING_AND_ENTERING),
            CrimeUiItem(CrimeType.DRUG_DEALING),
            CrimeUiItem(CrimeType.COUNTERFEIT_GOODS)
        ))
    )),
    CrimeCat("Heists & Smuggling", "High stakes, serious time", subs = listOf(
        CrimeSubcat("", listOf(
            CrimeUiItem(CrimeType.BURGLARY),
            CrimeUiItem(CrimeType.FRAUD),
            CrimeUiItem(CrimeType.ARMS_SMUGGLING),
            CrimeUiItem(CrimeType.DRUG_TRAFFICKING)
        ))
    )),
    CrimeCat("Mastermind Tier", "Elite jobs, massive risk", subs = listOf(
        CrimeSubcat("", listOf(
            CrimeUiItem(CrimeType.ARMED_ROBBERY),
            CrimeUiItem(CrimeType.EXTORTION),
            CrimeUiItem(CrimeType.KIDNAPPING_FOR_RANSOM),
            CrimeUiItem(CrimeType.PONZI_SCHEME),
            CrimeUiItem(CrimeType.CONTRACT_KILLING),
            CrimeUiItem(CrimeType.DARK_WEB_SALES),
            CrimeUiItem(CrimeType.ART_THEFT),
            CrimeUiItem(CrimeType.DIAMOND_HEIST),
            CrimeUiItem(CrimeType.BANK_HEIST),
            CrimeUiItem(CrimeType.POLITICAL_ASSASSINATION),
            CrimeUiItem(CrimeType.CRIME_SYNDICATE)
        ))
    ))
)

private fun accentForCrime(type: CrimeType): Color = when (type) {
    CrimeType.PICKPOCKETING, CrimeType.SHOPLIFTING, CrimeType.VANDALISM, CrimeType.PETTY_SCAM -> Color(0xFF2ECC71)
    CrimeType.MUGGING, CrimeType.BREAKING_AND_ENTERING, CrimeType.DRUG_DEALING, CrimeType.COUNTERFEIT_GOODS -> Color(0xFFFFC107)
    CrimeType.BURGLARY, CrimeType.FRAUD, CrimeType.ARMS_SMUGGLING, CrimeType.DRUG_TRAFFICKING -> Color(0xFFFF7043)
    else -> Color(0xFFE53935)
}
