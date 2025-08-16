@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.liveongames.liveon.ui

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.domain.model.GameEvent
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.AllGameThemes
import com.liveongames.liveon.ui.theme.LiveonTheme
import com.liveongames.liveon.viewmodel.GameViewModel
import com.liveongames.liveon.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import org.json.JSONObject
import kotlin.random.Random
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
// Animations
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.shape.RoundedCornerShape
import com.liveongames.liveon.ui.LocalChromeInsets
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.isActive

/* ────────────────────────────── Persistent Chrome API ────────────────────────────── */

data class ChromeInsets(val bottom: Dp)
val LocalChromeInsets = staticCompositionLocalOf { ChromeInsets(0.dp) }

/**
 * Wrap your NavHost or any screen inside this. The Stats panel and Life Management menu
 * stay mounted across navigation, and the content can use LocalChromeInsets for bottom padding.
 */
@Composable
fun LiveonChromeHost(
    // Provide VMs here so the chrome shows current stats everywhere
    gameViewModel: GameViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    showHeader: Boolean = true,
    onNavigateToCrime: () -> Unit = {},
    onNavigateToPets: () -> Unit = {},
    onNavigateToEducation: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNewLife: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val uiState by gameViewModel.uiState.collectAsState()
    val selectedThemeIndex by settingsViewModel.selectedThemeIndex.collectAsState()
    val currentTheme = AllGameThemes.getOrElse(selectedThemeIndex) { AllGameThemes[0] }

    // Shell state (persists while app is running)
    var isMenuOpen by rememberSaveable { mutableStateOf(false) }

    // Bottom chrome visible height (keep in sync with StatsPanel)
    val bottomChrome = 75.dp

    androidx.compose.runtime.CompositionLocalProvider(
        LocalChromeInsets provides ChromeInsets(bottom = bottomChrome)
    ) {
        Box(Modifier.fillMaxSize().background(currentTheme.background)) {

            // Header (only on Home / when requested)
            if (showHeader) {
                GameHeader(currentTheme = currentTheme)
            }

            // App content (your NavHost or Home content)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = if (showHeader) 96.dp else 0.dp, // no empty band on feature screens
                        bottom = bottomChrome
                    )
            ) {
                content()
            }

            // Persistent Stats panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                StatsPanel(
                    uiState = uiState,
                    currentTheme = currentTheme,
                    onMenuOpen = { isMenuOpen = true }
                )
            }

            // Persistent Life Management menu overlay
            if (isMenuOpen) {
                PopupMenu(
                    uiState = uiState,
                    currentTheme = currentTheme,
                    onDismiss = { isMenuOpen = false },
                    onNavigateToCrime = {
                        isMenuOpen = false
                        onNavigateToCrime()
                    },
                    onNavigateToPets = {
                        isMenuOpen = false
                        onNavigateToPets()
                    },
                    onNavigateToEducation = {
                        isMenuOpen = false
                        onNavigateToEducation()
                    },
                    onNavigateToSettings = {
                        isMenuOpen = false
                        onNavigateToSettings()
                    },
                    onNavigateToNewLife = {
                        isMenuOpen = false
                        onNavigateToNewLife()
                    }
                )
            }
        }
    }
}

/* ────────────────────────────── Home Screen (uses shell) ────────────────────────────── */

