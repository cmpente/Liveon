// domain/src/main/java/com/liveongames/domain/usecase/ToggleMatureContentUseCase.kt
package com.liveongames.domain.usecase

import javax.inject.Inject

class ToggleMatureContentUseCase @Inject constructor() {
    operator fun invoke(currentStatus: Boolean): Boolean {
        return !currentStatus
    }
}