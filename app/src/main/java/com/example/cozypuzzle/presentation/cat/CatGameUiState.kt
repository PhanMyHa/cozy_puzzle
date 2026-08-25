package com.example.cozypuzzle.presentation.cat

import com.example.cozypuzzle.core.path.model.CatBoard
import com.example.cozypuzzle.domain.model.PuzzleResult

data class CatGameUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val levelTitle: String = "",
    val board: CatBoard? = null,
    val selectedCell: Int? = null,
    val hintCells: List<Int> = emptyList(),
    val pathCells: List<Int> = emptyList(),
    val pathFound: Boolean = false,
    val moves: Int = 0,
    val elapsedTimeMs: Long = 0L,
    val hintsRemaining: Int = 0,
    val maxHints: Int = 0,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isPaused: Boolean = false,
    val isWalking: Boolean = false,
    val walkIndex: Int = -1,
    val isCompleted: Boolean = false,
    val result: PuzzleResult? = null
)
