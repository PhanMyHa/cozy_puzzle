package com.example.cozypuzzle.di

import com.example.cozypuzzle.core.path.engine.CatBoardEngine
import com.example.cozypuzzle.core.path.engine.CatBoardGenerator
import com.example.cozypuzzle.core.path.solver.AStarPathFinder
import com.example.cozypuzzle.core.path.solver.PathFinder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CatModule {

    @Provides
    @Singleton
    fun provideCatBoardEngine(): CatBoardEngine = CatBoardEngine()

    @Provides
    @Singleton
    fun provideCatBoardGenerator(): CatBoardGenerator = CatBoardGenerator()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CatBindModule {

    @Binds
    abstract fun bindPathFinder(impl: AStarPathFinder): PathFinder
}
