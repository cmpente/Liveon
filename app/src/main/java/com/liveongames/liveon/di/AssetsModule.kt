// app/src/main/java/com/liveongames/liveon/di/AssetsModule.kt
package com.liveongames.liveon.di

import android.content.Context
import com.google.gson.Gson
import com.liveongames.liveon.assets.common.RawAssetReader
import com.liveongames.liveon.assets.achievements.AchievementsAssetLoader
import com.liveongames.liveon.assets.education.EducationAssetLoader
import com.liveongames.liveon.assets.events.EventsAssetLoader
import com.liveongames.liveon.assets.scenarios.ScenariosAssetLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AssetsModule {

    @Provides
    @Singleton
    fun provideRawAssetReader(@ApplicationContext context: Context): RawAssetReader =
        RawAssetReader(context)

    // NOTE: We do NOT provide Gson here to avoid duplicate bindings.
    // A single @Singleton Gson is provided by AppModule.

    @Provides
    @Singleton
    fun provideAchievementsAssetLoader(reader: RawAssetReader, gson: Gson): AchievementsAssetLoader =
        AchievementsAssetLoader(reader, gson)

    @Provides
    @Singleton
    fun provideEventsAssetLoader(reader: RawAssetReader, gson: Gson): EventsAssetLoader =
        EventsAssetLoader(reader, gson)

    @Provides
    @Singleton
    fun provideScenariosAssetLoader(reader: RawAssetReader, gson: Gson): ScenariosAssetLoader =
        ScenariosAssetLoader(reader, gson)

    @Provides
    @Singleton
    fun provideEducationAssetLoader(reader: RawAssetReader, gson: Gson): EducationAssetLoader =
        EducationAssetLoader(reader, gson)
}
