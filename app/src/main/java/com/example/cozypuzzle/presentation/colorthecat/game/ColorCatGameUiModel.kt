package com.example.cozypuzzle.presentation.colorthecat.game

import com.example.cozypuzzle.core.colorthecat.model.ColorCatState

data class ColorCatGameUiState(
    val gameState: ColorCatState? = null,
    val formattedTime: String = "00:00",
    val canUndo: Boolean = false,
    val showResetConfirmation: Boolean = false
)

sealed interface ColorCatGameEffect {
    data object ShowPauseDialog : ColorCatGameEffect
    data object DismissPauseDialog : ColorCatGameEffect
    data class ShowLevelComplete(
        val stars: Int,
        val hintsUsed: Int,
        val formattedTime: String,
        val nextLevelId: String?,
        val addedStars: Int = 0
    ) : ColorCatGameEffect
    data object NavigateToLevels : ColorCatGameEffect
    data class NavigateToNextLevel(val levelId: String) : ColorCatGameEffect
    data class ShowMessage(val messageRes: Int) : ColorCatGameEffect
}
