package com.example.cozypuzzle.data.repository

import com.example.cozypuzzle.data.datasource.BundledBasicPuzzleDataSource
import com.example.cozypuzzle.data.datasource.LocalBasicPuzzleDataSource
import com.example.cozypuzzle.domain.model.BasicPuzzle
import com.example.cozypuzzle.domain.repository.BasicPuzzleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BasicPuzzleRepositoryImpl @Inject constructor(
    private val localDataSource: LocalBasicPuzzleDataSource
) : BasicPuzzleRepository {

    override suspend fun getBasicPuzzles(): List<BasicPuzzle> =
        localDataSource.getPuzzles()

    override suspend fun getBasicPuzzle(puzzleId: String): BasicPuzzle? =
        localDataSource.getPuzzle(puzzleId)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BasicPuzzleRepositoryModule {

    @Binds
    abstract fun bindBasicPuzzleRepository(impl: BasicPuzzleRepositoryImpl): BasicPuzzleRepository

    @Binds
    abstract fun bindLocalBasicPuzzleDataSource(
        impl: BundledBasicPuzzleDataSource
    ): LocalBasicPuzzleDataSource
}
