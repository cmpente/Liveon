package com.liveongames.liveon.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Let's comment out the problematic provides for now and test with just the database
    /*
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): liveonDatabase {
        return Room.databaseBuilder(
            context,
            liveonDatabase::class.java,
            "liveon_database"
        ).fallbackToDestructiveMigration().build()
    }
    */
}