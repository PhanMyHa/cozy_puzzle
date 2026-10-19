package com.example.cozypuzzle.presentation.catpuzzle.game

import com.example.cozypuzzle.core.catpuzzle.model.CatPuzzleLevel
import com.example.cozypuzzle.core.catpuzzle.model.CatPuzzleState

data class CatPuzzleGameUiState(
    val level: CatPuzzleLevel? = null,
    val puzzleState: CatPuzzleState? = null,
    val elapsedTimeMs: Long = 0L,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val starsEarned: Int = 0,
    val addedStars: Int = 0,
    val nextLevelId: String? = null,
    val isLoading: Boolean = true
)

sealed class CatPuzzleGameEffect {
    object NavigateBack : CatPuzzleGameEffect()
    data class NavigateToNextLevel(val levelId: String) : CatPuzzleGameEffect()
    data class PlayCorrectAnimation(val slotIndex: Int) : CatPuzzleGameEffect()
    object PlaySuccessSoundOrHaptic : CatPuzzleGameEffect()
}
