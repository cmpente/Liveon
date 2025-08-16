@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.liveongames.liveon.character

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liveongames.liveon.R
import kotlin.math.roundToInt
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New Life", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onDismiss) {
                        Icon(painter = painterResource(R.drawable.ic_close), contentDescription = "Close")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            StepProgress(stepIndex = step.ordinal, steps = CreateStep.values().size)
            Spacer(Modifier.height(10.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(140))
                },
                label = "cc-steps"
            ) { s ->
                when (s) {
                    CreateStep.IDENTITY -> StepIdentity(lists, sel, vm)
                    CreateStep.APPEARANCE -> StepAppearance(lists, sel, vm)
                    CreateStep.NATIONALITY -> StepNationality(lists, sel, vm)
                    CreateStep.TRAITS -> StepTraits(lists, sel, vm)
                    CreateStep.STATS -> StepStats(sel, vm)
                    CreateStep.REVIEW -> StepReview(lists, sel, vm::buildSummary)
                }
            }

            if (!err.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(err!!, color = Color(0xFFE53935), style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = vm::back,
                    enabled = step != CreateStep.IDENTITY,
                    modifier = Modifier.weight(1f)
                ) { Text("Back") }

                if (step != CreateStep.REVIEW) {
                    Button(
                        onClick = vm::next,
                        modifier = Modifier.weight(1f)
                    ) { Text("Next") }
                } else {
                    Button(
                        onClick = { vm.finalizeCreate()?.let(onComplete) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Begin Life") }
                }
            }

            if (step == CreateStep.IDENTITY) {
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = vm::randomizeIdentity, modifier = Modifier.weight(1f)) {
                        Text("Randomize Name")
                    }
                    Button(onClick = vm::quickStart, modifier = Modifier.weight(1f)) {
                        Text("Quick Start")
                    }
                }
            }
        }
    }
}

/* ---------- Steps ---------- */

