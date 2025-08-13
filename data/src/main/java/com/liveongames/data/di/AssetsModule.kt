package com.liveongames.data.di

import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.liveongames.data.assets.common.RawAssetReader
import com.liveongames.data.assets.achievements.AchievementsAssetLoader
import com.liveongames.data.assets.events.EventsAssetLoader
import com.liveongames.data.assets.scenarios.ScenariosAssetLoader
import com.liveongames.data.assets.education.EducationAssetLoader

@Module
@InstallIn(SingletonComponent::class)
object AssetsModule {

    @Provides
    @Singleton
    fun provideRawAssetReader(@ApplicationContext context: Context): RawAssetReader =
        RawAssetReader(context)

    @Provides
    @Singleton
    fun provideAchievementsAssetLoader(reader: RawAssetReader, gson: Gson): AchievementsAssetLoader =
        AchievementsAssetLoader(reader, gson)

    @Provides
    @Singleton
    fun provideScenariosAssetLoader(reader: RawAssetReader, gson: Gson): ScenariosAssetLoader =
        ScenariosAssetLoader(reader, gson)

    @Provides
    @Singleton
    fun provideEventsAssetLoader(reader: RawAssetReader, gson: Gson): EventsAssetLoader =
        EventsAssetLoader(reader, gson)

    @Provides
    @Singleton
    fun provideEducationAssetLoader(@ApplicationContext context: Context): EducationAssetLoader {
        return EducationAssetLoader(context) // Pass the Context
        }
}