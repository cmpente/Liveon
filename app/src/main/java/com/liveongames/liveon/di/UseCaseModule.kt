// app/src/main/java/com/liveongames/liveon/di/UseCaseModule.kt
package com.liveongames.liveon.di

import com.liveongames.domain.repository.CrimeRepository
import com.liveongames.domain.repository.EventRepository
import com.liveongames.domain.repository.PetRepository
import com.liveongames.domain.repository.ScenarioRepository
import com.liveongames.domain.usecase.AdoptPetUseCase
import com.liveongames.domain.usecase.AddEventUseCase
import com.liveongames.domain.usecase.GetCrimeStatsUseCase
import com.liveongames.domain.usecase.GetCrimesUseCase
import com.liveongames.domain.usecase.GetRandomEventsUseCase
import com.liveongames.domain.usecase.GetScenarioUseCase
import com.liveongames.domain.usecase.GetScenariosUseCase
import com.liveongames.domain.usecase.GetYearlyEventsUseCase
import com.liveongames.domain.usecase.MarkEventAsShownUseCase
import com.liveongames.domain.usecase.RecordCrimeUseCase
import com.liveongames.domain.usecase.RemoveEventUseCase
import com.liveongames.domain.usecase.RemovePetUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // Scenarios
    @Provides @Singleton fun provideGetScenarioUseCase(repo: ScenarioRepository) = GetScenarioUseCase(repo)
    @Provides @Singleton fun provideGetScenariosUseCase(repo: ScenarioRepository) = GetScenariosUseCase(repo)

    // Crimes
    @Provides @Singleton fun provideGetCrimesUseCase(repo: CrimeRepository) = GetCrimesUseCase(repo)
    @Provides @Singleton fun provideRecordCrimeUseCase(repo: CrimeRepository) = RecordCrimeUseCase(repo)
    @Provides @Singleton fun provideGetCrimeStatsUseCase(repo: CrimeRepository) = GetCrimeStatsUseCase(repo)

    // Pets
    @Provides @Singleton fun provideAdoptPetUseCase(repo: PetRepository) = AdoptPetUseCase(repo)
    @Provides @Singleton fun provideRemovePetUseCase(repo: PetRepository) = RemovePetUseCase(repo)

    // Events
    @Provides @Singleton fun provideGetRandomEventsUseCase(repo: EventRepository) = GetRandomEventsUseCase(repo)
    @Provides @Singleton fun provideGetYearlyEventsUseCase(repo: EventRepository) = GetYearlyEventsUseCase(repo)
    @Provides @Singleton fun provideAddEventUseCase(repo: EventRepository) = AddEventUseCase(repo)
    @Provides @Singleton fun provideRemoveEventUseCase(repo: EventRepository) = RemoveEventUseCase(repo)
    @Provides @Singleton fun provideMarkEventAsShownUseCase(repo: EventRepository) = MarkEventAsShownUseCase(repo)
}