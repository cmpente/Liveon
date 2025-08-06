// app/src/main/java/com/liveongames/liveon/ui/LiveonGameScreen.kt
package com.liveongames.liveon.ui

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.viewmodel.GameViewModel
import com.liveongames.domain.model.GameEvent
import kotlinx.coroutines.delay
import org.json.JSONObject
import kotlin.random.Random

// Theme definitions
data class LiveonTheme(
    val name: String,
    val background: Color,
    val surface: Color,
    val primary: Color,
    val accent: Color,
    val secondary: Color,
    val text: Color,
    val surfaceVariant: Color
)

val PremiumSleek = LiveonTheme(
    name = "Premium & Sleek",
    background = Color(0xFF151926), // midnight navy
    surface = Color(0xFF232C3A),    // deep indigo
    primary = Color(0xFFFFD700),     // gold
    accent = Color(0xFF00B4D8),     // crystal blue
    secondary = Color(0xFF43A047),  // bold green
    text = Color(0xFFF1F5F9),      // very light gray-white
    surfaceVariant = Color(0xFF2D3A4A)
)

val MinimalEnergetic = LiveonTheme(
    name = "Minimal & Energetic",
    background = Color(0xFFF5F6FA), // almost white
    surface = Color(0xFFE2E8F0),    // gentle cool gray
    primary = Color(0xFFFF6F00),    // amber orange
    accent = Color(0xFF009688),    // modern teal
    secondary = Color(0xFF546E7A), // slate blue-gray
    text = Color(0xFF22314A),       // deep slate
    surfaceVariant = Color(0xFFD1DAE8)
)

val CalmHorizon = LiveonTheme(
    name = "Calm Horizon",
    background = Color(0xFFF4F8FB), // sky mist
    surface = Color(0xFFEAF2F7),    // cloud blue
    primary = Color(0xFF0EA5E9),    // vibrant sky blue
    accent = Color(0xFFFFB300),     // soft gold
    secondary = Color(0xFF00897B),  // teal green
    text = Color(0xFF23304A),       // slate navy
    surfaceVariant = Color(0xFFD2E4EC)
)

val GreenfieldSerenity = LiveonTheme(
    name = "Greenfield Serenity",
    background = Color(0xFFF3F9F5), // minty off-white
    surface = Color(0xFFE6F2EA),    // pale mint
    primary = Color(0xFF43A047),    // forest green
    accent = Color(0xFFF9BE00),     // mellow yellow
    secondary = Color(0xFF2196F3),  // lively blue
    text = Color(0xFF1C2B36),       // charcoal blue-black
    surfaceVariant = Color(0xFFD8EEDC)
)

val SoftSunset = LiveonTheme(
    name = "Soft Sunset",
    background = Color(0xFFFFFAF3), // warm sand
    surface = Color(0xFFFFF3E7),    // peach cream
    primary = Color(0xFFF97316),    // sunset orange
    accent = Color(0xFF38BDF8),     // bright sky blue
    secondary = Color(0xFF10B981),  // minty green
    text = Color(0xFF373737),       // deep graphite
    surfaceVariant = Color(0xFFFFEDD5)
)

val NeutralPremium = LiveonTheme(
    name = "Neutral Premium",
    background = Color(0xFFF5F6FA), // soft light gray
    surface = Color(0xFFE2E8F0),    // gentle cool gray
    primary = Color(0xFF6750A4),    // muted indigo
    accent = Color(0xFF00B4D8),     // crystal blue
    secondary = Color(0xFF26A69A),  // calm teal
    text = Color(0xFF22223B),       // deep navy-black
    surfaceVariant = Color(0xFFE8EAF6)
)

val TwilightForest = LiveonTheme(
    name = "Twilight Forest",
    background = Color(0xFF23272E), // charcoal blue
    surface = Color(0xFF2D333B),    // deep blue-gray
    primary = Color(0xFF7BD389),    // fresh leaf green
    accent = Color(0xFFF9E066),     // pale golden yellow
    secondary = Color(0xFF296748),  // deep forest
    text = Color(0xFFF3F9F6),       // off-white
    surfaceVariant = Color(0xFF3E4854)
)

