package com.example.cozypuzzle.core.common

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

interface ClockProvider {
    fun currentDate(): LocalDate
    fun currentDateString(): String = currentDate().toString()
    fun currentEpochDay(): Long = currentDate().toEpochDay()
    fun dayOfWeek(): DayOfWeek = currentDate().dayOfWeek
    fun currentTimeMillis(): Long = System.currentTimeMillis()
}

@Singleton
class SystemClockProvider @Inject constructor() : ClockProvider {
    override fun currentDate(): LocalDate = LocalDate.now()
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ClockProviderModule {
    @Binds
    @Singleton
    abstract fun bindClockProvider(impl: SystemClockProvider): ClockProvider
}
