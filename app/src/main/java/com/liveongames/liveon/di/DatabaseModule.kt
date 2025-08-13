package com.liveongames.liveon.di

import android.content.Context
import androidx.room.Room
import com.liveongames.data.db.LiveonDatabase
import com.liveongames.liveon.data.db.LiveonMigrations
import com.liveongames.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LiveonDatabase =
        Room.databaseBuilder(context, LiveonDatabase::class.java, "liveon.db")
            .addMigrations(
                LiveonDatabase.MIGRATION_1_2,
                LiveonDatabase.MIGRATION_2_3,
                LiveonDatabase.MIGRATION_3_4,
                LiveonMigrations.MIGRATION_4_5,
                LiveonMigrations.MIGRATION_5_6
            )
            .build()

    @Provides @Singleton fun provideAssetDao(db: LiveonDatabase): AssetDao = db.assetDao()
    @Provides @Singleton fun provideCareerDao(db: LiveonDatabase): CareerDao = db.careerDao()
    @Provides @Singleton fun provideCharacterDao(db: LiveonDatabase): CharacterDao = db.characterDao()
    @Provides @Singleton fun provideCrimeDao(db: LiveonDatabase): CrimeDao = db.crimeDao()
    @Provides @Singleton fun provideEducationDao(db: LiveonDatabase): EducationDao = db.educationDao()
    @Provides @Singleton fun provideEducationActionStateDao(db: LiveonDatabase): EducationActionStateDao = db.educationActionStateDao()
    @Provides @Singleton fun provideTermStateDao(db: LiveonDatabase): TermStateDao = db.termStateDao()
    @Provides @Singleton fun provideEventDao(db: LiveonDatabase): EventDao = db.eventDao()
    @Provides @Singleton fun providePetDao(db: LiveonDatabase): PetDao = db.petDao()
    @Provides @Singleton fun provideRelationshipDao(db: LiveonDatabase): RelationshipDao = db.relationshipDao()
    @Provides @Singleton fun provideSaveSlotDao(db: LiveonDatabase): SaveSlotDao = db.saveSlotDao()
    @Provides @Singleton fun provideScenarioDao(db: LiveonDatabase): ScenarioDao = db.scenarioDao()
    @Provides @Singleton fun provideUnlockedAchievementDao(db: LiveonDatabase): UnlockedAchievementDao = db.unlockedAchievementDao()
}
