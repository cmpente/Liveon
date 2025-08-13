package com.liveongames.liveon.ui.screens.education

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Generic bottom sheet that reads your action's first dialog step and lists its choices.
 * Works without changing your ViewModel. It expects the model fields:
 *   action.title : String
 *   action.dialog[0].text : String
 *   action.dialog[0].choices : List<{ id:String, label:String }>
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionChoiceSheet(
    action: Any,
    onDismiss: () -> Unit,
    onChoose: (choiceId: String) -> Unit
) {
    val cs = MaterialTheme.colorScheme

    val (title, prompt, choices) = remember(action) { extractDialogBits(action) }

    // If your Material3 version doesn't support containerColor, remove the param (defaults will be used).
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = cs.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                title.ifBlank { "Action" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = cs.onSurface
            )
            if (prompt.isNotBlank()) {
                Text(prompt, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            }

            Spacer(Modifier.height(8.dp))

            if (choices.isEmpty()) {
                // No choices found — fall back to a single default branch
                Button(
                    onClick = { onChoose("default_choice") },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Do it") }
            } else {
                choices.forEach { (id, label) ->
                    Button(
                        onClick = { onChoose(id) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(label) }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * Safely extract dialog title, prompt and choices from any action model with your JSON shape.
 * Fixes "Not enough information to infer type variable T" by using explicit List<Any?> types.
 */
private fun extractDialogBits(action: Any): Triple<String, String, List<Pair<String, String>>> {
    return try {
        val k = action::class

        // title
        val title = (k.members.firstOrNull { it.name == "title" }?.call(action) as? String).orElseEmpty()

        // dialog list
        val dialogAny = k.members.firstOrNull { it.name == "dialog" }?.call(action)
        val dialogList: List<Any?> = when (dialogAny) {
            is List<*> -> dialogAny as List<Any?>
            else -> emptyList<Any?>()
        }

        val firstStep: Any? = dialogList.firstOrNull()
        val stepK = firstStep?.let { it::class }

        // prompt text
        val prompt = (stepK
            ?.members
            ?.firstOrNull { it.name == "text" }
            ?.call(firstStep) as? String
                ).orElseEmpty()

        // choices
        val choicesAny = stepK
            ?.members
            ?.firstOrNull { it.name == "choices" }
            ?.call(firstStep)

        val rawChoices: List<Any?> = when (choicesAny) {
            is List<*> -> choicesAny as List<Any?>
            else -> emptyList<Any?>()
        }

        val pairs: List<Pair<String, String>> = rawChoices.mapNotNull { ch ->
            val ck = ch?.let { it::class }
            val id = ck?.members?.firstOrNull { it.name == "id" }?.call(ch) as? String
            val label = ck?.members?.firstOrNull { it.name == "label" }?.call(ch) as? String
            if (id != null && label != null) id to label else null
        }

        Triple(title, prompt, pairs)
    } catch (_: Throwable) {
        Triple("", "", emptyList())
    }
}

private fun String?.orElseEmpty(): String = this ?: ""
