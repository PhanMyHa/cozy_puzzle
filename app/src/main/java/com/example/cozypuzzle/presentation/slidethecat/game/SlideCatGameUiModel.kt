package com.example.cozypuzzle.presentation.slidethecat.game

import com.example.cozypuzzle.core.slidethecat.model.SlideCatState

data class SlideCatGameUiState(
    val gameState: SlideCatState? = null,
    val formattedTime: String = "00:00",
    val showRestartConfirmation: Boolean = false
)

sealed interface SlideCatGameEffect {
    data object ShowPauseDialog : SlideCatGameEffect
    data object DismissPauseDialog : SlideCatGameEffect
    data class ShowLevelComplete(
        val stars: Int,
        val moves: Int,
        val hintsUsed: Int,
        val formattedTime: String,
        val nextLevelId: String?,
        val addedStars: Int = 0
    ) : SlideCatGameEffect
    data object NavigateToLevels : SlideCatGameEffect
    data class NavigateToNextLevel(val levelId: String) : SlideCatGameEffect
    data class ShowMessage(val messageRes: Int) : SlideCatGameEffect
}
