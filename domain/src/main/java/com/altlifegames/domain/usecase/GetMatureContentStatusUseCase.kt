package com.altlifegames.domain.usecase

import com.altlifegames.domain.repository.SettingsRepository
import javax.inject.Inject
import com.altlifegames.domain.model.Character
import com.altlifegames.domain.model.EventOutcome

class GetMatureContentStatusUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): Boolean {
        return settingsRepository.isMatureContentEnabled()
    }
}