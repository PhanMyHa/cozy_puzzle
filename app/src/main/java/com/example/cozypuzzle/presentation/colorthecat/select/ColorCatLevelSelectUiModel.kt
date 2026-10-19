package com.example.cozypuzzle.presentation.colorthecat.select

import com.example.cozypuzzle.core.colorthecat.model.ColorCatDifficulty
import com.example.cozypuzzle.core.colorthecat.model.ColorCatLevel

data class ColorCatLevelUiItem(
    val level: ColorCatLevel,
    val isUnlocked: Boolean,
    val stars: Int
)

data class ColorCatLevelSelectUiState(
    val selectedDifficulty: ColorCatDifficulty = ColorCatDifficulty.BASIC,
    val levels: List<ColorCatLevelUiItem> = emptyList(),
    val totalStars: Int = 0,
    val isLoading: Boolean = false
)

sealed interface ColorCatLevelSelectEffect {
    data class NavigateToGame(val levelId: String) : ColorCatLevelSelectEffect
    data class ShowLockedDialog(val levelNumber: Int) : ColorCatLevelSelectEffect
    data object NavigateBack : ColorCatLevelSelectEffect
}
