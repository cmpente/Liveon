package com.altlifegames.altlife.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Let's comment out the problematic provides for now and test with just the database
    /*
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AltLifeDatabase {
        return Room.databaseBuilder(
            context,
            AltLifeDatabase::class.java,
            "altlife_database"
        ).fallbackToDestructiveMigration().build()
    }
    */
}