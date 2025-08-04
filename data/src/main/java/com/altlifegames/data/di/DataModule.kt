package com.altlifegames.data.di

import android.content.Context
import androidx.room.Room
import com.altlifegames.data.db.AltLifeDatabase
import com.altlifegames.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
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
    fun provideCharacterDao(database: AltLifeDatabase): CharacterDao = database.characterDao()
    
    @Provides
    fun provideAssetDao(database: AltLifeDatabase): AssetDao = database.assetDao()
    
    @Provides
    fun provideCareerDao(database: AltLifeDatabase): CareerDao = database.careerDao()
    
    @Provides
    fun provideCrimeDao(database: AltLifeDatabase): CrimeDao = database.crimeDao()
    
    @Provides
    fun providePetDao(database: AltLifeDatabase): PetDao = database.petDao()
    
    @Provides
    fun provideUnlockedAchievementDao(database: AltLifeDatabase): UnlockedAchievementDao = database.unlockedAchievementDao()
    
    @Provides
    fun provideSaveSlotDao(database: AltLifeDatabase): SaveSlotDao = database.saveSlotDao()
}