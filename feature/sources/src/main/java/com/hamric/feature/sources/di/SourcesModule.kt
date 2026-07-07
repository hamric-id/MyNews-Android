package com.hamric.feature.sources.di

import com.hamric.feature.sources.data.repository.SourcesRepositoryImpl
import com.hamric.feature.sources.domain.repository.SourcesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SourcesModule {

    @Binds
    @Singleton
    abstract fun bindSourcesRepository(
        impl: SourcesRepositoryImpl
    ): SourcesRepository
}