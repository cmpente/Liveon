@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.liveongames.liveon.character

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.LocalChromeInsets
import java.util.Locale

@Composable
fun CharacterCreationScreen(
    onDismiss: () -> Unit,
    onComplete: (NewLifePayload) -> Unit,
    vm: CharacterCreationViewModel = hiltViewModel()
) {
    val step by vm.step.collectAsStateWithLifecycle()
    val lists by vm.lists.collectAsStateWithLifecycle()
    val sel by vm.sel.collectAsStateWithLifecycle()
    val err by vm.error.collectAsStateWithLifecycle()
    val insets = LocalChromeInsets.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "New Life",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onDismiss) {
                        Icon(painter = painterResource(R.drawable.ic_close), contentDescription = "Close")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 14.dp)
                .padding(bottom = insets.bottom)
        ) {
            // step progress
            val stepIndex = when (step) {
                CreateStep.IDENTITY -> 0
                CreateStep.BACKGROUND -> 1
                CreateStep.TRAITS -> 2
                CreateStep.APPEARANCE -> 3
                CreateStep.STATS -> 4
                CreateStep.REVIEW -> 5
            }
            LinearProgressIndicator(
                progress = (stepIndex + 1) / 6f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(10.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    fadeIn(animationSpec = tween(180)) togetherWith
                            fadeOut(animationSpec = tween(140))
                },
                label = "cc-steps"
            ) { s ->
                when (s) {
                    CreateStep.IDENTITY -> StepIdentity(
                        lists = lists,
                        sel = sel,
                        onName = vm::setName,
                        onPronouns = vm::setPronouns,
                        onBirthplace = vm::setBirthplace,
                        onRandom = vm::randomizeAll
                    )
                    CreateStep.BACKGROUND -> StepBackground(
                        list = lists.backgrounds,
                        selectedId = sel.backgroundId,
                        onPick = vm::selectBackground
                    )
                    CreateStep.TRAITS -> StepTraits(
                        pack = lists.traitsPack,
                        selected = sel.traitIds,
                        onToggle = vm::toggleTrait
                    )
                    CreateStep.APPEARANCE -> StepAppearance(
                        presets = lists.appearance,
                        value = sel.appearance,
                        onChange = vm::setAppearance
                    )
                    CreateStep.STATS -> StepStats(
                        pool = sel.pointsPool,
                        stats = sel.stats,
                        onSet = vm::setStat
                    )
                    CreateStep.REVIEW -> StepReview(lists = lists, sel = sel)
                }
            }

            if (!err.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(err!!, color = Color(0xFFE53935), style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = vm::back,
                        enabled = step != CreateStep.IDENTITY,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Back") }
                }
                Box(Modifier.weight(1f)) {
                    if (step != CreateStep.REVIEW) {
                        Button(
                            onClick = vm::next,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Next") }
                    } else {
                        Button(
                            onClick = { vm.finalizeCreate()?.let(onComplete) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Begin Life") }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/* ---------------- Steps ---------------- */

@Composable
private fun StepIdentity(
    lists: UiLists,
    sel: Selections,
    onName: (String, String) -> Unit,
    onPronouns: (Pronouns) -> Unit,
    onBirthplace: (LocationRef) -> Unit,
    onRandom: () -> Unit
) {
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Identity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onRandom) { Text("Randomize") }
        }
        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = sel.identity.firstName,
                onValueChange = { onName(it, sel.identity.lastName) },
                label = { Text("First name") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = sel.identity.lastName,
                onValueChange = { onName(sel.identity.firstName, it) },
                label = { Text("Last name") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))

        Text("Pronouns", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Pronouns.HE_HIM, Pronouns.SHE_HER, Pronouns.THEY_THEM).forEach { p ->
                ChoiceChip(
                    selected = sel.identity.pronouns == p,
                    label = when (p) {
                        Pronouns.HE_HIM -> "He/Him"
                        Pronouns.SHE_HER -> "She/Her"
                        Pronouns.THEY_THEM -> "They/Them"
                    },
                    onClick = { onPronouns(p) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Birthplace", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))

        val countries = lists.countries
        var pickedCode by rememberSaveable {
            mutableStateOf(
                sel.identity.birthplace?.countryCode ?: countries.firstOrNull()?.code ?: ""
            )
        }
        val country = countries.firstOrNull { it.code == pickedCode } ?: countries.firstOrNull()
        val cities = country?.cities.orEmpty()
        var pickedCity by rememberSaveable {
            mutableStateOf(
                sel.identity.birthplace?.cityId ?: cities.firstOrNull()?.id ?: ""
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DropdownCard(
                label = "Country",
                value = country?.name ?: "—",
                items = countries.map { it.name },
                onItem = { idx ->
                    val c = countries[idx]
                    pickedCode = c.code
                    val firstCity = c.cities.firstOrNull()
                    pickedCity = firstCity?.id ?: ""
                    firstCity?.let { onBirthplace(LocationRef(c.code, it.id)) }
                },
                modifier = Modifier.weight(1f)
            )
            DropdownCard(
                label = "City",
                value = cities.firstOrNull { it.id == pickedCity }?.name
                    ?: (cities.firstOrNull()?.name ?: "—"),
                items = cities.map { it.name },
                onItem = { idx ->
                    val cc = cities[idx]
                    pickedCity = cc.id
                    country?.let { onBirthplace(LocationRef(it.code, cc.id)) }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StepBackground(
    list: List<BackgroundDef>,
    selectedId: String?,
    onPick: (String) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text("Background", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.weight(1f)) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(list) { bg ->
                    val sel = selectedId == bg.id
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(bg.id) },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(bg.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                if (sel) SelectedPill()
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(bg.desc, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatDelta("Health", bg.statMods.health)
                                StatDelta("Happy", bg.statMods.happiness)
                                StatDelta("Int", bg.statMods.intelligence)
                                StatDelta("Social", bg.statMods.social)
                                MoneyDelta(bg.statMods.money)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepTraits(
    pack: TraitPack,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Traits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            Text("Choose up to ${pack.limits.maxSelected}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.weight(1f)) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(pack.traits) { t ->
                    val isSel = t.id in selected
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(t.id) },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(t.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                if (isSel) SelectedPill()
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(t.desc, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                t.effects
                                    .filter { it.type == "stat_boost" && it.amount != null }
                                    .forEach {
                                        StatDelta(
                                            it.key.replaceFirstChar { c -> c.titlecase(Locale.getDefault()) },
                                            it.amount!!
                                        )
                                    }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepAppearance(
    presets: AppearancePresets,
    value: Appearance,
    onChange: (Appearance) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        Text("Skin Tone", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.palettes.skinTones.forEachIndexed { idx, hex ->
                val col = Color(android.graphics.Color.parseColor(hex))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(col)
                        .clickable { onChange(value.copy(skinToneIndex = idx)) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownCard(
                label = "Hair Style",
                value = presets.hairStyles.firstOrNull { it.id == value.hairStyleId }?.name
                    ?: presets.hairStyles.firstOrNull()?.name.orEmpty(),
                items = presets.hairStyles.map { it.name },
                onItem = { i -> presets.hairStyles.getOrNull(i)?.let { onChange(value.copy(hairStyleId = it.id)) } },
                modifier = Modifier.weight(1f)
            )
            DropdownCard(
                label = "Hair Color",
                value = value.hairColor,
                items = presets.hairColors,
                onItem = { i -> presets.hairColors.getOrNull(i)?.let { onChange(value.copy(hairColor = it)) } },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownCard(
                label = "Eye Color",
                value = value.eyeColor,
                items = presets.eyeColors,
                onItem = { i -> presets.eyeColors.getOrNull(i)?.let { onChange(value.copy(eyeColor = it)) } },
                modifier = Modifier.weight(1f)
            )
            DropdownCard(
                label = "Body Type",
                value = value.bodyType,
                items = presets.bodyTypes,
                onItem = { i -> presets.bodyTypes.getOrNull(i)?.let { onChange(value.copy(bodyType = it)) } },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun StepStats(
    pool: Int,
    stats: PlayerStats,
    onSet: (StatKey, Int) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Starting Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Pill("Points left: $pool")
        }
        Spacer(Modifier.height(8.dp))
        StatSliderRow("Health", stats.health) { onSet(StatKey.HEALTH, it) }
        StatSliderRow("Happiness", stats.happiness) { onSet(StatKey.HAPPINESS, it) }
        StatSliderRow("Intelligence", stats.intelligence) { onSet(StatKey.INTELLIGENCE, it) }
        StatSliderRow("Social", stats.social) { onSet(StatKey.SOCIAL, it) }
        Spacer(Modifier.height(6.dp))
        Text("Tip: You can’t go below 0 or above 100. Points can’t be negative.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StepReview(
    lists: UiLists,
    sel: Selections
) {
    val bg = lists.backgrounds.firstOrNull { it.id == sel.backgroundId }
    Column(Modifier.fillMaxSize()) {
        Text("Review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("${sel.identity.firstName} ${sel.identity.lastName}", fontWeight = FontWeight.SemiBold)
                Text("${sel.identity.pronouns}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text("Background: ${bg?.name ?: "—"}")
                Text("Traits: ${sel.traitIds.joinToString().ifBlank { "None" }}")
                Spacer(Modifier.height(6.dp))
                Text("Appearance: ${sel.appearance.bodyType}, ${sel.appearance.hairColor} hair, ${sel.appearance.eyeColor} eyes")
                Spacer(Modifier.height(6.dp))
                Divider()
                Spacer(Modifier.height(6.dp))
                Text("Starting Stats")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Pill("Health ${sel.stats.health}")
                    Pill("Happy ${sel.stats.happiness}")
                    Pill("Int ${sel.stats.intelligence}")
                    Pill("Social ${sel.stats.social}")
                    Pill("$${sel.stats.money}")
                }
            }
        }
    }
}

/* ---------------- Small UI helpers ---------------- */

@Composable private fun SelectedPill() {
    Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp)) {
        Text("Selected", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}
@Composable private fun Pill(text: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}
@Composable private fun StatDelta(label: String, delta: Int) {
    val c = when {
        delta > 0 -> Color(0xFF2E7D32)
        delta < 0 -> Color(0xFFE53935)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = c.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)) {
        Text("$label ${if (delta >= 0) "+$delta" else "$delta"}", color = c, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}
@Composable private fun MoneyDelta(delta: Int) {
    Surface(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)) {
        Text("$$delta", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}
@Composable private fun ChoiceChip(selected: Boolean, label: String, onClick: () -> Unit) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Composable
private fun DropdownCard(
    label: String,
    value: String,
    items: List<String>,
    onItem: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(10.dp)) {
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(2.dp))
            Text(value, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items.take(3).forEachIndexed { idx, s ->
                    OutlinedButton(onClick = { onItem(idx) }, shape = RoundedCornerShape(12.dp)) {
                        Text(s.take(8))
                    }
                }
                if (items.size > 3) Text("…", modifier = Modifier.align(Alignment.CenterVertically))
            }
        }
    }
}

@Composable private fun StatSliderRow(
    label: String,
    value: Int,
    onChange: (Int) -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    value.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = 0f..100f,
            steps = 99
        )
    }
}