@Composable
fun LiveonGameScreen(
    gameViewModel: GameViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToCrime: () -> Unit = {},
    onNavigateToPets: () -> Unit = {},
    onNavigateToEducation: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNewLife: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by gameViewModel.uiState.collectAsState()
    val selectedThemeIndex by settingsViewModel.selectedThemeIndex.collectAsState()
    val currentTheme = AllGameThemes.getOrElse(selectedThemeIndex) { AllGameThemes.first() }

    // Lifebook state
    val lifebookScrollState = rememberScrollState()
    var lifeHistoryEntries by rememberSaveable { mutableStateOf(listOf<String>()) }
    var lastAge by rememberSaveable { mutableStateOf<Int?>(null) }

    // Animations used by Age Up
    val hourglassRotation = rememberInfiniteTransition(label = "hourglass").animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(2400, easing = LinearEasing)),
        label = "hourglassSpin"
    )
    val glowAnimation = rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    // Cooldown after age up (prevents spam)
    var isCooldown by rememberSaveable { mutableStateOf(false) }
    var cooldownProgress by rememberSaveable { mutableStateOf(0f) } // kept for future use

    // Demo life events
    val raw = loadJsonFromAssets(context, "life_events.json")
    val randomEvents = remember(raw) { parseRandomEvents(raw) }

    LaunchedEffect(uiState.playerStats?.age) {
        val currentAge = uiState.playerStats?.age ?: 0
        if (lastAge != null && currentAge > lastAge!!) {
            isCooldown = true
            cooldownProgress = 0f

            val eventCount = kotlin.random.Random.nextInt(1, 4)
            val events = generateRandomLifeEvents(randomEvents, eventCount)
            for (i in events.indices) {
                delay(1000L)
                val entry = "Age $currentAge: ${events[i]}"
                if (!lifeHistoryEntries.contains(entry)) {
                    lifeHistoryEntries = (lifeHistoryEntries + entry).distinct()
                    delay(200L)
                    lifebookScrollState.animateScrollTo(lifebookScrollState.maxValue)
                }
            }

            // simple cooldown timer
            val cooldownMs = 2500L
            val steps = 50
            val stepMs = cooldownMs / steps
            repeat(steps) {
                delay(stepMs)
                cooldownProgress = (it + 1) / steps.toFloat()
            }
            isCooldown = false
        }
        lastAge = currentAge
    }
    val insets = LocalChromeInsets.current
    // IMPORTANT: Do NOT render header or stats here; the host does it.
    GameContent(
        uiState = uiState,
        currentTheme = currentTheme,
        lifebookScrollState = lifebookScrollState,
        lifeHistoryEntries = lifeHistoryEntries,
        entryAlphas = rememberEntryAlphas(lifeHistoryEntries.size),
        isCooldown = isCooldown,
        hourglassRotation = hourglassRotation.value,
        glowAnimation = glowAnimation.value,
        gameViewModel = gameViewModel,
        bottomInset = insets.bottom
    )
}

/* ────────────────────────────── Main Content (home) ────────────────────────────── */

