// app/src/main/java/com/liveongames/liveon/LiveOnApplication.kt
package com.liveongames.liveon

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LiveOnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}