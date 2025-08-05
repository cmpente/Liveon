package com.liveongames.liveon.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    // Keep minimal for now
    /*
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): Any {
        return Any() // Placeholder
    }
    */
}