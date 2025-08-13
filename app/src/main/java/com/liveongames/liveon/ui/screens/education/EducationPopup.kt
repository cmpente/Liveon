package com.liveongames.liveon.ui.screens.education

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.liveongames.liveon.viewmodel.EducationViewModel

@Composable
fun EducationPopup(
    onDismiss: () -> Unit,
    viewModel: EducationViewModel = hiltViewModel()
) {
    EducationSheet(onDismiss = onDismiss, viewModel = viewModel)
}
