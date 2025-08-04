package com.altlifegames.altlife.di

import com.altlifegames.domain.usecase.AdvanceYearUseCase
import com.altlifegames.domain.usecase.ApplyEventUseCase
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
    fun provideAdvanceYearUseCase(): AdvanceYearUseCase {
        return AdvanceYearUseCase()
    }

    @Provides
    @Singleton
    fun provideApplyEventUseCase(): ApplyEventUseCase {
        return ApplyEventUseCase()
    }
}