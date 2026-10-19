package com.example.cozypuzzle.presentation.slidethecat.select

import com.example.cozypuzzle.core.slidethecat.model.SlideCatDifficulty
import com.example.cozypuzzle.core.slidethecat.model.SlideCatLevel

data class SlideCatLevelUiItem(
    val level: SlideCatLevel,
    val isUnlocked: Boolean,
    val stars: Int
)

data class SlideCatLevelSelectUiState(
    val selectedDifficulty: SlideCatDifficulty = SlideCatDifficulty.EASY,
    val levels: List<SlideCatLevelUiItem> = emptyList(),
    val totalStars: Int = 0,
    val isLoading: Boolean = false
)

sealed interface SlideCatLevelSelectEffect {
    data class NavigateToGame(val levelId: String) : SlideCatLevelSelectEffect
    data class ShowLockedDialog(val levelNumber: Int) : SlideCatLevelSelectEffect
    data object NavigateBack : SlideCatLevelSelectEffect
}
