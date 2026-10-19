package com.example.cozypuzzle.presentation.catpuzzle.select

import androidx.annotation.DrawableRes
import com.example.cozypuzzle.core.catpuzzle.model.CatPuzzleDifficulty

data class CatPuzzleLevelItemUiModel(
    val id: String,
    val number: Int,
    @DrawableRes val imageRes: Int,
    val stars: Int,
    val isUnlocked: Boolean
)

data class CatPuzzleLevelSelectUiState(
    val playerStars: Int = 0,
    val selectedDifficulty: CatPuzzleDifficulty = CatPuzzleDifficulty.BASIC,
    val levels: List<CatPuzzleLevelItemUiModel> = emptyList(),
    val isLoading: Boolean = true
)

sealed class CatPuzzleSelectEffect {
    data class NavigateToGame(val levelId: String) : CatPuzzleSelectEffect()
    data class ShowLockedDialog(val levelNumber: Int = 1) : CatPuzzleSelectEffect()
    object NavigateBack : CatPuzzleSelectEffect()
}
