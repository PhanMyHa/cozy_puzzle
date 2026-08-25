package com.example.cozypuzzle.data.repository

import com.example.cozypuzzle.data.local.dao.PuzzleResultDao
import com.example.cozypuzzle.data.local.dao.PuzzleSessionDao
import com.example.cozypuzzle.data.mapper.toDomain
import com.example.cozypuzzle.data.mapper.toEntity
import com.example.cozypuzzle.domain.model.PuzzleResult
import com.example.cozypuzzle.domain.repository.PuzzleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PuzzleRepositoryImpl @Inject constructor(
    private val puzzleSessionDao: PuzzleSessionDao,
    private val puzzleResultDao: PuzzleResultDao
) : PuzzleRepository {

    override suspend fun saveResult(result: PuzzleResult): Long =
        puzzleResultDao.insert(result.toEntity())

    override suspend fun getResultsForPuzzle(puzzleId: String): List<PuzzleResult> =
        puzzleResultDao.getByPuzzleId(puzzleId).map { it.toDomain() }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PuzzleRepositoryModule {

    @Binds
    abstract fun bindPuzzleRepository(impl: PuzzleRepositoryImpl): PuzzleRepository
}
