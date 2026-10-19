package com.example.cozypuzzle.domain.model

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.core.path.model.StrokePoint

/**
 * A static, fully defined path-drawing puzzle.
 *
 * The board is a square grid of [boardSize] × [boardSize] cells. Cells are
 * indexed row-major: `cell = row * boardSize + column`.
 *
 * This model is pure Kotlin so it can later be served from Firebase without
 * touching gameplay code.
 */
data class DailyPuzzle(
    val id: String,
    val title: String,
    val boardSize: Int,
    val startCell: Int,
    val goalCell: Int,
    val obstacleCells: Set<Int>,
    val difficulty: Difficulty,
    val theme: DailyPuzzleTheme = DailyPuzzleTheme.MEADOW,
    val recommendedBrushId: String? = null
) {
    init {
        require(boardSize in MIN_SIZE..MAX_SIZE) { "Board size must be between $MIN_SIZE and $MAX_SIZE" }
        require(startCell in 0 until cellCount) { "Start cell outside the board" }
        require(goalCell in 0 until cellCount) { "Goal cell outside the board" }
        require(startCell != goalCell) { "Start and goal must differ" }
        require(goalCell !in obstacleCells && startCell !in obstacleCells) {
            "Start/goal cannot be obstacles"
        }
    }

    val cellCount: Int get() = boardSize * boardSize

    /** Center of a cell in board units, where the board spans [0, boardSize]². */
    fun centerOf(cell: Int): StrokePoint = StrokePoint(
        x = (cell % boardSize) + 0.5f,
        y = (cell / boardSize) + 0.5f
    )

    companion object {
        const val MIN_SIZE = 4
        const val MAX_SIZE = 12
    }
}

enum class DailyPuzzleTheme {
    MEADOW,
    POND,
    GARDEN
}
