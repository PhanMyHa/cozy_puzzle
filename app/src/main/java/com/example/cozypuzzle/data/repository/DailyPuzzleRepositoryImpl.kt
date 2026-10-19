package com.example.cozypuzzle.data.repository

import com.example.cozypuzzle.data.datasource.LocalDailyPuzzleDataSource
import com.example.cozypuzzle.domain.model.DailyPuzzle
import com.example.cozypuzzle.domain.repository.DailyPuzzleRepository
import javax.inject.Inject

class DailyPuzzleRepositoryImpl @Inject constructor(
    private val dataSource: LocalDailyPuzzleDataSource
) : DailyPuzzleRepository {

    override suspend fun getDailyPuzzles(): List<DailyPuzzle> = dataSource.getPuzzles()

    override suspend fun getDailyPuzzle(puzzleId: String): DailyPuzzle? =
        dataSource.getPuzzle(puzzleId)
}
