// app/src/main/java/com/liveongames/liveon/di/UseCaseModule.kt
package com.liveongames.liveon.di

import com.liveongames.domain.repository.CrimeRepository
import com.liveongames.domain.repository.PetRepository
import com.liveongames.domain.repository.ScenarioRepository
import com.liveongames.domain.repository.EventRepository
import com.liveongames.domain.usecase.AdoptPetUseCase
import com.liveongames.domain.usecase.ClearCriminalRecordUseCase
import com.liveongames.domain.usecase.GetCrimeStatsUseCase
import com.liveongames.domain.usecase.GetCrimesUseCase
import com.liveongames.domain.usecase.RecordCrimeUseCase
import com.liveongames.domain.usecase.RemovePetUseCase
import com.liveongames.domain.usecase.GetScenarioUseCase
import com.liveongames.domain.usecase.GetScenariosUseCase
import com.liveongames.domain.usecase.GetRandomEventsUseCase
import com.liveongames.domain.usecase.GetYearlyEventsUseCase
import com.liveongames.domain.usecase.AddEventUseCase
import com.liveongames.domain.usecase.RemoveEventUseCase
import com.liveongames.domain.usecase.MarkEventAsShownUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // Scenario UseCases
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

    // Crime UseCases
    @Provides
    @Singleton
    fun provideGetCrimesUseCase(repository: CrimeRepository): GetCrimesUseCase {
        return GetCrimesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRecordCrimeUseCase(repository: CrimeRepository): RecordCrimeUseCase {
        return RecordCrimeUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideClearCriminalRecordUseCase(repository: CrimeRepository): ClearCriminalRecordUseCase {
        return ClearCriminalRecordUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetCrimeStatsUseCase(repository: CrimeRepository): GetCrimeStatsUseCase {
        return GetCrimeStatsUseCase(repository)
    }

    // Pet UseCases
    @Provides
    @Singleton
    fun provideAdoptPetUseCase(repository: PetRepository): AdoptPetUseCase {
        return AdoptPetUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRemovePetUseCase(repository: PetRepository): RemovePetUseCase {
        return RemovePetUseCase(repository)
    }

    // Event UseCases - ADD THESE!
    @Provides
    @Singleton
    fun provideGetRandomEventsUseCase(repository: EventRepository): GetRandomEventsUseCase {
        return GetRandomEventsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetYearlyEventsUseCase(repository: EventRepository): GetYearlyEventsUseCase {
        return GetYearlyEventsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddEventUseCase(repository: EventRepository): AddEventUseCase {
        return AddEventUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRemoveEventUseCase(repository: EventRepository): RemoveEventUseCase {
        return RemoveEventUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideMarkEventAsShownUseCase(repository: EventRepository): MarkEventAsShownUseCase {
        return MarkEventAsShownUseCase(repository)
    }
}