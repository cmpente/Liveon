package com.liveongames.liveon.di

import android.content.Context
import com.liveongames.data.assets.crime.CrimeAssetLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CrimeAssetsModule {

    @Provides
    @Singleton
    fun provideCrimeAssetLoader(
        @ApplicationContext context: Context
    ): CrimeAssetLoader = CrimeAssetLoader(context)
}
