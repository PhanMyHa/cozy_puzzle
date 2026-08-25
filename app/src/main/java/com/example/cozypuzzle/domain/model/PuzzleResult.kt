package com.example.cozypuzzle.domain.model

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode

data class PuzzleResult(
    val id: Long = 0L,
    val puzzleId: String,
    val mode: PuzzleMode,
    val difficulty: Difficulty,
    val rows: Int,
    val columns: Int,
    val durationMs: Long,
    val moveCount: Int,
    val hintsUsed: Int,
    val completedAtEpochMs: Long
) {
    val isPerfect: Boolean
        get() = hintsUsed == 0
}
