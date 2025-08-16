package com.liveongames.liveon.character

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CharacterAssetModule {
    @Binds
    @Singleton
    abstract fun bindCharacterAssets(impl: CharacterAssetLoaderImpl): CharacterAssetLoader
}