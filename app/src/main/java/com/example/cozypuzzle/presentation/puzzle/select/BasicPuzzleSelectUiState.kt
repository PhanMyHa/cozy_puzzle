package com.example.cozypuzzle.presentation.puzzle.select

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.R

data class BasicPuzzleListItemUiModel(
    val id: String,
    val title: String,
    @DrawableRes val imageRes: Int,
    @StringRes val difficultyLabelRes: Int,
    val gridSizes: List<Int>,
    val bestTimeMs: Long?
)

data class BasicPuzzleSelectUiState(
    val isLoading: Boolean = true,
    val puzzles: List<BasicPuzzleListItemUiModel> = emptyList()
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

sealed interface BasicPuzzleSelectEvent {

    data class StartPuzzle(
        val puzzleId: String,
        val gridSize: Int
    ) : BasicPuzzleSelectEvent
}

sealed interface BasicPuzzleSelectEffect {

    data class NavigateToGameplay(
        val puzzleId: String,
        val gridSize: Int
    ) : BasicPuzzleSelectEffect
}
