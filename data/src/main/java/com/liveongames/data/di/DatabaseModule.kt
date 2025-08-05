package com.liveongames.data.di

import android.content.Context
import androidx.room.Room
import com.liveongames.data.db.liveonDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): liveonDatabase {
        return Room.databaseBuilder(
            context,
            liveonDatabase::class.java,
            "liveon_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAssetDao(database: liveonDatabase) = database.assetDao()

    @Provides
    @Singleton
    fun provideCareerDao(database: liveonDatabase) = database.careerDao()

    @Provides
    @Singleton
    fun provideCharacterDao(database: liveonDatabase) = database.characterDao()

    @Provides
    @Singleton
    fun provideCrimeDao(database: liveonDatabase) = database.crimeDao()

    @Provides
    @Singleton
    fun provideEventDao(database: liveonDatabase) = database.eventDao()

    @Provides
    @Singleton
    fun providePetDao(database: liveonDatabase) = database.petDao()

    @Provides
    @Singleton
    fun provideRelationshipDao(database: liveonDatabase) = database.relationshipDao()

    @Provides
    @Singleton
    fun provideSaveSlotDao(database: liveonDatabase) = database.saveSlotDao()

    @Provides
    @Singleton
    fun provideScenarioDao(database: liveonDatabase) = database.scenarioDao()

    @Provides
    @Singleton
    fun provideUnlockedAchievementDao(database: liveonDatabase) = database.unlockedAchievementDao()
}