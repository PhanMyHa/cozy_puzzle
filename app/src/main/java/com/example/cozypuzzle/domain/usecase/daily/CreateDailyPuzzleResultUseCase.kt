package com.example.cozypuzzle.domain.usecase.daily

import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import com.example.cozypuzzle.domain.model.DailyPuzzle
import com.example.cozypuzzle.domain.model.DailyPuzzleResult
import com.example.cozypuzzle.domain.model.PuzzleResult
import javax.inject.Inject

/**
 * Builds the completion record for a solved daily puzzle.
 */
class CreateDailyPuzzleResultUseCase @Inject constructor(
    private val clock: () -> Long
) {

    data class Stats(
        val puzzle: DailyPuzzle,
        val durationMs: Long,
        val attempts: Int,
        val brushColorId: String
    )

    operator fun invoke(stats: Stats): DailyPuzzleResult = DailyPuzzleResult(
        puzzleId = stats.puzzle.id,
        durationMs = stats.durationMs,
        attempts = stats.attempts,
        brushColorId = stats.brushColorId,
        completedAtEpochMs = clock()
    )

    /**
     * Maps the daily-specific result onto the shared [PuzzleResult] model so
     * it can be persisted through the existing results infrastructure.
     */
    fun toUniversalResult(
        stats: Stats,
        result: DailyPuzzleResult,
        id: Long = 0L
    ): PuzzleResult = PuzzleResult(
        id = id,
        puzzleId = result.puzzleId,
        mode = PuzzleMode.DAILY_DRAW,
        difficulty = stats.puzzle.difficulty,
        rows = stats.puzzle.boardSize,
        columns = stats.puzzle.boardSize,
        durationMs = result.durationMs,
        moveCount = result.attempts,
        hintsUsed = NO_HINTS,
        attempts = result.attempts,
        brushColorId = result.brushColorId,
        completedAtEpochMs = result.completedAtEpochMs
    )

    private companion object {
        const val NO_HINTS = 0
    }
}
