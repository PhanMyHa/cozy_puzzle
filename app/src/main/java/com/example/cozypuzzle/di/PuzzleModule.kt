package com.example.cozypuzzle.di

import com.example.cozypuzzle.core.puzzle.engine.GridPuzzleEngine
import com.example.cozypuzzle.core.puzzle.engine.GridPuzzleValidator
import com.example.cozypuzzle.core.puzzle.engine.PuzzleEngine
import com.example.cozypuzzle.core.puzzle.engine.PuzzleGenerator
import com.example.cozypuzzle.core.puzzle.engine.PuzzleValidator
import com.example.cozypuzzle.core.puzzle.engine.SeededPuzzleGenerator
import com.example.cozypuzzle.data.remote.firebase.BasicPuzzleConfigParser
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PuzzleModule {

    @Provides
    @Singleton
    fun provideGridPuzzleEngine(): GridPuzzleEngine = GridPuzzleEngine()

    @Provides
    @Singleton
    fun provideGridPuzzleValidator(): GridPuzzleValidator = GridPuzzleValidator()

    @Provides
    @Singleton
    fun provideSeededPuzzleGenerator(): SeededPuzzleGenerator = SeededPuzzleGenerator()

    @Provides
    @Singleton
    fun provideBasicPuzzleConfigParser(): BasicPuzzleConfigParser = BasicPuzzleConfigParser()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PuzzleEngineBindModule {

    @Binds
    abstract fun bindPuzzleEngine(impl: GridPuzzleEngine): PuzzleEngine

    @Binds
    abstract fun bindPuzzleValidator(impl: GridPuzzleValidator): PuzzleValidator

    @Binds
    abstract fun bindPuzzleGenerator(impl: SeededPuzzleGenerator): PuzzleGenerator
}
