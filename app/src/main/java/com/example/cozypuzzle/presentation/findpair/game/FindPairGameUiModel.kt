package com.example.cozypuzzle.presentation.findpair.game

import com.example.cozypuzzle.core.findpair.model.FindPairState

data class FindPairGameUiState(
    val gameState: FindPairState? = null,
    val formattedTimer: String = "02:00",
    val showResetConfirmation: Boolean = false
)

sealed interface FindPairGameEffect {
    data object ShowPauseDialog : FindPairGameEffect
    data object DismissPauseDialog : FindPairGameEffect
    data object ShowGameOverDialog : FindPairGameEffect
    data class ShowLevelComplete(
        val stars: Int,
        val pairsFound: Int,
        val totalPairs: Int,
        val flips: Int,
        val formattedTime: String,
        val nextLevelId: String?,
        val addedStars: Int = 0
    ) : FindPairGameEffect
    data object NavigateToLevels : FindPairGameEffect
    data class NavigateToNextLevel(val levelId: String) : FindPairGameEffect
    data class ShowMessage(val messageRes: Int) : FindPairGameEffect
    data class AnimateCardFlip(val cardId: Int, val toFaceUp: Boolean) : FindPairGameEffect
    data class AnimateCardsMatched(val cardId1: Int, val cardId2: Int) : FindPairGameEffect
}