@Composable
fun GameContent(
    uiState: com.liveongames.liveon.viewmodel.GameUiState,
    currentTheme: LiveonTheme,
    lifebookScrollState: androidx.compose.foundation.ScrollState,
    lifeHistoryEntries: List<String>,
    entryAlphas: List<MutableState<Float>>,
    isCooldown: Boolean,
    hourglassRotation: Float,
    glowAnimation: Float,
    gameViewModel: GameViewModel,
    bottomInset: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .padding(bottom = bottomInset), // host reserves top/bottom already
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // DO NOT render GameHeader here (host already renders it)

        LifebookSection(
            uiState = uiState,
            currentTheme = currentTheme,
            lifebookScrollState = lifebookScrollState,
            lifeHistoryEntries = lifeHistoryEntries,
            entryAlphas = entryAlphas,
            isCooldown = isCooldown,
            hourglassRotation = hourglassRotation,
            glowAnimation = glowAnimation,
            gameViewModel = gameViewModel
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/* ────────────────────────────── Header / Lifebook / AgeUp ────────────────────────────── */

@Composable
fun GameHeader(
    currentTheme: LiveonTheme,
    showTagline: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.liveon_logo),
            contentDescription = "Liveon logo",
            modifier = Modifier
                .height(96.dp)
                .wrapContentWidth(),
            contentScale = ContentScale.Fit
        )

        if (showTagline) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Life Without Limits",
                style = MaterialTheme.typography.bodyMedium,
                color = currentTheme.accent,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LifebookSection(
    uiState: com.liveongames.liveon.viewmodel.GameUiState,
    currentTheme: LiveonTheme,
    lifebookScrollState: androidx.compose.foundation.ScrollState,
    lifeHistoryEntries: List<String>,
    entryAlphas: List<MutableState<Float>>,
    isCooldown: Boolean,
    hourglassRotation: Float,
    glowAnimation: Float,
    gameViewModel: GameViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 25.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = currentTheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_yearbook),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = currentTheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lifebook",
                        style = MaterialTheme.typography.titleMedium,
                        color = currentTheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1.0f))
                    Text(
                        text = currentTheme.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = currentTheme.accent,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(lifebookScrollState)
                    ) {
                        if (lifeHistoryEntries.isEmpty()) {
                            Text(
                                text = "Your life story will appear here...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = currentTheme.accent,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        } else {
                            lifeHistoryEntries.forEachIndexed { index, entry ->
                                LifebookEntryItem(
                                    entry = entry,
                                    alpha = if (index < entryAlphas.size) entryAlphas[index].value else 1f,
                                    theme = currentTheme
                                )
                                if (index < lifeHistoryEntries.size - 1) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = (35).dp)
                .height(70.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            AgeUpButton(
                isCooldown = isCooldown,
                hourglassRotation = hourglassRotation,
                glowAnimation = glowAnimation,
                currentTheme = currentTheme,
                onAgeUp = { gameViewModel.ageUp() }
            )
        }
    }
}

@Composable
fun AgeUpButton(
    isCooldown: Boolean,               // true only during/just after Age Up
    hourglassRotation: Float,          // kept for call-site compatibility (ignored)
    glowAnimation: Float,              // kept for your glow pulse
    currentTheme: LiveonTheme,
    onAgeUp: () -> Unit
) {
    // Spin only while cooldown is true
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isCooldown) {
        if (isCooldown) {
            while (isActive) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = tween(durationMillis = 1200, easing = LinearEasing)
                )
            }
        } else {
            rotation.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier.size(70.dp),
        contentAlignment = Alignment.Center
    ) {
        // soft glow from your theme, pulsing via glowAnimation
        Box(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    scaleX = glowAnimation
                    scaleY = glowAnimation
                }
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            currentTheme.primary.copy(alpha = 0.30f),
                            currentTheme.primary.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        IconButton(
            onClick = { if (!isCooldown) onAgeUp() },
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(currentTheme.primary)
                .border(2.dp, currentTheme.accent, CircleShape),
            enabled = !isCooldown,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent,
                contentColor = currentTheme.text
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_hourglass),
                contentDescription = "Advance Time",
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer { rotationZ = rotation.value }, // ← conditional spin
                tint = Color.Unspecified
            )
        }
    }
}

/* ────────────────────────────── Stats Panel / Menu / Dialogs ────────────────────────────── */

