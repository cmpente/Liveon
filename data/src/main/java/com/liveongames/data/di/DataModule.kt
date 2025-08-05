package com.liveongames.data.di

import com.liveongames.data.repository.*
import com.liveongames.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAssetRepository(impl: AssetRepositoryImpl): AssetRepository

    @Binds
    @Singleton
    abstract fun bindCareerRepository(impl: CareerRepositoryImpl): CareerRepository

    @Binds
    @Singleton
    abstract fun bindCharacterRepository(impl: CharacterRepositoryImpl): CharacterRepository

    @Binds
    @Singleton
    abstract fun bindClubRepository(impl: ClubRepositoryImpl): ClubRepository

    @Binds
    @Singleton
    abstract fun bindCrimeRepository(impl: CrimeRepositoryImpl): CrimeRepository

    @Binds
    @Singleton
    abstract fun bindEducationRepository(impl: EducationRepositoryImpl): EducationRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository

    @Binds
    @Singleton
    abstract fun bindAchievementRepository(impl: AchievementRepositoryImpl): AchievementRepository

    @Binds
    @Singleton
    abstract fun bindPetRepository(impl: PetRepositoryImpl): PetRepository

    @Binds
    @Singleton
    abstract fun bindRelationshipRepository(impl: RelationshipRepositoryImpl): RelationshipRepository

    @Binds
    @Singleton
    abstract fun bindSaveRepository(impl: SaveRepositoryImpl): SaveRepository

    @Binds
    @Singleton
    abstract fun bindScenarioRepository(impl: ScenarioRepositoryImpl): ScenarioRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}