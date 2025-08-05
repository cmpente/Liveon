package com.altlifegames.altlife.di

import com.altlifegames.domain.repository.ScenarioRepository
import com.altlifegames.data.repository.ScenarioRepositoryImpl
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