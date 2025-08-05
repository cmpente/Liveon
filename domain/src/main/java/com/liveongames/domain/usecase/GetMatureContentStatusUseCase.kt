// domain/src/main/java/com/liveongames/domain/usecase/GetMatureContentStatusUseCase.kt
package com.liveongames.domain.usecase

import javax.inject.Inject

class GetMatureContentStatusUseCase @Inject constructor() {
    operator fun invoke(): Boolean {
        // Return default value - you can modify this to read from settings
        return false
    }
}