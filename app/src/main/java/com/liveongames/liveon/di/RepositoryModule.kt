package com.liveongames.liveon.di

import com.liveongames.domain.repository.ScenarioRepository
import com.liveongames.data.repository.ScenarioRepositoryImpl
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
}