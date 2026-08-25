package com.example.cozypuzzle.di

import com.example.cozypuzzle.data.remote.firebase.FirebaseRemoteConfigDataSource
import com.example.cozypuzzle.data.remote.firebase.RemoteConfigDataSource
import com.example.cozypuzzle.data.repository.ConfigRepositoryImpl
import com.example.cozypuzzle.domain.repository.ConfigRepository
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig = Firebase.remoteConfig

    @Provides
    @Singleton
    fun provideRemoteConfigDataSource(
        impl: FirebaseRemoteConfigDataSource
    ): RemoteConfigDataSource = impl

    @Provides
    @Singleton
    fun provideConfigRepository(
        remoteConfigDataSource: RemoteConfigDataSource
    ): ConfigRepository = ConfigRepositoryImpl(remoteConfigDataSource)
}
