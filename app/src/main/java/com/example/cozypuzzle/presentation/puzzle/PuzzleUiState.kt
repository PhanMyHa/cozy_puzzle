package com.example.cozypuzzle.presentation.puzzle

import com.example.cozypuzzle.core.puzzle.model.Puzzle
import com.example.cozypuzzle.domain.model.PuzzleResult

data class PuzzleUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val puzzleTitle: String = "",
    val imageId: String? = null,
    val puzzle: Puzzle? = null,
    val gridSize: Int = 0,
    val selectedPieceId: Int? = null,
    val hintPieceId: Int? = null,
    val moves: Int = 0,
    val elapsedTimeMs: Long = 0L,
    val hintsRemaining: Int = 0,
    val maxHints: Int = 0,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val result: PuzzleResult? = null,
    val allowRotate: Boolean = true,
    val allowSwap: Boolean = true,
    val allowDrag: Boolean = true
)
