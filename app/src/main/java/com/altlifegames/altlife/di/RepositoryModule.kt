package com.altlifegames.altlife.di

import com.altlifegames.data.repository.CharacterRepositoryImpl
import com.altlifegames.data.repository.CrimeRepositoryImpl
import com.altlifegames.data.repository.EventRepositoryImpl
import com.altlifegames.domain.repository.CharacterRepository
import com.altlifegames.domain.repository.CrimeRepository
import com.altlifegames.domain.repository.EventRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    abstract fun bindCharacterRepository(
        characterRepositoryImpl: CharacterRepositoryImpl
    ): CharacterRepository
    
    @Binds
    abstract fun bindEventRepository(
        eventRepositoryImpl: EventRepositoryImpl
    ): EventRepository
    
    @Binds
    abstract fun bindCrimeRepository(
        crimeRepositoryImpl: CrimeRepositoryImpl
    ): CrimeRepository
}