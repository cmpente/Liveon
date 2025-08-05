// domain/src/main/java/com/altlifegames/domain/usecase/ToggleMatureContentUseCase.kt
package com.altlifegames.domain.usecase

import javax.inject.Inject

class ToggleMatureContentUseCase @Inject constructor() {
    operator fun invoke(currentStatus: Boolean): Boolean {
        return !currentStatus
    }
}