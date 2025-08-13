// app/src/main/java/com/liveongames/liveon/ui/LiveonGameScreen.kt
package com.liveongames.liveon.ui

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.liveon.R
import com.liveongames.liveon.viewmodel.GameViewModel
import com.liveongames.liveon.viewmodel.SettingsViewModel
import com.liveongames.domain.model.GameEvent
import com.liveongames.liveon.ui.theme.AllGameThemes
import com.liveongames.liveon.ui.theme.LiveonTheme
import kotlinx.coroutines.delay
import org.json.JSONObject
import kotlin.random.Random

@Composable
fun LiveonGameScreen(
    gameViewModel: GameViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToCrime: () -> Unit = {},
    onNavigateToPets: () -> Unit = {},
    onNavigateToEducation: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val uiState by gameViewModel.uiState.collectAsState()
    val selectedThemeIndex by settingsViewModel.selectedThemeIndex.collectAsState()
    val currentTheme = AllGameThemes.getOrElse(selectedThemeIndex) { AllGameThemes[0] }

    // Game state
    var isMenuOpen by remember { mutableStateOf(false) }
    var lifeHistoryEntries by rememberSaveable { mutableStateOf(listOf<String>()) }
    val entryAlphas = remember(lifeHistoryEntries.size) { List(lifeHistoryEntries.size) { mutableStateOf(1f) } }
    var isCooldown by remember { mutableStateOf(false) }
    var cooldownProgress by remember { mutableStateOf(0f) }
    val lifebookScrollState = rememberScrollState()
    val randomEvents = remember(context) { loadRandomEventsFromAssets(context) }
    var lastAge by remember { mutableStateOf<Int?>(null) }

    val hourglassRotation by animateFloatAsState(
        targetValue = if (isCooldown) 360f * 3 else 0f,
        animationSpec = if (isCooldown) tween(durationMillis = 3000, easing = LinearEasing) else spring(),
        label = "hourglassRotation"
    )

    val glowAnimation by animateFloatAsState(
        targetValue = if (isCooldown) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAnimation"
    )

    // Handle aging logic
    LaunchedEffect(uiState.playerStats?.age) {
        val currentAge = uiState.playerStats?.age ?: 0
        if (lastAge != null && currentAge > lastAge!!) {
            isCooldown = true
            cooldownProgress = 0f

            val eventCount = Random.nextInt(1, 4)
            val events = generateRandomLifeEvents(randomEvents, eventCount)

            for (i in events.indices) {
                delay(1000L)
                val entry = "Age $currentAge: ${events[i]}"
                if (!lifeHistoryEntries.contains(entry)) {
                    lifeHistoryEntries = (lifeHistoryEntries + entry).distinct()
                    val newIndex = lifeHistoryEntries.size - 1
                    if (newIndex < entryAlphas.size) {
                        entryAlphas[newIndex].value = 0f
                        repeat(10) { step ->
                            delay(50)
                            entryAlphas[newIndex].value = (step + 1) / 10f
                        }
                    }
                }
                cooldownProgress = (i + 1).toFloat() / eventCount
            }

            delay(500L)
            isCooldown = false
        }
        lastAge = currentAge
    }

    LaunchedEffect(lifeHistoryEntries.size) {
        // Scroll to bottom of lifebook
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.background)
    ) {
        // Main game content
        GameContent(
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

        // Stats panel (always visible at bottom)
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

        // Popup menu (appears when menu is open)
        if (isMenuOpen) {
            PopupMenu(
                uiState = uiState,
                currentTheme = currentTheme,
                onDismiss = { isMenuOpen = false },
                onNavigateToCrime = onNavigateToCrime,
                onNavigateToPets = onNavigateToPets,
                onNavigateToEducation = onNavigateToEducation,
                onNavigateToSettings = onNavigateToSettings
            )
        }

        // Event dialogs
        EventDialogs(
            uiState = uiState,
            currentTheme = currentTheme,
            gameViewModel = gameViewModel
        )
    }
}