@Composable
fun StatsPanel(
    uiState: com.liveongames.liveon.viewmodel.GameUiState,
    currentTheme: LiveonTheme,
    onMenuOpen: () -> Unit
) {
    val closedVisibleHeight = 160
    val panelRestPosition = 5

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(closedVisibleHeight.dp)
            .offset(y = (-panelRestPosition).dp)
            .background(currentTheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(currentTheme.primary)
                    .clickable { onMenuOpen() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = currentTheme.text
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Life Management",
                        style = MaterialTheme.typography.titleMedium,
                        color = currentTheme.text,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Stats body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
                    .background(currentTheme.surface)
                    .padding(vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_person),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = currentTheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Stats",
                            style = MaterialTheme.typography.titleSmall,
                            color = currentTheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CompactStatItem("Age", uiState.playerStats?.age?.toString() ?: "0", R.drawable.ic_yearbook, currentTheme.primary, currentTheme)
                            CompactStatItem("Health", uiState.playerStats?.health?.toString() ?: "0", R.drawable.ic_health, Color(0xFF4CAF50), currentTheme)
                            CompactStatItem("Happiness", uiState.playerStats?.happiness?.toString() ?: "0", R.drawable.ic_happiness_new, Color(0xFFFF9800), currentTheme)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CompactStatItem("Intelligence", uiState.playerStats?.intelligence?.toString() ?: "0", R.drawable.ic_brain, Color(0xFF2196F3), currentTheme)
                            CompactStatItem("Money", "$${uiState.playerStats?.money?.toString() ?: "0"}", R.drawable.ic_money, Color(0xFF9E9E9E), currentTheme)
                            CompactStatItem("Social", uiState.playerStats?.social?.toString() ?: "0", R.drawable.ic_people, Color(0xFFF44336), currentTheme)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PopupMenu(
    uiState: com.liveongames.liveon.viewmodel.GameUiState,
    currentTheme: LiveonTheme,
    onDismiss: () -> Unit,
    onNavigateToCrime: () -> Unit,
    onNavigateToPets: () -> Unit,
    onNavigateToEducation: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNewLife: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // SCRIM — this captures outside taps only
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable { onDismiss() }
        )

        // SHEET — no parent clickable; all row clicks propagate correctly
        ElevatedCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.78f)           // bottom sheet height cap
                .padding(vertical = 12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = currentTheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PopupMenuHeader(currentTheme = currentTheme, onDismiss = onDismiss)

                CurrentStatsCard(uiState = uiState, currentTheme = currentTheme)

                // Scrollable menu list inside the sheet
                val menuItems = listOf(
                    MenuItemData(R.drawable.ic_business, "Career", "Manage your professional life", currentTheme) { },
                    MenuItemData(R.drawable.ic_people, "Social", "Manage relationships", currentTheme) { },
                    MenuItemData(R.drawable.ic_law, "Criminal Activities", "View criminal activities.", currentTheme) { onNavigateToCrime() },
                    MenuItemData(R.drawable.ic_band, "Pet Management", "Adopt companions", currentTheme) { onNavigateToPets() },
                    MenuItemData(R.drawable.ic_education, "Education", "Manage schooling", currentTheme) { onNavigateToEducation() },
                    MenuItemData(R.drawable.ic_relationship, "Relationships", "View connections", currentTheme) { },
                    MenuItemData(R.drawable.ic_health, "Healthcare", "View medical history and seek care", currentTheme) { },
                    MenuItemData(R.drawable.ic_travel, "Travel Log", "View places visited", currentTheme) { },
                    MenuItemData(R.drawable.ic_save, "Save Game", "Manage saves", currentTheme) { },
                    // NEW LIFE
                    MenuItemData(R.drawable.ic_continue, "New Life", "Start a new character", currentTheme) { onNavigateToNewLife() },
                    MenuItemData(R.drawable.ic_settings, "Settings", "Game preferences including theme selection", currentTheme) { onNavigateToSettings() }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(menuItems) { item ->
                        MenuItemRow(item = item)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 3.dp),
                            thickness = 1.dp,
                            color = currentTheme.surfaceVariant
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
fun PopupMenuHeader(
    currentTheme: LiveonTheme,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Life Management",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface, // ← material color, not your custom theme
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = "Close"
            )
        }
    }
}

@Composable
fun CurrentStatsCard(
    uiState: com.liveongames.liveon.viewmodel.GameUiState,
    currentTheme: LiveonTheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = currentTheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Current Stats",
                    style = MaterialTheme.typography.titleSmall,
                    color = currentTheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CompactStatItem("Age", uiState.playerStats?.age?.toString() ?: "0", R.drawable.ic_yearbook, currentTheme.primary, currentTheme)
                    CompactStatItem("Health", uiState.playerStats?.health?.toString() ?: "0", R.drawable.ic_health, Color(0xFF4CAF50), currentTheme)
                    CompactStatItem("Happiness", uiState.playerStats?.happiness?.toString() ?: "0", R.drawable.ic_happiness_new, Color(0xFFFF9800), currentTheme)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CompactStatItem("Intelligence", uiState.playerStats?.intelligence?.toString() ?: "0", R.drawable.ic_brain, Color(0xFF2196F3), currentTheme)
                    CompactStatItem("Money", "$${uiState.playerStats?.money?.toString() ?: "0"}", R.drawable.ic_money, Color(0xFF9E9E9E), currentTheme)
                    CompactStatItem("Social", uiState.playerStats?.social?.toString() ?: "0", R.drawable.ic_people, Color(0xFFF44336), currentTheme)
                }
            }
        }
    }
}

