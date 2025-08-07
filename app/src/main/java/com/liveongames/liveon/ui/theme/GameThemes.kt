// app/src/main/java/com/liveongames/liveon/ui/theme/GameThemes.kt
package com.liveongames.liveon.ui.theme

import androidx.compose.ui.graphics.Color

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
    background = Color(0xFF21110D),        // dark iron
    surface = Color(0xFF39241B),           // deep bronze
    primary = Color(0xFFD7263D),           // vibrant crimson
    accent = Color(0xFF28AFB0),            // patina teal
    secondary = Color(0xFFF46036),         // molten orange
    text = Color(0xFFF5E9E2),              // antique white
    surfaceVariant = Color(0xFF2B1911)     // scorched brown
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
val AllGameThemes = listOf(
    PremiumSleek,
    MinimalEnergetic,
    CalmHorizon,
    GreenfieldSerenity,
    SoftSunset,
    NeutralPremium,
    TwilightForest,
    BreezeBlossom
)