@Composable
private fun StepIdentity(lists: UiLists, sel: Selections, vm: CharacterCreationViewModel) {
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
        Text("Name & Gender", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = sel.identity.firstName,
                onValueChange = { vm.setName(it, sel.identity.lastName) },
                label = { Text("First name") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = sel.identity.lastName,
                onValueChange = { vm.setName(sel.identity.firstName, it) },
                label = { Text("Last name") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text("Gender", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(6.dp))
        FlowRow {
            Gender.values().forEach { g ->
                ChoiceChip(
                    selected = sel.identity.gender == g,
                    label = g.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) },
                    onClick = { vm.setGender(g) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Pronouns", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(6.dp))
        FlowRow {
            listOf(Pronouns.HE_HIM, Pronouns.SHE_HER, Pronouns.THEY_THEM).forEach { p ->
                ChoiceChip(
                    selected = sel.identity.pronouns == p,
                    label = when (p) {
                        Pronouns.HE_HIM -> "He/Him"
                        Pronouns.SHE_HER -> "She/Her"
                        Pronouns.THEY_THEM -> "They/Them"
                    },
                    onClick = { vm.setPronouns(p) }
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun StepAppearance(lists: UiLists, sel: Selections, vm: CharacterCreationViewModel) {
    Column(Modifier.fillMaxSize()) {
        Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        GeneratedPortrait(
            skinHex = lists.appearance.palettes.skinTones.getOrElse(sel.appearance.skinToneIndex) { "#E3B294" },
            hairStyleId = sel.appearance.hairStyleId,
            hairColor = sel.appearance.hairColor,
            eyeColor = sel.appearance.eyeColor,
            features = sel.notableFeatures
        )

        Spacer(Modifier.height(12.dp))
        Text("Skin Tone", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            lists.appearance.palettes.skinTones.forEachIndexed { idx, hex ->
                val col = Color(android.graphics.Color.parseColor(hex))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(col)
                        .clickable { vm.setAppearance(sel.appearance.copy(skinToneIndex = idx)) }
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownCard(
                label = "Hair Style",
                value = lists.appearance.hairStyles.firstOrNull { it.id == sel.appearance.hairStyleId }?.name
                    ?: lists.appearance.hairStyles.firstOrNull()?.name.orEmpty(),
                items = lists.appearance.hairStyles.map { it.name },
                onItem = { i -> lists.appearance.hairStyles.getOrNull(i)?.let { vm.setAppearance(sel.appearance.copy(hairStyleId = it.id)) } },
                modifier = Modifier.weight(1f)
            )
            DropdownCard(
                label = "Hair Color",
                value = sel.appearance.hairColor,
                items = lists.appearance.hairColors,
                onItem = { i -> lists.appearance.hairColors.getOrNull(i)?.let { vm.setAppearance(sel.appearance.copy(hairColor = it)) } },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownCard(
                label = "Eye Color",
                value = sel.appearance.eyeColor,
                items = lists.appearance.eyeColors,
                onItem = { i -> lists.appearance.eyeColors.getOrNull(i)?.let { vm.setAppearance(sel.appearance.copy(eyeColor = it)) } },
                modifier = Modifier.weight(1f)
            )
            DropdownCard(
                label = "Build",
                value = sel.appearance.bodyType,
                items = lists.appearance.bodyTypes,
                onItem = { i -> lists.appearance.bodyTypes.getOrNull(i)?.let { vm.setAppearance(sel.appearance.copy(bodyType = it)) } },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))
        Text("Height: ${sel.heightCm} cm", style = MaterialTheme.typography.titleSmall)
        Slider(
            value = sel.heightCm.toFloat(),
            onValueChange = { vm.setHeight(it.roundToInt()) },
            valueRange = 120f..210f,
            steps = 90
        )

        Spacer(Modifier.height(8.dp))
        Text("Notable Features", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(6.dp))
        FlowRow {
            listOf("Scar","Tattoo","Freckles","Piercing","Birthmark").forEach { tag ->
                ChoiceChip(
                    selected = tag in sel.notableFeatures,
                    label = tag,
                    onClick = { vm.toggleFeature(tag) }
                )
            }
        }
    }
}

@Composable
private fun StepNationality(lists: UiLists, sel: Selections, vm: CharacterCreationViewModel) {
    Column(Modifier.fillMaxSize()) {
        Text("Nationality & Ethnicity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            val country = lists.countries.firstOrNull { it.code == sel.countryCode } ?: lists.countries.first()
            DropdownCard(
                label = "Country",
                value = country.name,
                items = lists.countries.map { it.name },
                onItem = { idx -> lists.countries.getOrNull(idx)?.let { vm.setCountry(it.code) } },
                modifier = Modifier.weight(1f)
            )

            val cities = lists.countries.firstOrNull { it.code == sel.countryCode }?.cities ?: country.cities
            val cityName = cities.firstOrNull { it.id == sel.cityId }?.name ?: cities.firstOrNull()?.name.orEmpty()
            DropdownCard(
                label = "City",
                value = cityName,
                items = cities.map { it.name },
                onItem = { idx -> cities.getOrNull(idx)?.let { vm.setCity(it.id) } },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))
        val ethName = lists.ethnicities.firstOrNull { it.id == sel.ethnicityId }?.name
            ?: lists.ethnicities.first().name
        DropdownCard(
            label = "Ethnicity",
            value = ethName,
            items = lists.ethnicities.map { it.name },
            onItem = { idx -> lists.ethnicities.getOrNull(idx)?.let { vm.setEthnicity(it.id) } },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Note: Cultural/Regional events and religion options will unlock more storylines in future updates.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StepTraits(lists: UiLists, sel: Selections, vm: CharacterCreationViewModel) {
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Traits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            Text("Pick up to ${lists.traitsPack.limits.maxSelected}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.weight(1f)) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(lists.traitsPack.traits) { t ->
                    val selected = t.id in sel.traitIds
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.toggleTrait(t.id) },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(t.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                if (selected) SelectedPill()
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(t.desc, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepStats(sel: Selections, vm: CharacterCreationViewModel) {
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Starting Attributes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
                Text("Points left: ${sel.pointsPool}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        StatSliderRow("Intelligence", sel.stats.intelligence) { vm.setStat("intelligence", it) }
        StatSliderRow("Creativity", sel.stats.creativity) { vm.setStat("creativity", it) }
        StatSliderRow("Charisma", sel.stats.charisma) { vm.setStat("charisma", it) }
        StatSliderRow("Athleticism", sel.stats.athleticism) { vm.setStat("athleticism", it) }
        StatSliderRow("Health", sel.stats.health) { vm.setStat("health", it) }
        StatSliderRow("Luck", sel.stats.luck) { vm.setStat("luck", it) }
        StatSliderRow("Sociability", sel.stats.sociability) { vm.setStat("sociability", it) }
        StatSliderRow("Discipline", sel.stats.discipline) { vm.setStat("discipline", it) }
        Spacer(Modifier.height(6.dp))
        Text("Tip: allocate points to shape your early opportunities. You can adjust later via life events.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StepReview(lists: UiLists, sel: Selections, summary: (UiLists, Selections) -> String) {
    Column(Modifier.fillMaxSize()) {
        Text("Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("${sel.identity.firstName} ${sel.identity.lastName}", fontWeight = FontWeight.SemiBold)
                Text(
                    "Pronouns: ${when (sel.identity.pronouns) {
                        Pronouns.HE_HIM -> "He/Him"
                        Pronouns.SHE_HER -> "She/Her"
                        Pronouns.THEY_THEM -> "They/Them"
                    }}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("Country: ${lists.countries.firstOrNull { it.code == sel.countryCode }?.name ?: "—"}")
                    Pill("City: ${lists.countries.firstOrNull { it.code == sel.countryCode }?.cities?.firstOrNull { it.id == sel.cityId }?.name ?: "—"}")
                    Pill("Ethnicity: ${lists.ethnicities.firstOrNull { it.id == sel.ethnicityId }?.name ?: "—"}")
                }
                Spacer(Modifier.height(6.dp))
                Text("Traits: ${sel.traitIds.takeIf { it.isNotEmpty() }?.let { ids ->
                    lists.traitsPack.traits.filter { it.id in ids }.joinToString { it.name }
                } ?: "None"}")
                Spacer(Modifier.height(6.dp))
                Text(
                    "Attributes: Int ${sel.stats.intelligence}, Cr ${sel.stats.creativity}, Ch ${sel.stats.charisma}, " +
                            "Ath ${sel.stats.athleticism}, Hlth ${sel.stats.health}, Luck ${sel.stats.luck}, " +
                            "Soc ${sel.stats.sociability}, Disc ${sel.stats.discipline}"
                )
                Spacer(Modifier.height(10.dp))
                Text(summary(lists, sel), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

/* ---------- UI helpers ---------- */

@Composable private fun StepProgress(stepIndex: Int, steps: Int) {
    LinearProgressIndicator(
        progress = (stepIndex + 1) / steps.toFloat(),
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(8.dp)),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}
@Composable private fun ChoiceChip(selected: Boolean, label: String, onClick: () -> Unit) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(end = 8.dp, bottom = 8.dp)
            .clickable { onClick() }
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Composable private fun FlowRow(content: @Composable RowScope.() -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, content = content)
}
@Composable private fun Pill(text: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}
@Composable private fun SelectedPill() {
    Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp)) {
        Text("Selected", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}
@Composable private fun DropdownCard(
    label: String,
    value: String,
    items: List<String>,
    onItem: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(10.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(2.dp))
            Text(value, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items.take(3).forEachIndexed { idx, s ->
                    OutlinedButton(onClick = { onItem(idx) }, shape = RoundedCornerShape(12.dp)) { Text(s.take(10)) }
                }
                if (items.size > 3) Text("…", modifier = Modifier.align(Alignment.CenterVertically))
            }
        }
    }
}
@Composable private fun StatSliderRow(label: String, value: Int, onChange: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
                Text(value.toString(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
            }
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            valueRange = 0f..100f,
            steps = 99
        )
    }
}

/* ---------- Tag-driven Portrait (simple, stylized) ---------- */

@Composable
private fun GeneratedPortrait(
    skinHex: String,
    hairStyleId: String,
    hairColor: String,
    eyeColor: String,
    features: Set<String>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(140.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant)
) {
    val skin = Color(android.graphics.Color.parseColor(skinHex))
    val hair = when (hairColor) {
        "Black" -> Color(0xFF222222)
        "Brown" -> Color(0xFF5C4033)
        "Blonde" -> Color(0xFFE6C56C)
        "Red" -> Color(0xFFB23A2C)
        "Gray" -> Color(0xFF9E9E9E)
        else -> Color(0xFF5C4033)
    }
    val eye = when (eyeColor) {
        "Blue" -> Color(0xFF4895EF)
        "Green" -> Color(0xFF52B788)
        "Hazel" -> Color(0xFF8C6A43)
        "Gray" -> Color(0xFF90A4AE)
        else -> Color(0xFF5D4037) // Brown
    }

    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawRoundRect(
                color = skin,
                topLeft = androidx.compose.ui.geometry.Offset(w*0.3f, h*0.15f),
                size = androidx.compose.ui.geometry.Size(w*0.4f, h*0.6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w*0.2f, w*0.2f)
            )

            val hairTop = Path().apply {
                moveTo(w*0.28f, h*0.12f)
                quadraticBezierTo(w*0.5f, h*0.0f, w*0.72f, h*0.12f)
                lineTo(w*0.7f, h*0.28f)
                quadraticBezierTo(w*0.5f, h*0.2f, w*0.3f, h*0.28f)
                close()
            }
            drawPath(hairTop, hair)

            if (hairStyleId == "long" || hairStyleId == "curly") {
                drawRoundRect(
                    color = hair,
                    topLeft = androidx.compose.ui.geometry.Offset(w*0.26f, h*0.28f),
                    size = androidx.compose.ui.geometry.Size(w*0.48f, h*0.42f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w*0.22f, w*0.22f),
                    style = Stroke(width = 8f)
                )
            }

            drawCircle(eye, radius = w*0.02f, center = androidx.compose.ui.geometry.Offset(w*0.45f, h*0.4f))
            drawCircle(eye, radius = w*0.02f, center = androidx.compose.ui.geometry.Offset(w*0.55f, h*0.4f))

            if ("Scar" in features) {
                drawLine(Color(0xFF8D6E63), androidx.compose.ui.geometry.Offset(w*0.42f, h*0.35f), androidx.compose.ui.geometry.Offset(w*0.38f, h*0.5f), strokeWidth = 4f)
            }
            if ("Tattoo" in features) {
                drawCircle(Color(0xFF1E88E5), radius = w*0.015f, center = androidx.compose.ui.geometry.Offset(w*0.7f, h*0.62f))
            }
            if ("Freckles" in features) {
                repeat(8) { i ->
                    drawCircle(Color(0xFF6D4C41), radius = w*0.006f, center = androidx.compose.ui.geometry.Offset(w*(0.44f + i*0.01f), h*0.47f))
                }
            }
            if ("Piercing" in features) {
                drawCircle(Color(0xFFBDBDBD), radius = w*0.006f, center = androidx.compose.ui.geometry.Offset(w*0.32f, h*0.48f))
            }
            if ("Birthmark" in features) {
                drawCircle(Color(0xFF6D4C41), radius = w*0.012f, center = androidx.compose.ui.geometry.Offset(w*0.36f, h*0.6f))
            }
        }
    }
}