val BreezeBlossom = LiveonTheme(
    name = "Breeze Blossom",
    background = Color(0xFFFCF8FF), // white-lavender
    surface = Color(0xFFF1F2F6),    // icy blue-gray
    primary = Color(0xFF9B5DE5),    // vivid purple
    accent = Color(0xFF00BBF9),     // electric blue
    secondary = Color(0xFFF15BB5),  // bright pink
    text = Color(0xFF2E294E),       // plum-navy
    surfaceVariant = Color(0xFFE5D1F7)
)

// Theme list for selection
val AllThemes = listOf(
    PremiumSleek,
    MinimalEnergetic,
    CalmHorizon,
    GreenfieldSerenity,
    SoftSunset,
    NeutralPremium,
    TwilightForest,
    BreezeBlossom
)

@Composable
fun LiveonGameScreen(viewModel: GameViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val uiState by viewModel.uiState.collectAsState()

    // Theme selection state (stored in ViewModel or saved state in real implementation)
    var currentThemeIndex by remember { mutableStateOf(0) }
    val currentTheme = AllThemes[currentThemeIndex]

    // Calculate panel heights - Optimized for visibility
    val headerHeight = 48f
    val statsHeight = 100f
    val menuHeight = 340f
    val totalPanelHeight = headerHeight + statsHeight + menuHeight
    val stopPosition = 70f  // Stop position (button height + spacing)

    // Draggable Life Management/Stats state
    var panelOffset by remember { mutableStateOf(0f) }

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

            // Lifebook Section with Age Up Button Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 35.dp),
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
                                text = "Lifebook",  // Removed number of entries
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

                // Age Up Button with Gradient Glow
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 17.dp)
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
                                viewModel.ageUp()
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

            Spacer(modifier = Modifier.height(52.dp))
        }

        // Combined Life Management/Stats Panel (slides up from bottom)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .offset {
                    // Calculate offset so panel stops correctly
                    val maxOffset = (totalPanelHeight - stopPosition) * density.density
                    val currentOffset = maxOffset - (panelOffset * density.density)
                    androidx.compose.ui.unit.IntOffset(0, currentOffset.toInt())
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            // Slow movement - divide by 3 for controlled response
                            val slowDrag = dragAmount.y / 3f
                            val maxVisibleHeight = totalPanelHeight - stopPosition
                            panelOffset = (panelOffset - slowDrag)
                                .coerceIn(0f, maxVisibleHeight)
                        },
                        onDragEnd = { }
                    )
                }
        ) {
            // Combined Panel Content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalPanelHeight.dp), // Full height, position controlled by offset
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp
                ),
                colors = CardDefaults.cardColors(containerColor = currentTheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Life Management Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(headerHeight.dp)
                            .background(currentTheme.primary),
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

                    // Stats Section - Always visible when panel is partially up
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(statsHeight.dp)
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

                    // Life Management Menu - Fits perfectly without scrolling
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(menuHeight.dp)
                            .padding(top = 12.dp, start = 10.dp, end = 10.dp, bottom = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ) {
                            val menuItems = listOf(
                                MenuItemData(R.drawable.ic_business, "Career", "Manage your professional life", currentTheme) { },
                                MenuItemData(R.drawable.ic_people, "Social", "Manage relationships", currentTheme) { },
                                MenuItemData(R.drawable.ic_law, "Crime Records", "View criminal history", currentTheme) { },
                                MenuItemData(R.drawable.ic_band, "Pet Management", "Adopt companions", currentTheme) { },
                                MenuItemData(R.drawable.ic_education, "Education", "Manage schooling", currentTheme) { },
                                MenuItemData(R.drawable.ic_relationship, "Relationships", "View connections", currentTheme) { },
                                MenuItemData(R.drawable.ic_health, "Health Records", "View medical history", currentTheme) { },
                                MenuItemData(R.drawable.ic_travel, "Travel Log", "View places visited", currentTheme) { },
                                MenuItemData(R.drawable.ic_save, "Save Game", "Manage saves", currentTheme) { },
                                MenuItemData(R.drawable.ic_settings, "Settings", "Game preferences including theme selection", currentTheme) {
                                    // Theme selection happens in Settings
                                }
                            )

                            menuItems.forEachIndexed { index, item ->
                                MenuItemRow(item = item)
                                if (index < menuItems.size - 1) {
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
        }

        // Event dialog
        uiState.activeEvents
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
                        viewModel.makeChoice(firstEvent.id, choiceId)
                    },
                    onDismiss = {
                        viewModel.dismissEvent(firstEvent.id)
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

// Lifebook Entry Item Composable
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