@Composable
fun MenuOptionsSection(
    currentTheme: LiveonTheme,
    onNavigateToCrime: () -> Unit,
    onNavigateToPets: () -> Unit,
    onNavigateToEducation: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNewLife: () -> Unit              // ← NEW
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Menu Options:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val menuItems = listOf(
            MenuItemData(R.drawable.ic_business, "Career", "Manage your professional life", currentTheme) { },
            MenuItemData(R.drawable.ic_people, "Social", "Manage relationships", currentTheme) { },
            MenuItemData(R.drawable.ic_law, "Criminal Activities", "View criminal activities.", currentTheme) {
                onNavigateToCrime()
            },
            MenuItemData(R.drawable.ic_band, "Pet Management", "Adopt companions", currentTheme) {
                onNavigateToPets()
            },
            MenuItemData(R.drawable.ic_education, "Education", "Manage schooling", currentTheme) {
                onNavigateToEducation()
            },
            MenuItemData(R.drawable.ic_relationship, "Relationships", "View connections", currentTheme) { },
            MenuItemData(R.drawable.ic_health, "Healthcare", "View medical history and seek care", currentTheme) { },
            MenuItemData(R.drawable.ic_travel, "Travel Log", "View places visited", currentTheme) { },
            MenuItemData(R.drawable.ic_save, "Save Game", "Manage saves", currentTheme) { },

            // ↓↓↓ NEW LIFE ENTRY (choose any icon you prefer)
            MenuItemData(R.drawable.ic_continue, "New Life", "Start a new character", currentTheme) {
                onNavigateToNewLife()
            },

            MenuItemData(R.drawable.ic_settings, "Settings", "Game preferences including theme selection", currentTheme) {
                onNavigateToSettings()
            }
        )

        LazyColumn(modifier = Modifier.weight(1.0f)) {
            items(menuItems) { item ->
                MenuItemRow(item = item)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 3.dp),
                    thickness = 1.dp,
                    color = currentTheme.surfaceVariant
                )
            }
        }
    }
}

/* ────────────────────────────── Event Dialogs (unchanged) ────────────────────────────── */

@Composable
fun EventDialogs(
    uiState: com.liveongames.liveon.viewmodel.GameUiState,
    currentTheme: LiveonTheme,
    gameViewModel: GameViewModel
) {
    uiState.activeEvents
        .filterIsInstance<GameEvent>()
        .filter { event ->
            !event.title.contains("Age", ignoreCase = true) &&
                    !event.title.contains("Year", ignoreCase = true) &&
                    !event.title.contains("Birthday", ignoreCase = true)
        }
        .firstOrNull()
        ?.let { firstEvent ->
            EventDialogComposable(
                event = firstEvent,
                onChoiceSelected = { choiceId ->
                    gameViewModel.makeChoice(firstEvent.id, choiceId)
                },
                onDismiss = { gameViewModel.dismissEvent(firstEvent.id) },
                theme = currentTheme
            )
        }
}

/* ────────────────────────────── Utility Composables ────────────────────────────── */

