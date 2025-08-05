// domain/src/main/java/com/altlifegames/domain/usecase/GetMatureContentStatusUseCase.kt
package com.altlifegames.domain.usecase

import javax.inject.Inject

class GetMatureContentStatusUseCase @Inject constructor() {
    operator fun invoke(): Boolean {
        // Return default value - you can modify this to read from settings
        return false
    }
}