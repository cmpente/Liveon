package com.altlifegames.altlife.di

import android.content.Context
import androidx.room.Room
import com.altlifegames.data.db.AltLifeDatabase
import com.altlifegames.data.db.dao.AssetDao
import com.altlifegames.data.db.dao.CareerDao
import com.altlifegames.data.db.dao.CharacterDao
import com.altlifegames.data.db.dao.RelationshipDao
import com.altlifegames.data.db.dao.UnlockedAchievementDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AltLifeDatabase =
        Room.databaseBuilder(
            context,
            AltLifeDatabase::class.java,
            "altlife.db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideCharacterDao(db: AltLifeDatabase): CharacterDao = db.characterDao()

    @Provides
    fun provideRelationshipDao(db: AltLifeDatabase): RelationshipDao = db.relationshipDao()

    @Provides
    fun provideAssetDao(db: AltLifeDatabase): AssetDao = db.assetDao()

    @Provides
    fun provideCareerDao(db: AltLifeDatabase): CareerDao = db.careerDao()

    @Provides
    fun provideUnlockedAchievementDao(db: AltLifeDatabase): UnlockedAchievementDao = db.unlockedAchievementDao()

    @Provides
    fun providePetDao(db: AltLifeDatabase): com.altlifegames.data.db.dao.PetDao = db.petDao()

    @Provides
    fun provideCrimeDao(db: AltLifeDatabase): com.altlifegames.data.db.dao.CrimeDao = db.crimeDao()

    @Provides
    fun provideSaveSlotDao(db: AltLifeDatabase): com.altlifegames.data.db.dao.SaveSlotDao = db.saveSlotDao()
}