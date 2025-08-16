package com.liveongames.liveon.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.liveongames.liveon.viewmodel.GameViewModel

@Composable
fun PersistentStatsBar(
    vm: GameViewModel,
    onOpenLifeManagement: () -> Unit
) {
    Surface(tonalElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            // TODO: Replace with your existing stats composable(s)
            Text("Life Management", style = MaterialTheme.typography.titleSmall)
            Divider()
            // e.g., Player quick stats, lifebook snippet, money/health/happiness, etc.
            // Keep this lean to avoid covering too much screen height.
        }
    }
}
