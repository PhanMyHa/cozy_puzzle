package com.example.cozypuzzle.presentation.daily

import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.path.model.StrokePoint
import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.domain.model.DailyPuzzleResult

data class DailyPuzzleUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val puzzleId: String = "",
    val puzzleTitle: String = "",
    val boardSize: Int = 0,
    val startCell: Int = -1,
    val goalCell: Int = -1,
    val obstacleCells: Set<Int> = emptySet(),
    val difficultyLabelRes: Int = 0,

    /** Id of the currently selected brush color, resolved via BrushPalette. */
    val selectedBrushId: String = "",

    /** Logical stroke in board units; the single source of truth for drawing. */
    val pathPoints: List<StrokePoint> = emptyList(),

    val isDrawing: Boolean = false,

    /** True while the failure feedback (flash/shake) plays before clearing. */
    val isFailing: Boolean = false,

    val attempts: Int = 0,
    val elapsedTimeMs: Long = 0L,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val result: DailyPuzzleResult? = null
) {
    val acceptsInput: Boolean
        get() = !isLoading && !isError && !isPaused && !isCompleted && !isFailing

    companion object {
        fun difficultyLabelRes(difficulty: Difficulty): Int = when (difficulty) {
            Difficulty.EASY -> R.string.difficulty_easy
            Difficulty.MEDIUM -> R.string.difficulty_medium
            Difficulty.HARD -> R.string.difficulty_hard
            Difficulty.EXPERT -> R.string.difficulty_expert
        }
    }
}

sealed interface DailyPuzzleEvent {

    data class StrokeStarted(val x: Float, val y: Float) : DailyPuzzleEvent

    data class StrokeExtended(val points: List<StrokePoint>) : DailyPuzzleEvent

    data object StrokeEnded : DailyPuzzleEvent

    data class ColorSelected(val brushId: String) : DailyPuzzleEvent

    data object ClearStroke : DailyPuzzleEvent

    data object Restart : DailyPuzzleEvent

    data object Back : DailyPuzzleEvent
}

sealed interface DailyPuzzleEffect {

    /** Obstacle hit / invalid attempt: board shakes and the stroke resets. */
    data object ShowFailure : DailyPuzzleEffect

    data object NavigateBack : DailyPuzzleEffect
}
