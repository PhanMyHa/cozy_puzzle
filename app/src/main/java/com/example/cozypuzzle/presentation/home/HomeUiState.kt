package com.example.cozypuzzle.presentation.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode

data class GameModeUiModel(
    val mode: PuzzleMode,
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int
)

data class HomeUiState(
    val playerName: String = "",
    val dailyPuzzleSize: Int = 0,
    val gameModes: List<GameModeUiModel> = emptyList(),
    val isLoading: Boolean = true
) {
    companion object {
        fun defaultGameModes(): List<GameModeUiModel> = listOf(
            GameModeUiModel(PuzzleMode.BASIC, R.string.game_mode_basic, R.drawable.ic_mode_basic),
            GameModeUiModel(PuzzleMode.FIND_MY_CAT, R.string.game_mode_find_my_cat, R.drawable.ic_mode_cat),
            GameModeUiModel(PuzzleMode.COZY_GARDEN, R.string.game_mode_cozy_garden, R.drawable.ic_tab_garden),
            GameModeUiModel(PuzzleMode.FROG_POND, R.string.game_mode_frog_pond, R.drawable.ic_mode_frog)
        )
    }
}
