package com.altlifegames.domain.usecase

import com.altlifegames.domain.repository.SettingsRepository
import javax.inject.Inject
import com.altlifegames.domain.model.Character
import com.altlifegames.domain.model.EventOutcome

/**
 * Toggles the mature content flag.  If currently enabled it will be disabled and vice versa.
 */
class ToggleMatureContentUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(currentlyEnabled: Boolean) {
        settingsRepository.setMatureContentEnabled(!currentlyEnabled)
    }
}