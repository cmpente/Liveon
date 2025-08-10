// app/src/main/java/com/liveongames/liveon/di/RepositoryModule.kt
package com.liveongames.liveon.di

import com.liveongames.domain.repository.CrimeRepository
import com.liveongames.domain.repository.EventRepository
import com.liveongames.domain.repository.PetRepository
import com.liveongames.domain.repository.PlayerRepository
import com.liveongames.domain.repository.EducationRepository
import com.liveongames.data.repository.CrimeRepositoryImpl
import com.liveongames.data.repository.EventRepositoryImpl
import com.liveongames.data.repository.PetRepositoryImpl
import com.liveongames.data.repository.PlayerRepositoryImpl
import com.liveongames.data.repository.EducationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds @Singleton
    abstract fun bindCrimeRepository(impl: CrimeRepositoryImpl): CrimeRepository

    @Binds @Singleton
    abstract fun bindPlayerRepository(impl: PlayerRepositoryImpl): PlayerRepository

    @Binds @Singleton
    abstract fun bindPetRepository(impl: PetRepositoryImpl): PetRepository

    @Binds @Singleton
    abstract fun bindEducationRepository(impl: EducationRepositoryImpl): EducationRepository
}
