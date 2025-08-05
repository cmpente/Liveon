// app/src/main/java/com/liveongames/liveon/viewmodel/SettingsViewModel.kt
package com.liveongames.liveon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    // Add actual repositories when they become available
) : ViewModel() {

    private val _matureContentEnabled = MutableStateFlow(false)
    val matureContentEnabled: StateFlow<Boolean> = _matureContentEnabled.asStateFlow()

    fun toggleMatureContent() {
        viewModelScope.launch {
            _matureContentEnabled.value = !_matureContentEnabled.value
        }
    }
}