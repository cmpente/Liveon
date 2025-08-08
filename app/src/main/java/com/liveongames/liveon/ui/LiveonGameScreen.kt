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
import com.liveongames.liveon.ui.viewmodel.GameViewModel
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
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val uiState by gameViewModel.uiState.collectAsState()

    // Get selected theme from SettingsViewModel
    val selectedThemeIndex by settingsViewModel.selectedThemeIndex.collectAsState()
    val currentTheme = AllGameThemes.getOrElse(selectedThemeIndex) { AllGameThemes[0] }

// PANEL CONTROLS - TAP TO OPEN/CLOSE

// HEIGHT: Full height for stats panel when closed (shows ALL stats)
    val closedVisibleHeight = 160  // <-- FULL height - shows all stats

// OPEN STATE: Controls whether popup menu is visible
    var isMenuOpen by remember { mutableStateOf(false) } // <-- FALSE = stats only, TRUE = full menu

// Position where stats panel rests when closed (affects vertical placement)
// LOWERING: Increase value to move panel DOWN (away from top edge)
// RAISING: Decrease value to move panel UP (toward top edge)
// TIP: Low values (5-10) keep panel pinned to bottom of screen
    val panelRestPosition = 5 // <-- CONTROLS resting position on screen

    // Persistent storage for Lifebook entries
    var lifeHistoryEntries by rememberSaveable {
        mutableStateOf(listOf<String>())
    }

    // Entry animations
    val entryAlphas = remember(lifeHistoryEntries.size) {
        List(lifeHistoryEntries.size) { mutableStateOf(1f) }
    }

    // Cooldown state after aging
    var isCooldown by remember { mutableStateOf(false) }
    var cooldownProgress by remember { mutableStateOf(0f) }

    // Hourglass rotation animation
    val hourglassRotation by animateFloatAsState(
        targetValue = if (isCooldown) 360f * 3 else 0f,
        animationSpec = if (isCooldown) tween(
            durationMillis = 3000,
            easing = LinearEasing
        ) else spring(),
        label = "hourglassRotation"
    )

    // Animated glow for Age Up button
    val glowAnimation by animateFloatAsState(
        targetValue = if (isCooldown) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAnimation"
    )

    // Scroll state for Lifebook
    val lifebookScrollState = rememberScrollState()

    // Load random events from JSON assets
    val randomEvents = remember(context) {
        loadRandomEventsFromAssets(context)
    }

    // Generate random life events when aging up
    var lastAge by remember { mutableStateOf<Int?>(null) }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Liveon Title Header
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

            // Lifebook Section with Age Up Button Overlay - Increased height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 55.dp),
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
                            Spacer(modifier = Modifier.weight(1f))
                            // Current theme indicator
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
                                .weight(1f)
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
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
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

                        LaunchedEffect(lifeHistoryEntries.size) {
                            delay(100)
                            lifebookScrollState.animateScrollTo(lifebookScrollState.maxValue)
                        }
                    }
                }

                // Age Up Button with Gradient Glow - Moved further down
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 27.dp)
                ) {
                    // Gradient Glow Background
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .graphicsLayer(scaleY = glowAnimation, scaleX = glowAnimation)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    listOf(
                                        currentTheme.primary.copy(alpha = 0.3f),
                                        currentTheme.primary.copy(alpha = 0.1f),
                                        androidx.compose.ui.graphics.Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    // Actual Button
                    IconButton(
                        onClick = {
                            if (!isCooldown) {
                                gameViewModel.ageUp()
                            }
                        },
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(currentTheme.primary)
                            .border(2.dp, currentTheme.accent, CircleShape),
                        enabled = !isCooldown,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = currentTheme.text
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_hourglass),
                            contentDescription = "Advance Time",
                            modifier = Modifier
                                .size(28.dp)
                                .graphicsLayer(rotationZ = hourglassRotation),
                            tint = androidx.compose.ui.graphics.Color.Unspecified
                        )
                    }
                }
            }

            // DEBUG BUTTONS - ADD THIS SECTION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Debug Tools",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { gameViewModel.debugCharacter() },
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text("Debug Char", fontSize = 10.sp)
                    }

                    Button(
                        onClick = { gameViewModel.testCreateCharacter() },
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text("Test Create", fontSize = 10.sp)
                    }

                    Button(
                        onClick = { gameViewModel.testDirectDatabaseWrite() },
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text("Test DB", fontSize = 10.sp)
                    }

                    Button(
                        onClick = { gameViewModel.showCurrentState() },
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text("Show State", fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }

        // FULL STATS PANEL (always visible - shows ALL stats)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(closedVisibleHeight.dp)
                .offset(y = (-panelRestPosition).dp)
                .background(currentTheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Life Management Header - Tappable to open menu
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(currentTheme.primary)
                        .clickable { isMenuOpen = true }, // <-- TAP TO OPEN FULL MENU
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

                // Stats Content - Always fully visible
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(currentTheme.surface)
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.TopCenter
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

        // POPUP MENU (appears when isMenuOpen = true)
        if (isMenuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.0f)) // <-- NO DIMMED BACKGROUND
                    .clickable { isMenuOpen = false } // <-- TAP BACKGROUND TO CLOSE
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f) // <-- TAKES 70% OF SCREEN HEIGHT
                        .background(currentTheme.surface) // <-- POPUP BACKGROUND
                        .clickable(enabled = false) { } // <-- PREVENT BACKGROUND CLICKS FROM CLOSING
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // HEADER WITH CLOSE ICON BUTTON
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Life Management",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { isMenuOpen = false }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_collapse),
                                    contentDescription = "Close",
                                    tint = currentTheme.text
                                )
                            }
                        }

                        // STAT DISPLAY (same as closed panel but in popup)
                        Card(
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

                        // MENU ITEMS
                        Text(
                            text = "Menu Options:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Life Management Menu - Scrollable List
                        val menuItems = listOf(
                            MenuItemData(R.drawable.ic_business, "Career", "Manage your professional life", currentTheme) { },
                            MenuItemData(R.drawable.ic_people, "Social", "Manage relationships", currentTheme) { },
                            MenuItemData(R.drawable.ic_law, "Criminal Activities", "View criminal activities.", currentTheme) {
                                isMenuOpen = false
                                onNavigateToCrime()
                            },
                            MenuItemData(R.drawable.ic_band, "Pet Management", "Adopt companions", currentTheme) {
                                isMenuOpen = false
                                onNavigateToPets()
                            },
                            MenuItemData(R.drawable.ic_education, "Education", "Manage schooling", currentTheme) { },
                            MenuItemData(R.drawable.ic_relationship, "Relationships", "View connections", currentTheme) { },
                            MenuItemData(R.drawable.ic_health, "Healthcare", "View medical history and seek care", currentTheme) { },
                            MenuItemData(R.drawable.ic_travel, "Travel Log", "View places visited", currentTheme) { },
                            MenuItemData(R.drawable.ic_save, "Save Game", "Manage saves", currentTheme) { },
                            MenuItemData(R.drawable.ic_settings, "Settings", "Game preferences including theme selection", currentTheme) {
                                isMenuOpen = false
                                onNavigateToSettings()
                            }
                        )

                        LazyColumn(
                            modifier = Modifier.weight(1f)
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
            }
        }

        // Event dialog with stat effects
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
}

// Load random events from JSON assets
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

// Generate random life events when aging up
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

// Lifebook Entry Item Composable with smaller text
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
    color: androidx.compose.ui.graphics.Color,
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
        color = androidx.compose.ui.graphics.Color.Transparent
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
                        shape = androidx.compose.foundation.shape.CircleShape
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

            Column(modifier = Modifier.weight(1f)) {
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