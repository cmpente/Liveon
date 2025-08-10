// app/src/main/java/com/liveongames/liveon/di/DatabaseModule.kt
package com.liveongames.liveon.di

import android.content.Context
import androidx.room.Room
import com.liveongames.data.db.LiveonDatabase
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

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LiveonDatabase {
        return Room.databaseBuilder(
            context,
            LiveonDatabase::class.java,
            "liveon_database"
        )
            .addMigrations(LiveonDatabase.MIGRATION_1_2)
            .addMigrations(LiveonDatabase.MIGRATION_2_3)
            .addMigrations(LiveonDatabase.MIGRATION_3_4)
            .fallbackToDestructiveMigration() // Add this for development
            .build()
    }

    @Provides
    @Singleton
    fun provideCrimeDao(database: LiveonDatabase): CrimeDao {
        return database.crimeDao()
    }

    @Provides
    @Singleton
    fun providePetDao(database: LiveonDatabase): PetDao {
        return database.petDao()
    }

    @Provides
    @Singleton
    fun provideEventDao(database: LiveonDatabase): EventDao {
        return database.eventDao()
    }

    @Provides
    @Singleton
    fun provideSaveSlotDao(database: LiveonDatabase): SaveSlotDao {
        return database.saveSlotDao()
    }

    @Provides
    @Singleton
    fun provideCharacterDao(database: LiveonDatabase): CharacterDao {
        return database.characterDao()
    }

    @Provides
    @Singleton
    fun provideEducationDao(database: LiveonDatabase): EducationDao {
        return database.educationDao()
    }
}