package com.example.cozypuzzle.presentation.findpair.select

import com.example.cozypuzzle.core.findpair.model.FindPairDifficulty
import com.example.cozypuzzle.core.findpair.model.FindPairLevel

data class FindPairLevelUiItem(
    val level: FindPairLevel,
    val isUnlocked: Boolean,
    val stars: Int
)

data class FindPairLevelSelectUiState(
    val selectedDifficulty: FindPairDifficulty = FindPairDifficulty.EASY,
    val levels: List<FindPairLevelUiItem> = emptyList(),
    val totalStars: Int = 0,
    val isLoading: Boolean = false
)

sealed interface FindPairLevelSelectEffect {
    data class NavigateToGame(val levelId: String) : FindPairLevelSelectEffect
    data class ShowLockedDialog(val levelNumber: Int) : FindPairLevelSelectEffect
    data object NavigateBack : FindPairLevelSelectEffect
}
