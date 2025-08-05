package com.liveongames.liveon.di

import com.liveongames.domain.repository.ScenarioRepository
import com.liveongames.domain.usecase.GetScenarioUseCase
import com.liveongames.domain.usecase.GetScenariosUseCase
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