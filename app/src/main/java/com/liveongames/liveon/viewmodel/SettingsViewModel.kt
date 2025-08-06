// app/src/main/java/com/liveongames/liveon/viewmodel/SettingsViewModel.kt
package com.liveongames.liveon.viewmodel

import android.content.SharedPreferences
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
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _selectedThemeIndex = MutableStateFlow(
        sharedPreferences.getInt("selected_theme_index", 0)
    )
    val selectedThemeIndex: StateFlow<Int> = _selectedThemeIndex.asStateFlow()

    private val _matureContentEnabled = MutableStateFlow(
        sharedPreferences.getBoolean("mature_content_enabled", false)
    )
    val matureContentEnabled: StateFlow<Boolean> = _matureContentEnabled.asStateFlow()

    fun selectTheme(index: Int) {
        viewModelScope.launch {
            _selectedThemeIndex.value = index
            sharedPreferences.edit()
                .putInt("selected_theme_index", index)
                .apply()
        }
    }

    fun toggleMatureContent() {
        viewModelScope.launch {
            val newValue = !_matureContentEnabled.value
            _matureContentEnabled.value = newValue
            sharedPreferences.edit()
                .putBoolean("mature_content_enabled", newValue)
                .apply()
        }
    }
}