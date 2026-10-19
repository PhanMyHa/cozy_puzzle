package com.example.cozypuzzle.di

import com.example.cozypuzzle.core.path.validation.DailyPathValidator
import com.example.cozypuzzle.data.datasource.BundledDailyPuzzleDataSource
import com.example.cozypuzzle.data.datasource.LocalDailyPuzzleDataSource
import com.example.cozypuzzle.data.repository.DailyPuzzleRepositoryImpl
import com.example.cozypuzzle.domain.repository.DailyPuzzleRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DailyModule {

    @Provides
    @Singleton
    fun provideDailyPathValidator(): DailyPathValidator = DailyPathValidator()

    @Provides
    fun provideLocalDateProvider(): () -> LocalDate = { LocalDate.now() }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DailyBindModule {

    @Binds
    abstract fun bindLocalDailyPuzzleDataSource(
        impl: BundledDailyPuzzleDataSource
    ): LocalDailyPuzzleDataSource

    @Binds
    abstract fun bindDailyPuzzleRepository(
        impl: DailyPuzzleRepositoryImpl
    ): DailyPuzzleRepository
}