@Composable
fun LifebookEntryItem(
    entry: String,
    alpha: Float,
    theme: LiveonTheme
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        color = theme.surfaceVariant.copy(alpha = 0.7f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .alpha(alpha)
        ) {
            Text(
                text = entry,
                style = MaterialTheme.typography.bodySmall,
                color = theme.text,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun CompactStatItem(
    label: String,
    value: String,
    iconId: Int,
    color: Color,
    theme: LiveonTheme
) {
    Row(
        modifier = Modifier
            .width(85.dp)
            .padding(horizontal = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(3.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = theme.text,
                fontSize = 9.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = theme.accent,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

data class MenuItemData(
    val iconResId: Int,
    val title: String,
    val description: String,
    val theme: LiveonTheme,
    val onClick: () -> Unit
)

@Composable
fun MenuItemRow(item: MenuItemData) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(vertical = 5.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = item.theme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = item.iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = item.theme.primary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = item.theme.text,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = item.theme.accent,
                    fontSize = 10.sp
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_continue),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = item.theme.accent
            )
        }
    }
}

@Composable
fun EventDialogComposable(
    event: GameEvent,
    onChoiceSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    theme: LiveonTheme
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = theme.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.text
                )
                if (event.choices.isNotEmpty()) {
                    Text(
                        text = "What do you choose?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = theme.primary
                    )
                    event.choices.forEach { choice ->
                        androidx.compose.material3.Button(
                            onClick = { onChoiceSelected(choice.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = theme.surfaceVariant,
                                contentColor = theme.text
                            )
                        ) { Text(choice.text, fontWeight = FontWeight.Medium) }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        },
        confirmButton = {
            if (event.choices.isEmpty()) {
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text("Continue", color = theme.primary)
                }
            }
        },
        containerColor = theme.surface
    )
}

/* ────────────────────────────── Random Events Helpers (unchanged) ────────────────────────────── */

fun loadRandomEventsFromAssets(context: Context): Map<String, List<String>> {
    return try {
        val jsonString = context.assets.open("random_events.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)

        val categories = listOf(
            "world_events", "local_events", "social_events",
            "personal_events", "travel_events", "pop_culture_events",
            "career_events", "random_events"
        )

        categories.associateWith { category ->
            val jsonArray = jsonObject.getJSONArray(category)
            val events = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                events.add(jsonArray.getString(i))
            }
            events
        }
    } catch (e: Exception) {
        Log.e("Lifebook", "Error loading random events: ${e.message}")
        mapOf(
            "world_events" to listOf("A major breakthrough in renewable energy was announced globally."),
            "local_events" to listOf("The local community center held its annual summer fair."),
            "social_events" to listOf("Your childhood friend sent you a thoughtful message."),
            "personal_events" to listOf("You felt a new sense of clarity about your future."),
            "travel_events" to listOf("You discovered an interesting travel documentary."),
            "pop_culture_events" to listOf("A new indie band released a catchy single."),
            "career_events" to listOf("You learned a new skill that could help your career."),
            "random_events" to listOf("You found a lucky coin on the sidewalk.")
        )
    }
}

fun generateRandomLifeEvents(eventsMap: Map<String, List<String>>, eventCount: Int): List<String> {
    val events = mutableListOf<String>()
    repeat(eventCount) {
        val categories = eventsMap.keys.toList()
        val category = categories.random()
        val categoryEvents = eventsMap[category] ?: emptyList()
        if (categoryEvents.isNotEmpty()) events.add(categoryEvents.random())
    }
    return events.distinct()


}

// ---------- tiny helpers restored ----------

private fun loadJsonFromAssets(context: Context, fileName: String): String {
    return try {
        context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (_: Exception) {
        // safe fallback stub
        "{\"events\":[\"Made a new friend\",\"Learned to ride a bike\",\"Found $5 in a pocket\",\"Won a spelling bee\"]}"
    }
}

private fun parseRandomEvents(jsonString: String): List<String> {
    return try {
        val json = JSONObject(jsonString)
        val arr = json.optJSONArray("events") ?: return emptyList()
        (0 until arr.length()).map { i -> arr.getString(i) }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun generateRandomLifeEvents(events: List<String>, count: Int): List<String> {
    if (events.isEmpty()) return listOf("A quiet year passed.")
    val n = minOf(count, events.size)
    return events.shuffled().take(n)
}

@Composable
fun rememberEntryAlphas(size: Int): List<MutableState<Float>> {
    val transition = rememberInfiniteTransition(label = "entryFades")

    // Compose animation states (internal)
    val animatedStates = List(size) { idx ->
        transition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2400 + (idx * 40), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha_$idx"
        )
    }

    // Public API your file expects: MutableState<Float>
    val exposed = remember(size) { List(size) { mutableStateOf(1f) } }

    // Bridge: push animated values into the mutable states each recomposition
    animatedStates.forEachIndexed { i, s -> exposed[i].value = s.value }

    return exposed
}
