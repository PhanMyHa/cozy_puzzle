package com.example.cozypuzzle.di

import com.example.cozypuzzle.core.common.DefaultDispatcherProvider
import com.example.cozypuzzle.core.common.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    fun provideEpochClock(): () -> Long = System::currentTimeMillis
}
