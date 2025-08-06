// app/src/main/java/com/liveongames/liveon/di/RepositoryModule.kt
package com.liveongames.liveon.di

import com.liveongames.data.repository.CrimeRepositoryImpl
import com.liveongames.data.repository.PetRepositoryImpl
import com.liveongames.data.repository.EventRepositoryImpl
import com.liveongames.data.repository.SaveRepositoryImpl
import com.liveongames.data.repository.ScenarioRepositoryImpl
import com.liveongames.data.repository.PlayerRepositoryImpl  // Add this import
import com.liveongames.domain.repository.CrimeRepository
import com.liveongames.domain.repository.PetRepository
import com.liveongames.domain.repository.EventRepository
import com.liveongames.domain.repository.SaveRepository
import com.liveongames.domain.repository.ScenarioRepository
import com.liveongames.domain.repository.PlayerRepository  // Add this import
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideScenarioRepository(): ScenarioRepository {
        return ScenarioRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideCrimeRepository(impl: CrimeRepositoryImpl): CrimeRepository {
        return impl
    }

    @Provides
    @Singleton
    fun providePetRepository(impl: PetRepositoryImpl): PetRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideEventRepository(impl: EventRepositoryImpl): EventRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideSaveRepository(impl: SaveRepositoryImpl): SaveRepository {
        return impl
    }

    // Add this provider for PlayerRepository
    @Provides
    @Singleton
    fun providePlayerRepository(): PlayerRepository {
        return PlayerRepositoryImpl()
    }
}