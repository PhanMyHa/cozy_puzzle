package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import com.example.cozypuzzle.domain.model.PuzzleResult
import javax.inject.Inject

class CreatePuzzleResultUseCase @Inject constructor(
    private val clock: () -> Long
) {

    data class Stats(
        val puzzleId: String,
        val mode: PuzzleMode,
        val difficulty: Difficulty,
        val gridSize: Int,
        val durationMs: Long,
        val moveCount: Int,
        val hintsUsed: Int
    )

    operator fun invoke(stats: Stats): PuzzleResult = PuzzleResult(
        puzzleId = stats.puzzleId,
        mode = stats.mode,
        difficulty = stats.difficulty,
        rows = stats.gridSize,
        columns = stats.gridSize,
        durationMs = stats.durationMs,
        moveCount = stats.moveCount,
        hintsUsed = stats.hintsUsed,
        completedAtEpochMs = clock()
    )
}
