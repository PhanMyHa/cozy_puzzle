package com.example.cozypuzzle.presentation.cat.select

import androidx.annotation.StringRes
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.puzzle.model.Difficulty

data class CatLevelListItemUiModel(
    val id: String,
    val title: String,
    @StringRes val difficultyLabelRes: Int,
    val gridSize: Int,
    val bestTimeMs: Long?
)

data class CatLevelSelectUiState(
    val isLoading: Boolean = true,
    val levels: List<CatLevelListItemUiModel> = emptyList()
) {
    companion object {

        fun difficultyLabelRes(difficulty: Difficulty): Int = when (difficulty) {
            Difficulty.EASY -> R.string.difficulty_easy
            Difficulty.MEDIUM -> R.string.difficulty_medium
            Difficulty.HARD -> R.string.difficulty_hard
            Difficulty.EXPERT -> R.string.difficulty_expert
        }
    }
}

sealed interface CatLevelSelectEvent {

    data class StartLevel(val levelId: String) : CatLevelSelectEvent
}

sealed interface CatLevelSelectEffect {

    data class NavigateToGameplay(val levelId: String) : CatLevelSelectEffect
}
