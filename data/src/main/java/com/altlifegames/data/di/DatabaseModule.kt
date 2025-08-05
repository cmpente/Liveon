package com.altlifegames.data.di

import android.content.Context
import androidx.room.Room
import com.altlifegames.data.db.AltLifeDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): AltLifeDatabase {
        return Room.databaseBuilder(
            context,
            AltLifeDatabase::class.java,
            "altlife_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAssetDao(database: AltLifeDatabase) = database.assetDao()

    @Provides
    @Singleton
    fun provideCareerDao(database: AltLifeDatabase) = database.careerDao()

    @Provides
    @Singleton
    fun provideCharacterDao(database: AltLifeDatabase) = database.characterDao()

    @Provides
    @Singleton
    fun provideCrimeDao(database: AltLifeDatabase) = database.crimeDao()

    @Provides
    @Singleton
    fun provideEventDao(database: AltLifeDatabase) = database.eventDao()

    @Provides
    @Singleton
    fun providePetDao(database: AltLifeDatabase) = database.petDao()

    @Provides
    @Singleton
    fun provideRelationshipDao(database: AltLifeDatabase) = database.relationshipDao()

    @Provides
    @Singleton
    fun provideSaveSlotDao(database: AltLifeDatabase) = database.saveSlotDao()

    @Provides
    @Singleton
    fun provideScenarioDao(database: AltLifeDatabase) = database.scenarioDao()

    @Provides
    @Singleton
    fun provideUnlockedAchievementDao(database: AltLifeDatabase) = database.unlockedAchievementDao()
}