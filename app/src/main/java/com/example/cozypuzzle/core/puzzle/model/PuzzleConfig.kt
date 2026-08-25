package com.example.cozypuzzle.core.puzzle.model

data class PuzzleConfig(
    val rows: Int,
    val columns: Int,
    val mode: PuzzleMode,
    val difficulty: Difficulty,
    val contentId: String? = null
) {
    val pieceCount: Int
        get() = rows * columns
}
