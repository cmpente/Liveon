// app/src/main/java/com/liveongames/liveon/di/DatabaseModule.kt
package com.liveongames.liveon.di

import android.content.Context
import androidx.room.Room
import com.liveongames.data.db.LiveOnDatabase
import com.liveongames.data.db.dao.CrimeDao
import com.liveongames.data.db.dao.PetDao
import com.liveongames.data.db.dao.EventDao
import com.liveongames.data.db.dao.SaveSlotDao
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
    fun provideDatabase(@ApplicationContext context: Context): LiveOnDatabase {
        return Room.databaseBuilder(
            context,
            LiveOnDatabase::class.java,
            "liveon_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideCrimeDao(database: LiveOnDatabase): CrimeDao {
        return database.crimeDao()
    }

    @Provides
    @Singleton
    fun providePetDao(database: LiveOnDatabase): PetDao {
        return database.petDao()
    }

    @Provides
    @Singleton
    fun provideEventDao(database: LiveOnDatabase): EventDao {
        return database.eventDao()
    }

    @Provides
    @Singleton
    fun provideSaveSlotDao(database: LiveOnDatabase): SaveSlotDao {
        return database.saveSlotDao()
    }
}