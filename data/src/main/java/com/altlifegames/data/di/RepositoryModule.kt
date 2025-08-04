package com.altlifegames.data.di

import com.altlifegames.data.repository.SaveRepositoryImpl
import com.altlifegames.domain.repository.SaveRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    abstract fun bindSaveRepository(saveRepositoryImpl: SaveRepositoryImpl): SaveRepository
}