package com.example.cozypuzzle.domain.repository

import com.example.cozypuzzle.domain.model.PuzzleResult

interface PuzzleRepository {

    suspend fun saveResult(result: PuzzleResult): Long

    suspend fun getResultsForPuzzle(puzzleId: String): List<PuzzleResult>
}
