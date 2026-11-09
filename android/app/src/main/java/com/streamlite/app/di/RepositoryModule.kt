package com.streamlite.app.di

import com.streamlite.app.data.repository.media.MediaRepositoryImpl
import com.streamlite.app.data.repository.media.PreferencesRepositoryImpl
import com.streamlite.app.domain.repository.MediaRepository
import com.streamlite.app.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMediaRepository(impl: MediaRepositoryImpl): MediaRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}