// ==================== MAIN CONTENT ====================
@Composable
fun GameContent(
    uiState: com.liveongames.liveon.viewmodel.GameUiState,
    currentTheme: LiveonTheme,
    lifebookScrollState: ScrollState,
    lifeHistoryEntries: List<String>,
    entryAlphas: List<MutableState<Float>>,
    isCooldown: Boolean,
    hourglassRotation: Float,
    glowAnimation: Float,
    gameViewModel: GameViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        GameHeader(currentTheme = currentTheme)

        // Lifebook Section with Age Up Button
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

        Spacer(modifier = Modifier.height(160.dp)) // Space for stats panel
    }
}

@Composable
fun GameHeader(currentTheme: LiveonTheme) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Liveon",
            style = MaterialTheme.typography.displaySmall,
            color = currentTheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Life Without Limits",
            style = MaterialTheme.typography.bodyMedium,
            color = currentTheme.accent
        )
    }
}

@Composable
fun LifebookSection(
    uiState: com.liveongames.liveon.viewmodel.GameUiState,
    currentTheme: LiveonTheme,
    lifebookScrollState: ScrollState,
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
                .padding(bottom = 25.dp), // Reduced padding to allow overlap
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

        // Age Up Button - positioned at bottom, overlapping lifebook
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
    isCooldown: Boolean,
    hourglassRotation: Float,
    glowAnimation: Float,
    currentTheme: LiveonTheme,
    onAgeUp: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(70.dp),
        contentAlignment = Alignment.Center
    ) {
        // Gradient Glow Background
        Box(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    scaleX = glowAnimation
                    scaleY = glowAnimation
                }
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        listOf(
                            currentTheme.primary.copy(alpha = 0.3f),
                            currentTheme.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Actual Button
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
                    .graphicsLayer {
                        rotationZ = hourglassRotation
                    },
                tint = Color.Unspecified
            )
        }
    }
}

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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Life Management Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(currentTheme.primary)
                    .clickable { onMenuOpen() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

            // Stats Content
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

// ==================== POPUP MENU ====================
@Composable
fun PopupMenu(
    uiState: com.liveongames.liveon.viewmodel.GameUiState,
    currentTheme: LiveonTheme,
    onDismiss: () -> Unit,
    onNavigateToCrime: () -> Unit,
    onNavigateToPets: () -> Unit,
    onNavigateToEducation: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .background(currentTheme.surface)
                .align(Alignment.BottomCenter)
        ) {
            // Header with close button
            PopupMenuHeader(
                currentTheme = currentTheme,
                onDismiss = onDismiss
            )

            // Current stats display
            CurrentStatsCard(
                uiState = uiState,
                currentTheme = currentTheme
            )

            // Menu options
            MenuOptionsSection(
                currentTheme = currentTheme,
                onNavigateToCrime = onNavigateToCrime,
                onNavigateToPets = onNavigateToPets,
                onNavigateToEducation = onNavigateToEducation,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    }
}

@Composable
fun PopupMenuHeader(
    currentTheme: LiveonTheme,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Life Management",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1.0f)
        )
        IconButton(
            onClick = onDismiss
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_collapse),
                contentDescription = "Close",
                tint = currentTheme.text
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
    onNavigateToSettings: () -> Unit
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
            MenuItemData(R.drawable.ic_settings, "Settings", "Game preferences including theme selection", currentTheme) {
                onNavigateToSettings()
            }
        )

        LazyColumn(
            modifier = Modifier.weight(1.0f)
        ) {
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

// ==================== EVENT DIALOGS ====================
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
                onDismiss = {
                    gameViewModel.dismissEvent(firstEvent.id)
                },
                theme = currentTheme
            )
        }
}

// ==================== UTILITY COMPONENTS ====================
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
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                        Button(
                            onClick = { onChoiceSelected(choice.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.surfaceVariant,
                                contentColor = theme.text
                            )
                        ) {
                            Text(
                                text = choice.text,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        },
        confirmButton = {
            if (event.choices.isEmpty()) {
                TextButton(onClick = onDismiss) {
                    Text("Continue", color = theme.primary)
                }
            }
        },
        containerColor = theme.surface
    )
}

// ==================== UTILITY FUNCTIONS ====================
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
        if (categoryEvents.isNotEmpty()) {
            events.add(categoryEvents.random())
        }
    }

    return events.distinct()
}