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
    val surfaceVariant: Color,
    val surfaceElevated: Color
)

val PremiumSleek = LiveonTheme(
    name = "Premium & Sleek",
    background = Color(0xFF18100E),
    surface = Color(0xFF261913),
    surfaceElevated = Color(0xFF2E1F18),
    primary = Color(0xFFC41F33),
    accent = Color(0xFF1F8E8F),
    secondary = Color(0xFFD95A2E),
    text = Color(0xFFEFE7E2),
    surfaceVariant = Color(0xFF1E130F)
)

val MinimalEnergetic = LiveonTheme(
    name = "Minimal & Energetic",
    background = Color(0xFF0E0F12),
    surface = Color(0xFF151A22),
    surfaceElevated = Color(0xFF1A202B),
    primary = Color(0xFFFF7A00),
    accent = Color(0xFF009B8F),
    secondary = Color(0xFF5B7382),
    text = Color(0xFFE6ECF4),
    surfaceVariant = Color(0xFF1D2430)
)

val CalmHorizon = LiveonTheme(
    name = "Calm Horizon",
    background = Color(0xFF0B1220),
    surface = Color(0xFF121A2A),
    surfaceElevated = Color(0xFF1A253A),
    primary = Color(0xFF2FB4F3),
    accent = Color(0xFFFFC857),
    secondary = Color(0xFF1AA68F),
    text = Color(0xFFEAF3FF),
    surfaceVariant = Color(0xFF172235)
)

val GreenfieldSerenity = LiveonTheme(
    name = "Greenfield Serenity",
    background = Color(0xFF0C1611),
    surface = Color(0xFF102018),
    surfaceElevated = Color(0xFF15281F),
    primary = Color(0xFF5DBB63),
    accent = Color(0xFFE1B12C),
    secondary = Color(0xFF2E86DE),
    text = Color(0xFFE8F5EE),
    surfaceVariant = Color(0xFF12261D)
)

val SoftSunset = LiveonTheme(
    name = "Soft Sunset",
    background = Color(0xFF1A1410),
    surface = Color(0xFF241A14),
    surfaceElevated = Color(0xFF2C221B),
    primary = Color(0xFFF97316),
    accent = Color(0xFF42C6F5),
    secondary = Color(0xFF2DD4BF),
    text = Color(0xFFF3EAE2),
    surfaceVariant = Color(0xFF2B2019)
)

val NeutralPremium = LiveonTheme(
    name = "Neutral Premium",
    background = Color(0xFF121214),
    surface = Color(0xFF1A1B20),
    surfaceElevated = Color(0xFF22242B),
    primary = Color(0xFF8E83D6),
    accent = Color(0xFF00A3C7),
    secondary = Color(0xFF26A69A),
    text = Color(0xFFE8EAF6),
    surfaceVariant = Color(0xFF24262F)
)

val TwilightForest = LiveonTheme(
    name = "Twilight Forest",
    background = Color(0xFF121619),
    surface = Color(0xFF182024),
    surfaceElevated = Color(0xFF1F2A30),
    primary = Color(0xFF7BD389),
    accent = Color(0xFFE8C66A),
    secondary = Color(0xFF1F4D3A),
    text = Color(0xFFE7F4ED),
    surfaceVariant = Color(0xFF22303A)
)

val BreezeBlossom = LiveonTheme(
    name = "Breeze Blossom",
    background = Color(0xFF13101A),
    surface = Color(0xFF1A1524),
    surfaceElevated = Color(0xFF221C2F),
    primary = Color(0xFFB087F6),
    accent = Color(0xFF00D3FF),
    secondary = Color(0xFFFF6BB5),
    text = Color(0xFFEEE7FF),
    surfaceVariant = Color(0xFF231B33)
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
