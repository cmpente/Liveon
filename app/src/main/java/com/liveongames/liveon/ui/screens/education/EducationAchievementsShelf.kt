package com.liveongames.liveon.ui.screens.education

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.liveongames.domain.model.Education
import com.liveongames.liveon.R
import com.liveongames.liveon.ui.theme.LiveonTheme

@Composable
fun EducationAchievementsShelf(
    certificates: List<Education>,
    theme: LiveonTheme,
    onCertificateSelected: (Education) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Achievements & Certificates",
            style = MaterialTheme.typography.headlineSmall,
            color = theme.text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (certificates.isEmpty()) {
            Text(
                text = "No certificates earned yet",
                style = MaterialTheme.typography.bodyMedium,
                color = theme.accent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                textAlign = TextAlign.Center
            )
            return
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(certificates) { certificate ->
                CertificateCard(
                    certificate = certificate,
                    theme = theme,
                    onSelected = { onCertificateSelected(certificate) }
                )
            }
        }
    }
}

@Composable
private fun CertificateCard(
    certificate: Education,
    theme: LiveonTheme,
    onSelected: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "certificateScale"
    )

    Card(
        modifier = Modifier
            .width(140.dp)
            .height(180.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable { pressed = true; onSelected() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_diploma),
                contentDescription = null,
                tint = theme.primary,
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = certificate.name,
                style = MaterialTheme.typography.bodyMedium,
                color = theme.text,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                modifier = Modifier.padding(top = 8.dp)
            )

            val dateStr = certificate.completionDate?.let {
                java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault()).format(it)
            } ?: "—"

            Spacer(Modifier.height(8.dp))

            Text("Completed: $dateStr", style = MaterialTheme.typography.bodySmall, color = theme.accent)
            Text("GPA: ${"%.2f".format(certificate.currentGPA)}", style = MaterialTheme.typography.bodySmall, color = theme.accent)
        }
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(120)
            pressed = false
        }
    }
}
