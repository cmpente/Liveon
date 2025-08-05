package com.altlifegames.altlife.di

import com.altlifegames.domain.repository.ScenarioRepository
import com.altlifegames.domain.usecase.GetScenarioUseCase
import com.altlifegames.domain.usecase.GetScenariosUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetScenarioUseCase(repository: ScenarioRepository): GetScenarioUseCase {
        return GetScenarioUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetScenariosUseCase(repository: ScenarioRepository): GetScenariosUseCase {
        return GetScenariosUseCase(repository)
    }
}