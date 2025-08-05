// app/src/main/java/com/altlifegames/altlife/AltLifeApplication.kt
package com.altlifegames.altlife

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AltLifeApplication : Application() {
    // Hilt will automatically generate the necessary components
}